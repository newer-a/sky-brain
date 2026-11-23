package com.zfy.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.*;
import static com.zfy.service.JsonSupport.*;

@Component
@Slf4j
public class FrameCache{
    // TTL：帧缓存保留24小时；过期后自动释放内存，调用方回到重算路径。
    private static final Duration TTL=Duration.ofHours(24);
    // PREFIX：帧缓存的键前缀，便于用redis-cli按模式查看和清理。
    private static final String PREFIX="sky:frames:";

    // redis：字符串模板，键和值都是字符串，值内部再用JSON表达一个对象。
    private final StringRedisTemplate redis;

    public FrameCache(StringRedisTemplate redis) {
        this.redis=redis;
    }
    /**
     * 缓存某个任务的仿真帧；写入失败只记录日志，不影响任务启动。
     *
     * @param mission 任务当前记录，提供id、plan_id和route_key
     * @param algorithmVersion 本次计算使用的算法版本，取计算响应中的algorithm字段
     * @param frames 仿真轨迹帧列表
     */
    public void put(Map<String,Object> mission,Object algorithmVersion,List<Map<String,Object>> frames) {
        try {
            redis.opsForValue().set(PREFIX+number(mission.get("id")),
                    json(obj("plan_id",mission.get("plan_id"),"route_key",mission.get("route_key"),
                            "algorithm_version",algorithmVersion,"cached_at",now().toString(),"frames",frames)),TTL);
        }
        catch(RuntimeException e) {
            log.warn("仿真帧写入Redis失败，本次不做缓存：{}",e.toString());
        }
    }

    /**
     * 读取可复用的仿真帧；未命中、换了规划/航线/算法版本或Redis异常时返回null。
     *
     * @param mission 任务当前记录，用于比对plan_id与route_key
     * @param algorithmVersion 任务当前航线所属的算法版本，取planning_runs.algorithm_version
     */
    public List<Map<String,Object>> get(Map<String,Object> mission,Object algorithmVersion) {
        try {
            // raw：缓存中的JSON文本；未命中时为null。
            var raw=redis.opsForValue().get(PREFIX+number(mission.get("id")));
            if(raw==null) return null;
            // cached：解析后的缓存内容，含校验字段与frames。
            var cached=objectJson(raw);
            if(number(cached.get("plan_id"))!=number(mission.get("plan_id"))) return null;
            if(!Objects.equals(cached.get("route_key"),mission.get("route_key"))) return null;
            if(!Objects.equals(cached.get("algorithm_version"),algorithmVersion)) return null;
            return rows(cached.get("frames"));
        }
        catch(RuntimeException e) {
            log.warn("仿真帧读取Redis失败，降级为重新计算：{}",e.toString());
            return null;
        }
    }

    /**
     * 主动删除某个任务的帧缓存；任务取消或重规划后可调用。
     *
     * @param missionId 任务主键
     */
    public void evict(long missionId) {
        try { redis.delete(PREFIX+missionId); }
        catch(RuntimeException e) { log.warn("仿真帧删除失败：{}",e.toString()); }
    }
}
