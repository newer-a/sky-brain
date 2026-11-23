package com.zfy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 查询任务总数和运行中任务数。
 */
@Mapper
public interface MissionMapper {

    /**
     * 统计所有任务，空表返回0。
     */
    @Select("select count(*) from missions")
    public Long countAll();

    /**
     * 统计status为running的任务。
     */
    @Select("select count(*) from missions where status = 'running'")
    public Long countRunning();
}
