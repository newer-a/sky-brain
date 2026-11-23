package com.zfy.mapper;

import java.util.*;
import java.util.stream.Collectors;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
 */

/**
 * 只生成SQL字符串、不直接执行SQL；表名/列名必须通过白名单，数据值使用绑定参数。
 */
public class BusinessSqlProvider {
    // COLUMNS：业务表到允许列名集合的映射，防止动态SQL拼入任意标识符。
    private static final Map<String,Set<String>> COLUMNS = Map.of(
        "data_source_snapshots", Set.of("id","source_key","source_name","endpoint","available","is_cache","observed_at",
        "fetched_at","error","record_count","payload_json"),
        "restriction_zones", Set.of("id","name","category","severity","min_altitude_m","max_altitude_m","source_name","source_url",
        "effective_from","effective_to","geojson","created_at"),
        "planning_runs", Set.of("id","request_json","result_json","environment_json","algorithm_version","created_at"),
        "missions", Set.of("id","name","task_type","status","execution_mode","plan_id","route_key","drone_json","created_at",
        "updated_at"),
        "flight_logs", Set.of("id","name","source_name","is_simulation","points_json","metrics_json","imported_at"),
        "planning_drafts", Set.of("id","mode","status","prompt","payload_json","applied_plan_id","created_at"),
        "operation_events", Set.of("id","kind","mission_id","detail_json","created_at"));

    /**
     * 从MyBatis参数Map取出表名并通过白名单验证。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    private String table(Map<String,Object> p) {
        // t：从参数取出的表名，接下来检查是否位于白名单中。
        String t=(String)p.get("table");
        if(!COLUMNS.containsKey(t)) throw new IllegalArgumentException("未知业务表");
        return t;
    }

    /*
     * p.get("row")的声明类型是Object，需要先强转为字段Map再取keySet。
     * new ArrayList<>(...)把键集合复制成列表；后续生成列名和值占位符都使用同一顺序。
     * 只允许白名单列且禁止写id，避免外部输入控制任意SQL字段。
     */

    /**
     * 验证写入列名，禁止手动写id和空字段集合。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    @SuppressWarnings("unchecked")
    private List<String> keys(Map<String,Object> p) {
        // t：通过白名单验证的表名。
        String t=table(p);
        // keys：从row中提取的待写列名列表，需要校验且禁止包含id。
        var keys=new ArrayList<>(((Map<String,Object>)p.get("row")).keySet());
        if(!COLUMNS.get(t).containsAll(keys) || keys.contains("id")) throw new IllegalArgumentException("非法写入列");
        if(keys.isEmpty()) throw new IllegalArgumentException("写入内容为空");
        return keys;
    }

    /**
     * 生成按ID倒序查询全表的SQL。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    public String list(Map<String,Object> p) {
        return "select * from "+table(p)+" order by id desc";
    }

    /**
     * 生成按ID查询记录的SQL。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    public String find(Map<String,Object> p) {
        return "select * from "+table(p)+" where id=#{id}";
    }

    /**
     * 在主键查询SQL后附加FOR UPDATE。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    public String lock(Map<String,Object> p) {
        return find(p)+" for update";
    }

    /*
     * 表名/列名不能作为普通SQL值绑定，所以只拼接经过白名单验证的标识符。
     * #{row.列名}是MyBatis值占位符，最终转为预编译参数；它不是把字段值直接拼进SQL。
     * Collectors.joining(",")将各列的占位符按逗号拼成VALUES列表。
     */

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 生成列名与绑定占位符一一对应的INSERT语句。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    public String insert(Map<String,Object> p) {
        // keys：已通过白名单的列名列表，列顺序与值占位符顺序保持一致。
        var keys=keys(p);
        return "insert into "+table(p)+" ("+String.join(",", keys)+") values ("+keys.stream().map(/* k：当前列名，用来生成对应的#{row.列名}值占位符。 */ k->"#{row."+k+"}").collect(Collectors.joining(","))+")";
    }

    /*
     * k -> k + "=#{row." + k + "}"是Lambda，含义是把每个列名转换为一个SET赋值片段。
     * joining把这些片段用逗号连接；WHERE中的#{id}仍由MyBatis绑定实际ID。
     */

    /**
     * 生成指定列的SET子句，并按ID更新记录。
     *
     * @param p MyBatis按@Param名称组织的参数Map，可能含table、id、row
     */
    public String update(Map<String,Object> p) {
        return "update "+table(p)+" set "+keys(p).stream().map(/* k：当前待更新列名，用来生成列名=#{row.列名}的SET片段。 */ k->k+"=#{row."+k+"}").collect(Collectors.joining(","))+" where id=#{id}";
    }
}
