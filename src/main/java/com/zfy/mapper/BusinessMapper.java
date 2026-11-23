package com.zfy.mapper;

import org.apache.ibatis.annotations.*;
import java.util.*;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 七张业务表的通用MyBatis访问接口；SQL由受白名单约束的Provider生成。
 */
@Mapper
public interface BusinessMapper {

    /**
     * 查询指定表全部记录，按ID从大到小排序。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     */
    @SelectProvider(type=BusinessSqlProvider.class, method="list")
    List<Map<String,Object>> list(@Param("table") String table);

    /**
     * 按主键查询一条记录，不存在时返回null。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要查询记录的主键
     */
    @SelectProvider(type=BusinessSqlProvider.class, method="find")
    Map<String,Object> find(@Param("table") String table, @Param("id") long id);

    /**
     * 按主键查询并加排他锁，供同一事务内安全更新。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要加锁的记录主键
     */
    @SelectProvider(type=BusinessSqlProvider.class, method="lock")
    Map<String,Object> lock(@Param("table") String table, @Param("id") long id);

    /*
     * MyBatis Provider：@InsertProvider指定生成SQL的方法，真正执行SQL仍由MyBatis负责。
     * @Param("row")给字段Map命名，所以SQL可用#{row.name}读取字段。
     * @Options启用数据库自增主键回填；keyProperty="row.id"表示把新ID写回参数Map，而非方法返回值。
     */

    /**
     * 插入记录，返回影响行数，并将生成的主键回填到row.id。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param row 待插入的列名和值；执行后MyBatis把自增主键写回row.id
     */
    @InsertProvider(type=BusinessSqlProvider.class, method="insert")
    @Options(useGeneratedKeys=true, keyProperty="row.id")
    int insert(@Param("table") String table, @Param("row") Map<String,Object> row);

    /**
     * 更新指定ID的字段，返回影响行数。
     *
     * @param table 本次访问的业务表名，必须在Provider白名单内
     * @param id 要更新的记录主键
     * @param row 需要更新的列名和值，不包含主键id
     */
    @UpdateProvider(type=BusinessSqlProvider.class, method="update")
    int update(@Param("table") String table, @Param("id") long id, @Param("row") Map<String,Object> row);

    /**
     * 仅在当前状态等于expected时改为status，返回1表示本次抢占/恢复成功。
     *
     * @param id 要更新的草案主键
     * @param expected 期望数据库中仍保持的旧状态，不匹配时影响行数为0
     * @param status 希望写入的草案新状态，如applying、applied或draft
     */
    @Update("update planning_drafts set status=#{status} where id=#{id} and status=#{expected}")
    int changeDraft(@Param("id") long id, @Param("expected") String expected, @Param("status") String status);
    // REPEATABLE READ事务下通过source索引记录/间隙锁防止同一来源重复插入。

    /*
     * FOR UPDATE需要在事务内生效；本项目使用REPEATABLE READ隔离级别。
     * 按source_name索引锁定已有记录或匹配范围，配合锁后检查，减少同一来源并发重复归档。
     * 不要单独把此方法当普通查询调用后就结束事务，否则不能保护后续插入。
     */

    /**
     * 查询并锁定同一来源的日志，用于归档去重。
     *
     * @param source 公开飞行数据的来源标识，用于查找同一来源的已有归档
     */
    @Select("select * from flight_logs where source_name=#{source} for update")
    List<Map<String,Object>> lockLogsBySource(String source);
}
