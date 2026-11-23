package com.zfy.service;

import com.zfy.mapper.DataSourceSnapshotMapper;
import com.zfy.mapper.MissionMapper;
import com.zfy.mapper.PlanningRunMapper;
import com.zfy.mapper.RestrictionZoneMapper;
import com.zfy.pojo.entity.DataSourceSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 不启动Spring或访问数据库，验证空库和响应转换的边界情况。 */
class DashboardServiceTest {
    private DashboardService service;
    private Long missionCount = 0L;
    private Long runningCount = 0L;
    private Long planCount = 0L;
    private Long latestPlanId;
    private Long restrictionCount = 0L;
    private List<DataSourceSnapshot> snapshots = List.of();

    @BeforeEach
    void setUp() {
        service = new DashboardService();
        // 用内存查询结果代替数据库，不需要运行MySQL或加载动态代理测试agent。
        service.missionMapper = new MissionMapper() {
            public Long countAll() { return missionCount; }
            public Long countRunning() { return runningCount; }
        };
        service.planningRunMapper = new PlanningRunMapper() {
            public Long countAll() { return planCount; }
            public Long selectLatestPlanId() { return latestPlanId; }
        };
        service.restrictionZoneMapper = () -> restrictionCount;
        service.dataSourceSnapshotMapper = () -> snapshots;
        ReflectionTestUtils.setField(service, "deepseekApiKey", "");
        ReflectionTestUtils.setField(service, "deepseekModel", "test-model");
    }

    @Test
    void emptyDatabaseReturnsZerosEmptyListAndNullablePlanId() {
        var result = service.getDashboard();
        assertEquals(0L, result.getMissions());
        assertEquals(0L, result.getActiveMissions());
        assertEquals(0L, result.getPlanningRuns());
        assertEquals(0L, result.getRestrictionZones());
        assertNull(result.getLatestPlanId());
        assertTrue(result.getDataSources().isEmpty());
        assertFalse(result.getAi().getAvailable());
        assertFalse(result.getAi().getConfigured());
        assertFalse(result.getAi().getConnectionVerified());
        assertEquals("test-model", result.getAi().getModel());
        assertDoesNotThrow(() -> OffsetDateTime.parse(result.getComputedAt()));
    }

    @Test
    void retainsFailedSourceAndHandlesMissingObservationTime() {
        var snapshot = new DataSourceSnapshot();
        snapshot.setSourceKey("weather");
        snapshot.setSourceName("天气");
        snapshot.setAvailable(false);
        snapshot.setIsCache(true);
        snapshot.setFetchedAt(LocalDateTime.of(2026, 11, 9, 10, 0));
        snapshot.setRecordCount(0);
        snapshot.setError("连接超时");
        snapshots = List.of(snapshot);
        missionCount = 5L;
        runningCount = 2L;
        planCount = 3L;
        latestPlanId = 9L;
        restrictionCount = 4L;

        var result = service.getDashboard();
        assertEquals(5L, result.getMissions());
        assertEquals(2L, result.getActiveMissions());
        assertEquals(3L, result.getPlanningRuns());
        assertEquals(9L, result.getLatestPlanId());
        assertEquals(4L, result.getRestrictionZones());
        assertEquals(1, result.getDataSources().size());
        var source = result.getDataSources().getFirst();
        assertEquals("weather", source.getKey());
        assertEquals("天气", source.getName());
        assertFalse(source.getAvailable());
        assertTrue(source.getIsHistoricalCache());
        assertEquals("2026-11-09T10:00:00", source.getFetchedAt());
        assertNull(source.getObservedAt());
        assertEquals(0, source.getRecordCount());
        assertEquals("连接超时", source.getError());
    }

    @Test
    void configuredKeyDoesNotMeanConnectionVerified() {
        ReflectionTestUtils.setField(service, "deepseekApiKey", "test-key");
        var ai = service.getDashboard().getAi();
        assertTrue(ai.getAvailable());
        assertTrue(ai.getConfigured());
        assertFalse(ai.getConnectionVerified());

        ReflectionTestUtils.setField(service, "deepseekApiKey", "   ");
        assertFalse(service.getDashboard().getAi().getConfigured());
    }
}
