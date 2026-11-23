package com.zfy.service;

import org.springframework.stereotype.Service;
import java.util.*;
import static com.zfy.service.JsonSupport.*;
import static com.zfy.service.BusinessStore.*;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
 */

/*
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * @Service把本类交给Spring管理；本类只有一个构造器时，Spring自动用它注入依赖，无需再写@Autowired。
 */

/**
 * 管理飞行日志、操作历史、通用计算结果与公开飞行数据归档。
 */
@Service
public class RecordService {
    // store：业务存储组件，统一处理MySQL读写、事务和计算上下文。
    private final BusinessStore store;

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param store 业务存储组件，统一处理MySQL读写、事务和计算上下文
     */
    public RecordService(BusinessStore store) {
        this.store=store;
    }

    /**
     * 将日志行转换为响应对象，按需附带轨迹点。
     *
     * @param row 需要转换的飞行日志数据库行
     * @param detail 是否附带完整轨迹点，列表接口不需要这些点
     */
    public Map<String,Object> logView(Map<String,Object> row,boolean detail) {
        // v：日志响应Map，包含基本信息、指标和导入时间。
        var v=obj("id",row.get("id"),"name",row.get("name"),"source_name",row.get("source_name"),"is_simulation",yes(row.get("is_simulation")),
            "metrics",parse(row.get("metrics_json")),"imported_at",iso(row.get("imported_at")));
        if(detail) v.put("points",parse(row.get("points_json")));
        return v;
    }

    /*
     * 事务Lambda：() -> {...}是无参回调，交给store.tx执行；大括号内的return是回调的返回值。
     * 先完成网络计算，再把关联写库放入短事务，减少长期持有数据库锁的时间。
     */

    /**
     * 检查输入长度、计算轨迹指标并保存日志。
     *
     * @param payload 日志导入请求，含名称、来源、仿真标记和轨迹点
     */
    public Object importLog(Object payload) {
        // p：日志请求的Map形式，用于检查字段长度。
        var p=map(payload);
        length(p.get("name"),180,"name");
        length(p.get("source_name"),180,"source_name");
        // result：轨迹处理后的完整计算响应，包含log产物。
        var result=store.calculate("replay",payload);
        return store.tx(()->logView(store.insert("flight_logs",store.artifact(result,"log")),false));
    }

    /**
     * 按ID读取含轨迹点的日志详情。
     *
     * @param id 本地飞行日志的MySQL主键
     */
    public Object log(long id) {
        return logView(store.get("flight_logs",id),true);
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 读取最近50条日志摘要，把source_name适配为列表契约的source。
     */
    public Object logs() {
        return store.list("flight_logs").stream().limit(50).map(/* row：当前遍历的日志数据库行。 */ row->{
            // v：日志摘要Map，将来源字段适配为source。
            var v=logView(row,false);
            v.put("source",v.remove("source_name"));
            return v;
        }).toList();
    }

    /**
     * 返回最近100条操作事件，解析事件详情JSON。
     */
    public Object operations() {
        return store.list("operation_events").stream().limit(100).map(/* r：当前转换为前端响应的操作事件数据库记录。 */ r->obj("id",r.get("id"),"kind",r.get("kind"),"mission_id",
            r.get("mission_id"),"detail",parse(r.get("detail_json")),"created_at",iso(r.get("created_at")))).toList();
    }

    /**
     * 执行通用计算，按需记录事件，并替换巡检结果的临时事件ID。
     *
     * @param operation 计算操作名称，如plan、simulate或inspection，用于选择内部计算接口
     * @param input 本次操作的DTO或请求对象，具体字段随operation变化
     * @param eventKind 计算成功后要记录的事件类型；为null时不保存该类事件
     */
    public Object calculate(String operation,Object input,String eventKind) {
        // envelope：计算服务完整响应，包含data、artifacts、sources等部分。
        var envelope=store.calculate(operation,input,"restriction_zones","data_source_snapshots");
        // result：计算响应中实际返回给前端的data部分。
        var result=envelope.get("data");
        if(operation.equals("inspection")) map(result).remove("event_id");
        if(eventKind!=null) store.tx(()->{
            // eventId：保存成功事件后得到的MySQL主键，用于替换巡检临时ID。
            long eventId=store.event(eventKind,null,result);
            if(operation.equals("inspection"))map(result).put("event_id",eventId);
            return null;
        });
        return result;
    }

    /**
     * 查询计算服务提供的公开数据目录。
     */
    public Object catalog() {
        return store.calculate("catalog",obj()).get("data");
    }

    /**
     * 查询指定公开飞行数据。
     *
     * @param id 公开数据中的飞行编号，不是本地flight_logs主键
     */
    public Map<String,Object> flight(long id) {
        return map(store.calculate("flight",obj(),obj(),id).get("data"));
    }

    /*
     * findFirst返回Optional，表示可能有记录也可能没有。
     * isPresent检查是否有值，只有确认有值后才调用get，避免空Optional抛出异常。
     * 来源检查和新日志插入在同一个事务中，配合Mapper索引锁处理重复归档。
     */

    /**
     * 读取公开飞行、计算指标，在来源索引锁内检查重复并归档。
     *
     * @param flightId 公开数据集中的飞行编号，用于取数并构造唯一来源名称
     */
    public Object archive(long flightId) {
        // flight：公开数据源返回的飞行详情，包含名称和轨迹点。
        var flight=flight(flightId);
        // source：由公开数据飞行编号构成的来源标识，用于检查重复归档。
        String source="CMU公开数据 / 2019 / flight-"+flightId;
        // request：把公开飞行详情转换成日志计算所需的请求。
        var request=obj("name",flight.get("name"),"source_name",source,"is_simulation",false,"points",flight.get("points"));
        // computed：轨迹指标计算响应，包含待保存的log产物。
        var computed=store.calculate("replay",request);
        return store.tx(()-> {
            // existing：同来源日志查询结果的Optional包装，有值说明已经归档。
            var existing=store.mapper.lockLogsBySource(source).stream().findFirst();
            if(existing.isPresent()) return obj("id",existing.get().get("id"),"already_archived",true);
            return logView(store.insert("flight_logs",store.artifact(computed,"log")),false);
        });
    }
}
