package com.zfy.service;

import com.zfy.mapper.DataSourceSnapshotMapper;
import com.zfy.mapper.MissionMapper;
import com.zfy.mapper.PlanningRunMapper;
import com.zfy.mapper.RestrictionZoneMapper;
import com.zfy.pojo.entity.DataSourceSnapshot;
import com.zfy.pojo.vo.AiSummaryVO;
import com.zfy.pojo.vo.DashboardVO;
import com.zfy.pojo.vo.SourceMetaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/*
 * @Autowired是Spring依赖注入：容器找到对应Mapper或Service对象并赋给字段，无需在这里new。
 */

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * @Value读取配置占位符；${配置名:默认值}在配置缺失时采用默认值，嵌套占位符可继续尝试环境变量。
 */

/**
 * 聚合多个Mapper的统计结果和最新数据源快照，组装工作台响应。
 */
@Service
public class DashboardService {
    // dataSourceSnapshotMapper：数据源快照Mapper，查询每个来源最新的一条记录。
    @Autowired
    DataSourceSnapshotMapper dataSourceSnapshotMapper;
    // missionMapper：任务统计Mapper，查询任务总数和运行中数量。
    @Autowired
    MissionMapper missionMapper;
    // planningRunMapper：规划统计Mapper，查询总数和最新规划ID。
    @Autowired
    PlanningRunMapper planningRunMapper;
    // restrictionZoneMapper：限制区统计Mapper，查询限制区总数。
    @Autowired
    RestrictionZoneMapper restrictionZoneMapper;
    // 优先使用Java配置项，也可以在IDEA运行配置中设置对应环境变量。
    // deepseekApiKey：Java侧读取的模型密钥，仅用于判断是否已配置，不写入响应。
    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String deepseekApiKey;
    // deepseekModel：工作台展示的模型名称，来自配置项或环境变量。
    @Value("${deepseek.model:${DEEPSEEK_MODEL:deepseek-flash}}")
    private String deepseekModel;

    /**
     * 汇总各表统计、每个来源最新快照以及AI配置，返回工作台VO。
     */
    public DashboardVO getDashboard() {
        // dashboardVO：最终返回前端的工作台汇总对象。
        DashboardVO dashboardVO = new DashboardVO();
        // 1. 查询统计数据：count空表返回0，max(id)空表返回null。
        dashboardVO.setMissions(missionMapper.countAll());
        dashboardVO.setActiveMissions(missionMapper.countRunning());
        dashboardVO.setPlanningRuns(planningRunMapper.countAll());
        dashboardVO.setRestrictionZones(restrictionZoneMapper.countAll());
        dashboardVO.setLatestPlanId(planningRunMapper.selectLatestPlanId());
        // 2. Mapper已经选出每种数据源的最新快照，这里转换成前端需要的字段。
        // dataSourceSnapshots：Mapper选出的每种数据源最新快照列表。
        List<DataSourceSnapshot> dataSourceSnapshots = dataSourceSnapshotMapper.selectLatestBySourceKey();
        // dataSources：用于前端展示的数据源状态VO列表。
        List<SourceMetaVO> dataSources = new ArrayList<>();
        for (/* snapshot：当前循环处理的一条数据源数据库快照。 */ DataSourceSnapshot snapshot : dataSourceSnapshots) {
            // source：由当前快照转换出的数据源状态VO。
            SourceMetaVO source = new SourceMetaVO();
            source.setKey(snapshot.getSourceKey());
            source.setName(snapshot.getSourceName());
            source.setAvailable(snapshot.getAvailable());
            source.setIsHistoricalCache(snapshot.getIsCache());
            source.setFetchedAt(formatTime(snapshot.getFetchedAt()));
            source.setObservedAt(formatTime(snapshot.getObservedAt()));
            source.setRecordCount(snapshot.getRecordCount());
            source.setError(snapshot.getError());
            dataSources.add(source);
        }
        dashboardVO.setDataSources(dataSources);
        // 3. 与原工作台约定一致：available只代表已配置密钥，不代表连接成功。
        // configured：是否配置了非空模型密钥，不代表模型连接成功。
        boolean configured = deepseekApiKey != null && !deepseekApiKey.isBlank();
        // ai：工作台展示的AI配置摘要，不包含密钥。
        AiSummaryVO ai = new AiSummaryVO();
        ai.setAvailable(configured);
        ai.setConfigured(configured);
        ai.setConnectionVerified(false);
        ai.setModel(deepseekModel);
        dashboardVO.setAi(ai);
        // 4. 记录本次汇总时间并返回；密钥不放进响应对象。
        dashboardVO.setComputedAt(OffsetDateTime.now(ZoneOffset.UTC).toString());
        return dashboardVO;
    }

    /** 数据库时间转ISO字符串；observed_at可能为空，不能直接调用toString。 */

    /*
     * 三元表达式：条件 ? 值A : 值B表示条件成立取A，否则取B。
     * 这里只计算被选中的分支，常用于处理可空字段或选择响应结构。
     */

    /**
     * 把可空数据库时间格式化为ISO字符串。
     *
     * @param time 待格式化的可空数据库时间
     */
    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
