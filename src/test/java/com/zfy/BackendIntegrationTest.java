package com.zfy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import static com.zfy.service.JsonSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/** 显式启用才运行：新建随机测试数据库，启动独立计算进程；最终只删除本测试创建的库。 */
@EnabledIfEnvironmentVariable(named="RUN_BACKEND_INTEGRATION", matches="true")
class BackendIntegrationTest {
    private final HttpClient http=HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(3)).build();
    private String base;
    private final Set<String> covered=new HashSet<>();
    private Object request(String method,String path,Object body,int expected) throws Exception {
        var builder=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(190));
        if(method.equals("POST")) builder.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json(body==null?obj():body)));
        else builder.GET();
        var response=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(expected,response.statusCode(),method+" "+path+" -> "+response.body());
        covered.add(method+" "+path.split("\\?")[0].replaceAll("/\\d+", "/{id}"));
        return JSON.readValue(response.body(),Object.class);
    }
    private Object get(String path) throws Exception { return request("GET",path,null,200); }
    private Map<String,Object> post(String path,Object body) throws Exception { return map(request("POST",path,body,200)); }

    @Test
    void allRoutesAndMysqlBusinessChain() throws Exception {
        Path project=Path.of("").toAbsolutePath();
        Path root=project.getParent().getParent();
        var yaml=new YamlPropertiesFactoryBean(); yaml.setResources(new FileSystemResource(project.resolve("src/main/resources/application.yaml")));
        var props=yaml.getObject();
        String original=props.getProperty("spring.datasource.url");
        String user=props.getProperty("spring.datasource.username");
        String password=props.getProperty("spring.datasource.password");
        // 测试使用当前本地连接配置，不在日志打印凭据。
        assertTrue(original.startsWith("jdbc:mysql://127.0.0.1:") || original.startsWith("jdbc:mysql://localhost:"));
        int slash=original.indexOf('/',"jdbc:mysql://".length());
        String server=original.substring(0,slash+1);
        String options=original.contains("?")?original.substring(original.indexOf('?')):"";
        String database="sky_brain_codex_test_"+UUID.randomUUID().toString().replace("-", "").substring(0,12);
        Process worker=null;
        try(var admin=DriverManager.getConnection(server+options,user,password)) {
            admin.createStatement().execute("create database `"+database+"` character set utf8mb4");
            try {
                String jdbc=server+database+options;
                try(var connection=DriverManager.getConnection(jdbc,user,password)) {
                    String sql=Files.readString(project.resolve("src/main/resources/db/schema-mysql.sql"));
                    for(String statement:sql.split(";")) {
                        String s=statement.trim();
                        if(s.isBlank() || s.startsWith("CREATE DATABASE") || s.startsWith("USE ")) continue;
                        connection.createStatement().execute(s);
                    }
                }
                int port;
                try(var socket=new ServerSocket(0,0,InetAddress.getLoopbackAddress())) { port=socket.getLocalPort(); }
                var command=new ProcessBuilder(root.resolve(".venv/Scripts/python.exe").toString(),"-m","uvicorn","app.compute:app","--app-dir","backend","--host","127.0.0.1","--port",String.valueOf(port));
                command.directory(root.toFile()); command.environment().put("DEEPSEEK_API_KEY",""); command.environment().put("PYTHONIOENCODING","utf-8");
                command.redirectErrorStream(true).redirectOutput(root.resolve("GPT/reports/java-backend-smoke-worker.log").toFile());
                worker=command.start();
                String compute="http://127.0.0.1:"+port;
                boolean ready=false;
                for(int i=0;i<100;i++) {
                    try { ready=http.send(HttpRequest.newBuilder(URI.create(compute+"/internal/health")).timeout(Duration.ofSeconds(1)).build(),HttpResponse.BodyHandlers.discarding()).statusCode()==200; }
                    catch(Exception ignored) {}
                    if(ready) break;
                    Thread.sleep(200);
                }
                assertTrue(ready,"计算进程未启动，查看GPT/reports/java-backend-smoke-worker.log");
                try(var context=new SpringApplicationBuilder(SkyBrainApplication.class).run(
                        "--server.port=0","--spring.datasource.url="+jdbc,"--python.compute-url="+compute,"--deepseek.api-key=","--logging.level.com.zfy=WARN")) {
                    base="http://127.0.0.1:"+((WebServerApplicationContext)context).getWebServer().getPort();
                    runChain();
                    try(var connection=DriverManager.getConnection(jdbc,user,password)) {
                        var results=connection.createStatement().executeQuery("select count(*) from operation_events where kind='mission_completed'");results.next();assertEquals(1,results.getInt(1));
                        results=connection.createStatement().executeQuery("select count(*) from missions where status='completed'");results.next();assertEquals(1,results.getInt(1));
                        results=connection.createStatement().executeQuery("select count(*) from planning_drafts where status='applied'");results.next();assertEquals(1,results.getInt(1));
                        results=connection.createStatement().executeQuery("select count(*) from operation_events where kind='ai_operation_failed'");results.next();assertTrue(results.getInt(1)>=3);
                    }
                    assertEquals(36,covered.size(),covered.toString());
                    Files.writeString(root.resolve("GPT/reports/Java后端集成验证.json"),json(obj("routes",covered.stream().sorted().toList(),"route_count",covered.size(),"database","isolated temporary MySQL","llm","disabled; unavailable branches tested","business_chain","passed","production_database_modified",false)));
                }
            } finally {
                if(worker!=null) { worker.destroy(); if(!worker.waitFor(5,java.util.concurrent.TimeUnit.SECONDS)) worker.destroyForcibly(); }
                assertTrue(database.matches("sky_brain_codex_test_[a-f0-9]{12}"));
                admin.createStatement().execute("drop database `"+database+"`");
            }
        }
    }

    private void runChain() throws Exception {
        var cors=http.send(HttpRequest.newBuilder(URI.create(base+"/api/plan")).method("OPTIONS",HttpRequest.BodyPublishers.noBody())
                .header("Origin","http://localhost:5173").header("Access-Control-Request-Method","POST")
                .header("Access-Control-Request-Headers","content-type").build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,cors.statusCode());assertEquals("http://localhost:5173",cors.headers().firstValue("access-control-allow-origin").orElse(null));
        get("/api/health"); var empty=map(get("/api/dashboard"));assertEquals(0,number(empty.get("missions")));
        get("/api/environment?data_mode=simulation");
        var polygon=obj("type","Polygon","coordinates",List.of(List.of(List.of(114.0,22.50),List.of(114.001,22.50),List.of(114.001,22.501),List.of(114.0,22.501),List.of(114.0,22.50))));
        post("/api/restrictions/import",obj("name","测试区域","source_name","隔离测试","geojson",polygon));get("/api/restrictions");
        request("POST","/api/restrictions/import",obj("name","无效区域","source_name","隔离测试","geojson",polygon,"min_altitude_m",300,"max_altitude_m",100),422);
        var planInput=obj("start",obj("longitude",114.0528,"latitude",22.5389,"altitude_m",150),"end",obj("longitude",114.0786,"latitude",22.5628,"altitude_m",150),"grid_size",24,"data_mode","simulation");
        var plan=post("/api/plan",planInput);long pid=number(plan.get("plan_id"));get("/api/plans/"+pid);
        var mission=post("/api/missions",obj("name","测试任务","task_type","inspection","plan_id",pid,"route_key",plan.get("recommended_route_key")));long mid=number(mission.get("id"));get("/api/missions");
        var sim=post("/api/missions/"+mid+"/simulate",null);
        post("/api/missions/"+mid+"/state",obj("status","paused"));post("/api/missions/"+mid+"/state",obj("status","running"));
        var points=List.of(obj("t_s",0,"longitude",114.0528,"latitude",22.5389,"altitude_m",150,"battery_percent",86),obj("t_s",10,"longitude",114.0538,"latitude",22.5399,"altitude_m",150,"battery_percent",85),obj("t_s",20,"longitude",114.0548,"latitude",22.5409,"altitude_m",150,"battery_percent",84));
        post("/api/missions/"+mid+"/telemetry",obj("source","SITL","points",points));
        var done=post("/api/missions/"+mid+"/complete-simulation",null);var repeat=post("/api/missions/"+mid+"/complete-simulation",null);assertEquals(done.get("log_id"),repeat.get("log_id"));
        request("POST","/api/missions/"+mid+"/state",obj("status","running"),409);
        var log=post("/api/replay/import",obj("name","外部日志","source_name","隔离测试","points",points));get("/api/replay");get("/api/replay/"+log.get("id"));
        post("/api/replan",obj("plan_id",pid,"geojson",polygon));
        var draft=post("/api/ai/planning/draft",obj("message","优先安全","request",planInput,"mode","offline"));get("/api/ai/planning/drafts");
        var adopted=post("/api/ai/planning/drafts/"+draft.get("draft_id")+"/apply",null);var adoptedAgain=post("/api/ai/planning/drafts/"+draft.get("draft_id")+"/apply",null);
        assertEquals(map(adopted.get("plan")).get("plan_id"),map(adoptedAgain.get("plan")).get("plan_id"));
        get("/api/operations");get("/api/knowledge/rules");get("/api/ai/status");post("/api/ai/command",obj("message","查看当前任务","plan_id",pid));
        post("/api/evaluation/run",planInput);post("/api/evaluation/batch",obj("count",2));
        post("/api/dispatch/optimize",obj("drones",List.of(obj("id","D1","name","测试机","battery_percent",86,"battery_capacity_wh",600,"energy_wh_per_km",24,"cruise_speed_mps",12)),"jobs",List.of(obj("id","J1","name","测试任务","distance_m",1000))));
        var samples=new ArrayList<Map<String,Object>>();for(int i=1;i<=8;i++)samples.add(obj("flight_id","F"+i,"effective_distance_km",i,"measured_energy_wh",25*i));
        post("/api/energy/calibrate",obj("source_name","隔离测试","samples",samples));
        request("POST","/api/inspection/analyze",obj("image_data_url","invalid"),422);
        get("/api/datasets/catalog");get("/api/datasets/flights/1");var archive=post("/api/datasets/flights/1/archive",null);var archive2=post("/api/datasets/flights/1/archive",null);assertEquals(archive.get("id"),archive2.get("id"));
        request("POST","/api/ai/advanced/fleet",obj("plan_id",pid),422);
        request("POST","/api/ai/advanced/fleet/999999/apply",null,404);
        request("POST","/api/ai/advanced/diagnosis",obj("source","archive","record_id",log.get("id")),422);
        get("/api/ai/advanced/history");
        // 重规划无解仍应保存风险区、暂停执行任务、记失败事件，而不是整体回滚。
        var second=post("/api/missions",obj("name","风险处置测试","task_type","inspection","plan_id",pid,"route_key",plan.get("recommended_route_key")));
        long secondId=number(second.get("id"));post("/api/missions/"+secondId+"/simulate",null);
        var blocked=obj("type","Polygon","coordinates",List.of(List.of(List.of(113.9,22.4),List.of(114.2,22.4),List.of(114.2,22.7),List.of(113.9,22.7),List.of(113.9,22.4))));
        request("POST","/api/replan",obj("plan_id",pid,"mission_id",secondId,"current_position",planInput.get("start"),"battery_percent",80,"remaining_waypoints",List.of(),"event_name","重规划失败风险区","geojson",blocked),409);
        var paused=rows(get("/api/missions")).stream().filter(r->number(r.get("id"))==secondId).findFirst().orElseThrow();
        assertEquals("paused",paused.get("status"));
        assertTrue(rows(get("/api/restrictions")).stream().anyMatch(r->"重规划失败风险区".equals(r.get("name"))));
        assertTrue(rows(get("/api/operations")).stream().anyMatch(r->"replan_failed".equals(r.get("kind"))));
    }
}
