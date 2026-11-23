package com.zfy.controller;

import com.zfy.pojo.dto.*;
import com.zfy.pojo.vo.*;
import com.zfy.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.zfy.service.JsonSupport.*;

/*
 * Spring入口注解：@RestController让返回对象自动序列化为JSON；Mapping注解把URL映射到方法。
 * @RequestBody把JSON绑定到DTO，@Valid触发DTO字段校验；@PathVariable读取路径中的ID。
 */

/*
 * Lombok构造器注入：@RequiredArgsConstructor为尚未初始化的final字段生成构造器，Spring通过构造器传入Service。
 * final只限制字段引用不再重新赋值，不会让Service对象本身变成不可修改。
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
 * 类字面量：PlanVO.class等表达式表示目标类型的信息，不是创建一个VO实例。
 * JSON.convertValue(结果, 某VO.class)按字段把Service结果转换为VO，再由Spring输出JSON。
 */

/**
 * 公开飞行数据入口：目录、单次飞行详情和归档到MySQL。
 */
@RestController
@RequiredArgsConstructor
public class DatasetController {
    // records：日志与记录业务服务，负责计算结果归档。
    private final RecordService records;

    /**
     * 返回可用的公开飞行数据目录。
     */
    @GetMapping("/api/datasets/catalog")
    public PublicCatalogVO publicCatalog() {
        return JSON.convertValue(records.catalog(), PublicCatalogVO.class);
    }

    /**
     * 返回指定公开飞行数据及轨迹点。
     *
     * @param flight_id URL路径中的公开数据飞行编号，不是本地日志主键
     */
    @GetMapping("/api/datasets/flights/{flight_id}")
    public PublicFlightVO publicFlight(@PathVariable("flight_id") long flight_id) {
        return JSON.convertValue(records.flight(flight_id), PublicFlightVO.class);
    }

    /**
     * 归档公开飞行数据；已存在时返回已有日志ID。
     *
     * @param flight_id URL路径中的公开数据飞行编号，不是本地日志主键
     */
    @PostMapping("/api/datasets/flights/{flight_id}/archive")
    public Object archivePublicFlight(@PathVariable("flight_id") long flight_id) {
        // result：归档服务返回的Map，包含新日志或already_archived重复标记。
        var result = map(records.archive(flight_id));
        if (yes(result.get("already_archived"))) return JSON.convertValue(result, AlreadyArchivedVO.class);
        return JSON.convertValue(result, LogSummaryVO.class);
    }
}
