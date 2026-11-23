package com.zfy.controller;

import com.zfy.pojo.dto.*;
import com.zfy.pojo.vo.*;
import com.zfy.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.zfy.service.JsonSupport.*;

/**
 * 任务业务入口：创建、列表、仿真、状态转换、完成和遥测归档。
 */
@RestController
@RequiredArgsConstructor
public class MissionController {
    // missions：任务业务服务，负责状态变更及仿真归档。
    private final MissionService missions;

    /**
     * 根据规划ID和航线标识创建仿真任务。
     *
     * @param body 由请求JSON绑定的MissionCreateDTO，包含任务名称、类型、关联规划ID和航线标识
     */
    @PostMapping("/api/missions")
    public MissionCreatedVO createMission(@Valid @RequestBody MissionCreateDTO body) {
        return JSON.convertValue(missions.create(body), MissionCreatedVO.class);
    }

    /**
     * 返回任务列表。
     */
    @GetMapping("/api/missions")
    public List<MissionVO> listMissions() {
        return rows(missions.list()).stream().map( row -> JSON.convertValue(row, MissionVO.class)).toList();
    }

    /**
     * 生成指定任务的仿真轨迹，并将任务置为运行中。
     *
     * @param mission_id URL路径中的任务主键，对应missions.id
     */
    @PostMapping("/api/missions/{mission_id}/simulate")
    public SimulationVO simulateMission(@PathVariable("mission_id") long mission_id) {
        return JSON.convertValue(missions.simulate(mission_id), SimulationVO.class);
    }

    /**
     * 按状态机规则暂停、恢复或取消任务。
     *
     * @param mission_id URL路径中的任务主键，对应missions.id
     * @param body 由请求JSON绑定的TransitionDTO，包含任务要切换到的目标状态
     */
    @PostMapping("/api/missions/{mission_id}/state")
    public MissionStateVO missionState(@PathVariable("mission_id") long mission_id, @Valid @RequestBody TransitionDTO body) {
        return JSON.convertValue(missions.transition(mission_id,body.getStatus()), MissionStateVO.class);
    }

    /**
     * 完成仿真任务并归档日志；重复请求复用已有结果。
     *
     * @param mission_id URL路径中的任务主键，对应missions.id
     */
    @PostMapping("/api/missions/{mission_id}/complete-simulation")
    public MissionCompletedVO completeSimulation(@PathVariable("mission_id") long mission_id) {
        return JSON.convertValue(missions.complete(mission_id), MissionCompletedVO.class);
    }

    /**
     * 导入指定任务的遥测批次并返回归档结果。
     *
     * @param mission_id URL路径中的任务主键，对应missions.id
     * @param body 由请求JSON绑定的TelemetryBatchDTO，包含需要归档的遥测点批次
     */
    @PostMapping("/api/missions/{mission_id}/telemetry")
    public TelemetryVO telemetry(@PathVariable("mission_id") long mission_id, @Valid @RequestBody TelemetryBatchDTO body) {
        return JSON.convertValue(missions.telemetry(mission_id,body), TelemetryVO.class);
    }
}
