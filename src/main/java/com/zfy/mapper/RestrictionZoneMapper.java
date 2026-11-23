package com.zfy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 统计所有限制区记录，供工作台汇总使用。
 */
@Mapper
public interface RestrictionZoneMapper {

    /**
     * 统计全部限制区，包括尚未生效和已经过期的记录。
     */
    @Select("select count(*) from restriction_zones")
    public Long countAll();

    /**
     * 查询当前时刻生效的限制区；有效期为空表示该侧不设限。
     *
     * @param now 判断生效区间用的当前UTC时间
     */
    @Select("select * from restriction_zones "
            + "where (effective_from is null or effective_from <= #{now}) "
            + "  and (effective_to   is null or effective_to   >= #{now})")
    public List<Map<String,Object>> listEffective(@Param("now") java.time.LocalDateTime now);
}
