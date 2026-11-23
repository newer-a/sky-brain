package com.zfy.controller;

import com.sun.net.httpserver.HttpServer;
import com.zfy.service.*;
import java.util.Map;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Java HTTP入口到本地上游测试服务器；不连接MySQL或真实外部数据源。 */
class EnvironmentEndpointTest {
    private HttpServer upstream;
    private MockMvc mvc;
    private volatile String queryString;
    private volatile String body;
    private volatile int upstreamStatus;
    private final AtomicInteger calls = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        // 原Python隔离执行的simulation响应，用来核验嵌套字段兼容性。
        try (var input = getClass().getResourceAsStream("/environment-simulation.json")) {
            assertNotNull(input);
            body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        upstreamStatus = 200;
        upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstream.createContext("/internal/compute/environment", exchange -> {
            calls.incrementAndGet();
            var input = JsonSupport.JSON.readValue(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), Map.class);
            var params = JsonSupport.map(input.get("payload"));
            queryString = "west="+params.get("west")+"&south="+params.get("south")+"&east="+params.get("east")+"&north="+params.get("north")+"&data_mode="+params.get("data_mode");
            byte[] bytes = ("{\"data\":"+body+",\"sources\":[],\"artifacts\":{}}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(upstreamStatus, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        upstream.start();
        var controller = new CheckController();
        var store = new BusinessStore(null,
                new ComputeClient("http://127.0.0.1:" + upstream.getAddress().getPort()),
                new DataSourceTransactionManager(), null) {
            @Override public Map<String,Object> context(String... tables) { return JsonSupport.obj(); }
            @Override public Map<String,Object> contextForMission(long missionId) {
                throw new UnsupportedOperationException("环境查询测试不提供任务上下文");
            }
        };
        controller.environmentService = new EnvironmentService(new PlanningService(store));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CheckExceptionHandler()).build();
    }

    @AfterEach
    void tearDown() {
        if (upstream != null) upstream.stop(0);
    }

    @Test
    void defaultParametersAndHybridResponseArePreserved() throws Exception {
        // 模拟上游auto模式返回hybrid；Java不自行更改数据模式。
        body = body.replace("\"data_mode_used\": \"simulation\"", "\"data_mode_used\": \"hybrid\"");
        mvc.perform(get("/api/environment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data_mode_used").value("hybrid"))
                .andExpect(jsonPath("$.bbox[0]").value(114.03));
        assertEquals("west=114.03&south=22.52&east=114.09&north=22.58&data_mode=auto", queryString);
    }

    @Test
    void simulationAndRealParametersReachUpstream() throws Exception {
        mvc.perform(get("/api/environment").param("data_mode", "simulation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data_mode_used").value("simulation"))
                .andExpect(jsonPath("$.buildings.length()").value(70))
                .andExpect(jsonPath("$.elevation_samples.length()").value(100))
                .andExpect(jsonPath("$.sources[0].is_simulation").value(true));
        assertTrue(queryString.endsWith("data_mode=simulation"));
        mvc.perform(get("/api/environment").param("west", "113").param("data_mode", "real"))
                .andExpect(status().isOk());
        assertTrue(queryString.startsWith("west=113.0&"));
        assertTrue(queryString.endsWith("data_mode=real"));
    }

    @Test
    void invalidParametersReturnDetailWithoutCallingUpstream() throws Exception {
        for (String[] pair : new String[][]{{"west","115"}, {"north","20"},
                {"west","NaN"}, {"east","Infinity"}, {"south","abc"}, {"data_mode","hybrid"}}) {
            mvc.perform(get("/api/environment").param(pair[0], pair[1]))
                    .andExpect(status().is(422)).andExpect(jsonPath("$.detail").isString());
        }
        assertEquals(0, calls.get());
    }

    @Test
    void upstreamErrorsDoNotBecomeSuccessfulEmptyEnvironments() throws Exception {
        upstreamStatus = 500;
        mvc.perform(get("/api/environment")).andExpect(status().is(502));
        upstreamStatus = 422;
        mvc.perform(get("/api/environment")).andExpect(status().is(422));
        upstreamStatus = 200;
        for (String invalid : new String[]{"not json", "null", "{}"}) {
            body = invalid;
            mvc.perform(get("/api/environment"))
                    .andExpect(status().is(502)).andExpect(jsonPath("$.detail").isString());
        }
    }

    @Test
    void unavailablePythonReturns503() throws Exception {
        upstream.stop(0);
        mvc.perform(get("/api/environment"))
                .andExpect(status().is(503)).andExpect(jsonPath("$.detail").isString());
    }
}
