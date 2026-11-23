package com.zfy.service;

import com.zfy.exception.EnvironmentException;
import com.zfy.pojo.dto.EnvironmentQueryDTO;
import com.zfy.pojo.vo.EnvironmentVO;
import org.springframework.stereotype.Service;
import java.util.Set;
import static com.zfy.service.JsonSupport.JSON;

/*
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * @Service把本类交给Spring管理；本类只有一个构造器时，Spring自动用它注入依赖，无需再写@Autowired。
 */

/**
 * 校验环境查询边界，调用规划服务，并检查环境响应的必要字段。
 */
@Service
public class EnvironmentService {
    // planning：规划业务服务，负责环境、限制区和规划快照。
    private final PlanningService planning;

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param planning 规划业务服务，负责环境、限制区和规划快照
     */
    public EnvironmentService(PlanningService planning) {
        this.planning = planning;
    }

    /**
     * 先校验请求，再调用计算服务，确认环境字段齐全后返回VO。
     *
     * @param query 包含经纬度边界和数据模式的环境查询DTO
     */
    public EnvironmentVO getEnvironment(EnvironmentQueryDTO query) {
        validateQuery(query);
        // result：从计算结果转换得到的环境VO，返回前还需检查必要字段。
        EnvironmentVO result = JSON.convertValue(planning.environment(query), EnvironmentVO.class);
        if (result == null || result.getBbox() == null || result.getBbox().size() != 4
            || result.getSources() == null || result.getDataModeUsed() == null
            || !Set.of("real", "simulation", "hybrid").contains(result.getDataModeUsed())) {
            throw new EnvironmentException(502, "计算服务返回的环境缺少必要字段");
        }
        return result;
    }

    /**
     * 校验经纬度为有限数、东西南北顺序正确且数据模式受支持。
     *
     * @param query 需要校验的环境查询DTO
     */
    private void validateQuery(EnvironmentQueryDTO query) {
        if (query == null || !finite(query.getWest()) || !finite(query.getSouth())
            || !finite(query.getEast()) || !finite(query.getNorth())) {
            throw new EnvironmentException(422, "区域边界必须是有效的有限数值");
        }
        if (query.getEast() <= query.getWest() || query.getNorth() <= query.getSouth()) {
            throw new EnvironmentException(422, "bbox参数无效：east必须大于west，north必须大于south");
        }
        if (query.getDataMode() == null || !Set.of("auto", "real", "simulation").contains(query.getDataMode())) {
            throw new EnvironmentException(422, "data_mode只能是auto、real、simulation");
        }
    }

    /**
     * 检查Double非空且不是NaN或正负无穷。
     *
     * @param value 待检查的可空经纬度数值
     */
    private boolean finite(Double value) {
        return value != null && Double.isFinite(value);
    }
}
