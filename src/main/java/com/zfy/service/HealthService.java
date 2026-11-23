package com.zfy.service;

import com.zfy.mapper.HealthMapper;
import com.zfy.pojo.vo.HealthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


/**
 * 执行轻量SQL确认数据库连接，生成健康检查响应。
 */
@Service

public class HealthService {
    // healthMapper：数据库探活Mapper，执行SELECT 1。
    private final HealthMapper healthMapper;
    public HealthService(HealthMapper healthMapper) { this.healthMapper = healthMapper; }

    /**
     * 检查SELECT 1结果；正常时返回数据库已连接和当前UTC时间。
     */
    public com.zfy.pojo.vo.HealthVO checkConnection() {
        // result：SELECT 1查询结果；只有整数1表示本次检查成功。
        Integer result = healthMapper.checkConnection();
        if (!Integer.valueOf(1).equals(result)) {
            throw new IllegalStateException("数据库检查结果异常");
        }
        // vo：要返回给前端的健康检查VO。
        HealthVO vo = new HealthVO();
        vo.setStatus("ok");
        vo.setDatabase("connected");
        vo.setAlgorithm(null);
        vo.setTime(OffsetDateTime.now(ZoneOffset.UTC).toString());
        return vo;
    }
}
