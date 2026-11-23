package com.zfy.service;

import com.zfy.exception.BusinessException;
import com.zfy.mapper.BusinessMapper;
import com.zfy.mapper.RestrictionZoneMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.function.Supplier;
import static com.zfy.service.JsonSupport.*;


/**
 * 封装数据访问、短事务、计算上下文和操作事件；网络计算通常放在业务事务之外。
 */
@Service
public class BusinessStore {
    public final BusinessMapper mapper;
    // compute：计算服务HTTP客户端，负责请求Python计算接口。
    private final ComputeClient compute;
    // transactions：Spring事务模板，用于执行提交或回滚受控的数据库操作。
    private final TransactionTemplate transactions;
    private final RestrictionZoneMapper restrictionZoneMapper;

    public BusinessStore(BusinessMapper mapper, ComputeClient compute, PlatformTransactionManager manager,
                         RestrictionZoneMapper restrictionZoneMapper) {
        this.mapper=mapper;this.compute=compute;
        this.transactions=new TransactionTemplate(manager);
        this.transactions.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
        this.restrictionZoneMapper=restrictionZoneMapper;
    }

    /*
     * 泛型与事务回调：<T>声明这个方法自己的类型参数，返回类型T跟随action的返回值。
     * Supplier<T>表示“不接收参数、返回T”的函数，action.get()才真正执行它。
     * status -> action.get()是Lambda；Spring开启事务后调用它，正常返回则提交，抛出运行时异常则回滚。
     * 需要返回空结果时使用return null；这不代表事务失败。
     */

    /**
     * 在Spring事务中执行回调，成功提交，未捕获的运行时异常触发回滚。
     *
     * @param action 需要放在事务内执行的无参业务回调，其返回值作为事务结果
     * @param <T> 事务回调的返回值类型
     */
    public <T> T tx(Supplier<T> action) {
        return transactions.execute(/* status：Spring传入的事务状态对象；当前回调未直接使用它。 */ status->action.get());
    }

    /**
     * 按ID倒序读取指定白名单表的全部记录。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     */
    public List<Map<String,Object>> list(String table) {
        return mapper.list(table);
    }

    /**
     * 按表名和ID读取记录；不存在时抛出404业务异常。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要读取的记录主键
     */
    public Map<String,Object> get(String table,long id) {
        // row：按主键查询的数据库行；为null表示不存在。
        var row=mapper.find(table,id);
        if(row==null) throw new BusinessException(404,"记录不存在："+table+"/"+id);
        return row;
    }

    /**
     * 按ID读取并锁住记录；必须在事务中调用才能覆盖后续更新。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要加锁读取的记录主键
     */
    public Map<String,Object> lock(String table,long id) {
        // row：FOR UPDATE查询得到的数据库行。
        var row=mapper.lock(table,id);
        if(row==null) throw new BusinessException(404,"记录不存在："+table+"/"+id);
        return row;
    }

    /*
     * 三元表达式：条件 ? 值A : 值B表示条件成立取A，否则取B。
     * 这里只计算被选中的分支，常用于处理可空字段或选择响应结构。
     */

    /**
     * 规范化字段，补齐时间并插入记录，再根据数据库生成的ID读取完整行。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param values 待写入的列名与字段值，调用方不必预先把JSON对象转成字符串
     */
    public Map<String,Object> insert(String table, Map<String,Object> values) {
        // row：规范化后的待插入字段，Mapper执行后会回填id。
        var row=normalize(values);
        row.remove("id");
        // date：当前表用于记录创建/导入/抓取时间的列名。
        String date=table.equals("flight_logs")?"imported_at":table.equals("data_source_snapshots")?"fetched_at":"created_at";
        row.putIfAbsent(date,now());
        if(table.equals("missions")) row.putIfAbsent("updated_at",now());
        mapper.insert(table,row);
        return get(table,number(row.get("id")));
    }

    /**
     * 规范化字段并更新指定记录；任务表额外刷新updated_at。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要更新的记录主键
     * @param values 待写入的列名与字段值，调用方不必预先把JSON对象转成字符串
     */
    public void update(String table,long id, Map<String,Object> values) {
        // row：规范化后的待更新字段，任务表会追加updated_at。
        var row=normalize(values);
        if(table.equals("missions")) row.put("updated_at",now());
        mapper.update(table,id,row);
    }

    /*
     * replaceAll((key, value) -> {...})会逐个读取Map中的键和值，使用Lambda返回值替换原值。
     * 它修改的是前面创建的副本，不是调用方传入的values。
     * instanceof String用于判断已经是JSON文本的值，避免再次序列化成带转义引号的文本。
     */

    /**
     * 复制字段Map，把JSON对象转为字符串，把时间字段转为LocalDateTime。
     *
     * @param values 待写入的列名与字段值，调用方不必预先把JSON对象转成字符串
     */
    private Map<String,Object> normalize(Map<String,Object> values) {
        // result：原字段Map的可修改副本，避免修改调用方的Map。
        var result=new LinkedHashMap<String,Object>(values);
        result.replaceAll((/* key：当前正在规范化的数据库列名。 */ key,/* value：该列原始字段值，可能为JSON对象、时间或普通标量。 */ value)-> {
            if(value==null) return null;
            if(key.endsWith("_json") || key.equals("geojson")) return value instanceof String?value:json(value);
            if(key.endsWith("_at") || key.equals("effective_from") || key.equals("effective_to")) return time(value);
            return value;
        });
        return result;
    }

    /**
     * 把一行数据库记录的顶层时间字段转成ISO字符串，供计算服务使用。
     *
     * @param row 数据库查询返回的一行记录
     */
    private Map<String,Object> isoRow(Map<String,Object> row) {
        // copy：数据库记录的副本，转换时间时不修改原始Map。
        var copy=new LinkedHashMap<>(row);
        copy.replaceAll((k,v)->v instanceof java.sql.Timestamp || v instanceof java.time.LocalDateTime?iso(v):v);
        return copy;
    }
    /**
     * 读取指定表，转换时间字段，构造计算服务需要的业务快照。
     *
     * @param tables 本次计算需要读取的业务表名集合
     */
    public Map<String,Object> context(String... tables) {
        var context=new LinkedHashMap<String,Object>();
        for(String table:tables) context.put(table,list(table).stream().map(this::isoRow).toList());
        return context;
    }
    /**
     * 组装某个任务计算所需的最小上下文：目标任务一行、关联规划一行、当前生效的限制区。
     * 与context(...)相比，不读取整张missions和planning_runs表。
     *
     * @param missionId 目标任务的MySQL主键
     */
    public Map<String,Object> contextForMission(long missionId) {
        // mission：目标任务记录；不存在说明任务ID无效。
        var mission=mapper.find("missions",missionId);
        if(mission==null) throw new BusinessException(404,"任务不存在："+missionId);
        // plan：任务关联的规划记录，计算服务按plan_id读取航线、环境快照与机型参数。
        var plan=mapper.find("planning_runs",number(mission.get("plan_id")));
        if(plan==null) throw new BusinessException(409,"任务关联的规划不存在："+mission.get("plan_id"));
        return obj("missions",List.of(isoRow(mission)),"planning_runs",List.of(isoRow(plan)),
                "restriction_zones",restrictionZoneMapper.listEffective(now()));
    }
    /**
     * 调用计算服务，保存新产生的数据源快照；AI计算异常时尽力记录失败摘要。
     *
     * @param operation 计算操作名称，如plan、simulate或inspection，用于选择内部计算接口
     * @param payload 本次计算的请求内容，具体结构由operation决定
     * @param context 提供给计算服务的业务快照，键为表名，值为该表记录列表
     * @param id 计算关联的记录ID；无指定记录时为null
     */
    public Map<String,Object> calculate(String operation,Object payload,Map<String,Object> context,Long id) {
        // response：计算服务返回的完整响应Map。
        Map<String,Object> response;
        try {
            response=compute.call(operation,payload,context,id);
        }
        catch(/* e：计算或业务校验抛出的异常，携带HTTP状态码与失败原因。 */ BusinessException e) {
            // 只保存失败摘要，不记录密钥、完整图片或完整模型提示词。
            if(Set.of("draft","fleet","fleet-apply","diagnosis","inspection","ai-command").contains(operation)) {
                try {
                    tx(()->{
                        event("ai_operation_failed",null,obj("operation",operation,"status",e.getStatus(),"reason",e.getMessage()));
                        return null;
                    });
                }
                catch(/* loggingError：保存失败事件时发生的第二个异常，附加到原异常而不覆盖原失败原因。 */ RuntimeException loggingError) {
                    e.addSuppressed(loggingError);
                }
            }
            throw e;
        }
        // sources：计算服务本次新产生的数据源快照列表。
        var sources=rows(response.getOrDefault("sources",List.of()));
        if(!sources.isEmpty()) tx(()->{
            for(/* source：当前正在保存的一条新数据源快照。 */ var source:sources) insert("data_source_snapshots",source);
            return null;
        });
        return response;
    }

    /*
     * 异常补充：catch中再次写失败日志也可能失败，addSuppressed把第二个异常附在原异常上。
     * 随后throw e重新抛出原始失败，使接口仍能说明最先发生的计算错误。
     * 同名calculate有两种参数列表，这是方法重载；编译器按参数类型/数量决定调用哪个版本。
     */

    /**
     * 调用计算服务，保存新产生的数据源快照；AI计算异常时尽力记录失败摘要。
     *
     * @param operation 计算操作名称，如plan、simulate或inspection，用于选择内部计算接口
     * @param payload 本次计算的请求内容，具体结构由operation决定
     * @param tables 本次计算需要读取的业务表名集合
     */
    public Map<String,Object> calculate(String operation,Object payload,String... tables) {
        return calculate(operation,payload,context(tables),null);
    }

    /**
     * 从计算响应的artifacts中取出指定持久化产物，缺失则报502。
     *
     * @param envelope 计算服务完整响应，包含data、artifacts、sources等部分
     * @param key 需要取出的产物名称，如plan、log、restriction或event
     */
    public Map<String,Object> artifact(Map<String,Object> envelope,String key) {
        // artifacts：响应中按名称组织的持久化产物集合。
        var artifacts=map(envelope.get("artifacts"));
        if(artifacts==null || !artifacts.containsKey(key)) throw new BusinessException(502,"计算服务缺少产物："+key);
        return map(artifacts.get(key));
    }

    /**
     * 保存一条操作事件并返回MySQL生成的事件ID。
     *
     * @param kind 操作事件类型，如mission_completed或replanned
     * @param missionId 关联任务的MySQL主键；允许为空的场景表示不关联具体任务
     * @param detail 操作详情，保存时会转换为detail_json字符串
     */
    public long event(String kind,Long missionId,Object detail) {
        return number(insert("operation_events",obj("kind",kind,"mission_id",missionId,"detail_json",detail)).get("id"));
    }

    /**
     * 查询指定任务指定类型的最新事件；没有记录时返回null。
     *
     * @param kind 操作事件类型，如mission_completed或replanned
     * @param missionId 关联任务的MySQL主键；允许为空的场景表示不关联具体任务
     */
    public Map<String,Object> lastEvent(String kind,long missionId) {
        return list("operation_events").stream().filter(/* r：正在匹配事件类型与任务ID的一条操作记录。 */ r->kind.equals(r.get("kind"))&&r.get("mission_id")!=null&&number(r.get("mission_id"))==missionId).findFirst().orElse(null);
    }

    /**
     * 统一检查业务条件；不成立时抛出指定状态码的业务异常。
     *
     * @param condition 必须成立的业务条件，为false时拒绝当前操作
     * @param status 要返回的HTTP状态码，如422参数错误、409业务冲突
     * @param message 向接口调用方解释失败原因的文字
     */
    public static void require(boolean condition,int status,String message) {
        if(!condition) throw new BusinessException(status,message);
    }

    /**
     * 检查可选字符串长度，超出上限时返回422。
     *
     * @param value 需要检查长度的可空字段值
     * @param max 允许的最大字符数
     * @param field 字段显示名称，用于生成明确的长度错误信息
     */
    public static void length(Object value,int max,String field) {
        require(value==null || value.toString().length()<=max,422,field+"长度不能超过"+max);
    }
}
