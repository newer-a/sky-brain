package com.zfy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 通过SELECT 1测试数据库连接，不读取业务表内容。
 */
@Mapper
public interface HealthMapper {

    /**
     * 执行SELECT 1确认连接可用；正常返回整数1，不需要业务表有数据。
     */
    @Select("select 1")
    public Integer checkConnection();
}
