package com.zfy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 查询规划总数和最新规划ID。
 */
@Mapper
public interface PlanningRunMapper {

    /**
     * 统计所有规划记录，空表返回0。
     */
    @Select("select count(*) from planning_runs")
    public Long countAll();

    /**
     * 读取最大的规划ID；空表返回null，所以返回类型使用Long而非long。
     */
    @Select("select max(id) from planning_runs")
    public Long selectLatestPlanId();
}
