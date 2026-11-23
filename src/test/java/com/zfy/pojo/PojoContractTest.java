package com.zfy.pojo;

import com.zfy.pojo.dto.*;
import com.zfy.pojo.vo.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** 不启动Spring、不连接MySQL，验证迁移时容易弄错的JSON契约。 */
class PojoContractTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test void planDefaultsAndSnakeCase() {
        var request = mapper.readValue("""
          {"start":{"longitude":114.05,"latitude":22.53},
           "end":{"longitude":114.07,"latitude":22.55},"reserve_battery_percent":30}
          """, PlanDTO.class);
        assertEquals(30D, request.getReserveBatteryPercent());
        assertEquals(120D, request.getStart().getAltitudeM());
        assertEquals(86D, request.getDrone().getBatteryPercent());
        assertTrue(request.getReturnToStart());
        assertNotNull(request.getWaypoints());
        var json = mapper.valueToTree(request);
        assertTrue(json.has("reserve_battery_percent"));
        assertFalse(json.has("reserveBatteryPercent"));
        assertFalse(json.has("finiteNumbers"));
        assertFalse(json.has("consistent"));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
            request.setAltitudeReference("MSL");
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }

    @Test void nullableBatteryStillHasBounds() {
        var request = mapper.readValue("""
          {"plan_id":1,"geojson":{},"current_position":{"longitude":114,"latitude":22},"battery_percent":101}
          """, ReplanDTO.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(request).isEmpty());
            request.setBatteryPercent(50D);
            assertTrue(factory.getValidator().validate(request).isEmpty());
            request.setBatteryPercent(null);
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }

    @Test void logNullMetricsAndBooleanNameRemainCompatible() {
        var log = new LogSummaryVO();
        log.setIsSimulation(false);
        log.setMetrics(new LogMetricsVO());
        var json = mapper.valueToTree(log);
        assertTrue(json.has("is_simulation"));
        assertFalse(json.has("isSimulation"));
        assertTrue(json.get("metrics").get("battery_drop_percent").isNull());
    }

    @Test void draftBranchesHaveDifferentShapes() {
        var first = new DraftAppliedVO(); first.setDraftId(1L);
        var again = new DraftAlreadyAppliedVO(); again.setDraftId(1L); again.setAlreadyApplied(true);
        assertFalse(mapper.valueToTree(first).has("already_applied"));
        assertFalse(mapper.valueToTree(again).has("request"));
        var detail = new PlanDetailVO(); detail.setPlanId(2L); detail.setRequest(new PlanDTO());
        assertTrue(mapper.valueToTree(detail).has("plan_id"));
        assertTrue(mapper.valueToTree(detail).has("request"));
    }

    @Test void replayAllowsEqualTimeButTelemetryDoesNot() {
        var log = new ReplayImportDTO(); log.setName("样例"); log.setSourceName("测试");
        log.setPoints(List.of(Map.of("longitude",114,"latitude",22,"t_s",1),Map.of("longitude",114.1,"latitude",22.1,"t_s",1)));
        assertTrue(log.isPointsValid());
        var batch = new TelemetryBatchDTO();
        var p = new TelemetryPointDTO(); p.setTS(1D);
        batch.setPoints(List.of(p,p));
        assertFalse(batch.isConsistent());
        log.setPoints(List.of(Map.of("longitude",999,"latitude",22),Map.of("longitude",114,"latitude",22)));
        assertFalse(log.isPointsValid());
    }
}
