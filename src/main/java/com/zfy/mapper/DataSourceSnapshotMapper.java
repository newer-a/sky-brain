package com.zfy.mapper;

import com.zfy.pojo.entity.DataSourceSnapshot;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * MyBatis代理：@Mapper接口没有手写实现类，框架创建代理并执行注解或XML中的SQL。
 * 返回值根据方法声明映射成单行对象、列表或影响行数。
 */

/**
 * 读取工作台所需的最新数据源快照；复杂SQL放在同名XML中。
 */
@Mapper
public interface DataSourceSnapshotMapper {

    /**
     * 查询每种source_key最新的一条快照；同时间按ID取较大值，空表返回空列表。
     * 失败快照也保留；SQL位于resources/mapper/DataSourceSnapshotMapper.xml。
     */
    public List<DataSourceSnapshot> selectLatestBySourceKey();
}
