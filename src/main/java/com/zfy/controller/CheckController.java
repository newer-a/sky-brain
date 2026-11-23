package com.zfy.controller;

import com.zfy.pojo.vo.DashboardVO;
import com.zfy.pojo.vo.EnvironmentVO;
import com.zfy.pojo.vo.HealthVO;
import com.zfy.pojo.dto.EnvironmentQueryDTO;
import com.zfy.service.DashboardService;
import com.zfy.service.HealthService;
import com.zfy.service.EnvironmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/*
 * Spring入口注解：类上的@RequestMapping("/api")与方法路径组合成完整URL。
 * @RequestParam读取URL查询参数，defaultValue是在未提供参数时使用的默认值。
 * @Slf4j由Lombok生成名为log的日志字段；它不是本类手写声明的局部变量。
 */

/*
 * @Autowired是Spring依赖注入：容器找到对应Mapper或Service对象并赋给字段，无需在这里new。
 */

/**
 * 健康检查、工作台汇总和环境查询入口。
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class CheckController {
    // healthService：健康检查业务服务，用于验证数据库连接。
    @Autowired
    HealthService healthService;
    // dashboardService：工作台业务服务，用于汇总统计和数据源状态。
    @Autowired
    DashboardService dashboardService;
    // environmentService：环境业务服务，用于校验边界并查询环境。
    @Autowired
    EnvironmentService environmentService;

    /**
     * 检查数据库连接并返回健康状态。
     */
    @GetMapping("/health")
    public HealthVO health() {
        log.info("检查数据库连接~");
        // healthVO：数据库连通性检查结果，随后记录日志并返回前端。
        HealthVO healthVO = healthService.checkConnection();
        log.info("数据库连接正常:{}", healthVO);
        return healthVO;
    }

    /**
     * 查询工作台计数、数据源状态和AI配置摘要。
     */
    @GetMapping("/dashboard")
    public DashboardVO dashboard() {
        log.info("工作台信息查询~");
        return dashboardService.getDashboard();
    }

    /**
     * 把URL查询参数组装成DTO，查询指定范围的环境。
     *
     * @param west 查询区域西侧经度，单位为度
     * @param south 查询区域南侧纬度，单位为度
     * @param east 查询区域东侧经度，单位为度
     * @param north 查询区域北侧纬度，单位为度
     * @param dataMode 数据模式：auto自动选择、real真实数据、simulation仿真数据
     */
    @GetMapping("/environment")
    public EnvironmentVO environment(
        @RequestParam(defaultValue = "114.03") Double west,
        @RequestParam(defaultValue = "22.52") Double south,
        @RequestParam(defaultValue = "114.09") Double east,
        @RequestParam(defaultValue = "22.58") Double north,
        @RequestParam(name = "data_mode", defaultValue = "auto") String dataMode) {
        log.info("环境信息查询！");
        // URL参数叫data_mode，Java属性叫dataMode，显式赋值避免绑定不到。
        // query：把URL参数汇总后的环境查询DTO。
        EnvironmentQueryDTO query = new EnvironmentQueryDTO();
        query.setWest(west);
        query.setSouth(south);
        query.setEast(east);
        query.setNorth(north);
        query.setDataMode(dataMode);
        return environmentService.getEnvironment(query);
    }
}
