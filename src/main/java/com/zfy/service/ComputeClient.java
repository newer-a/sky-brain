package com.zfy.service;

import com.zfy.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.io.IOException;
import java.util.*;
import static com.zfy.service.JsonSupport.*;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
 */

/*
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * @Value读取配置占位符；${配置名:默认值}在配置缺失时采用默认值，嵌套占位符可继续尝试环境变量。
 */

/**
 * Java访问Python计算服务的HTTP客户端；将网络/协议错误转换为业务异常。
 */
@Component
public class ComputeClient {
    // base：Python计算服务基础URL，默认http://127.0.0.1:8005。
    private final String base;
    // Uvicorn提供HTTP/1.1；避免h2c升级导致POST请求体解析异常。
    // client：复用的JDK HTTP客户端，使用HTTP/1.1和5秒连接超时。
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(5)).build();

    /*
     * replaceAll接收正则表达式：/+$表示字符串末尾连续的斜杠，将它们替换为空，避免拼URL时出现双斜杠。
     * HttpClient.newBuilder().version(...).connectTimeout(...).build()是构建器链，逐项配置后生成客户端。
     */

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param base Python计算服务基础URL，默认http://127.0.0.1:8005
     */
    public ComputeClient(@Value("${python.compute-url:http://127.0.0.1:8005}") String base) {
        this.base=base.replaceAll("/+$", "");
    }

    /*
     * Builder链式调用：newBuilder创建构建器，timeout/header/POST设置同一个请求，build才生成HttpRequest。
     * BodyPublishers.ofString提供请求体，BodyHandlers.ofString指定把响应体读取为字符串。
     * 捕获InterruptedException后重新调用interrupt恢复中断标记，让上层知道线程被要求中断。
     */

    /**
     * 发送一次JSON计算请求，验证响应结构并转换HTTP、超时和连接异常。
     *
     * @param operation 计算操作名称，如plan、simulate或inspection，用于选择内部计算接口
     * @param payload 计算请求内容，为null时发送空JSON对象
     * @param context 提供给计算服务的业务快照，键为表名，值为该表记录列表
     * @param id 计算关联的业务记录ID；不需要时传null
     */
    public Map<String,Object> call(String operation, Object payload, Map<String,Object> context, Long id) {
        // request：已设置URL、超时、JSON头和请求体的HTTP POST请求。
        var request=HttpRequest.newBuilder(URI.create(base+"/internal/compute/"+operation))
        .timeout(Duration.ofSeconds(180)).header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json(obj("payload",payload==null?obj():payload,"context",context,"id",
            id)))).build();
        try {
            // response：计算服务返回的HTTP响应，含状态码与原始响应体。
            var response=client.send(request, HttpResponse.BodyHandlers.ofString());
            // body：解析后的响应JSON对象，成功时应含data字段。
            Map<String,Object> body;
            try {
                body=JSON.readValue(response.body(), Map.class);
            }
            catch(/* e：当前操作抛出的运行时异常。 */ RuntimeException e) {
                throw new BusinessException(502,"计算服务返回无效JSON");
            }
            if(body==null) throw new BusinessException(502,"计算服务返回空响应");
            if(response.statusCode()!=200) throw new BusinessException(response.statusCode()>=400&&response.statusCode()<500?response.statusCode():502,
                String.valueOf(body.getOrDefault("detail","计算服务执行失败")));
            if(!body.containsKey("data")) throw new BusinessException(502,"计算服务响应缺少data");
            return body;
        } catch(/* e：等待计算服务响应超过超时时间的异常。 */ HttpTimeoutException e) {
            throw new BusinessException(504,"计算服务超时");
        }
        catch(/* e：连接或读取计算服务时的网络异常。 */ IOException e) {
            throw new BusinessException(503,"无法连接计算服务，请启动Python app.compute（默认8005端口）");
        }
        catch(/* e：当前线程等待HTTP响应时被中断的异常。 */ InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(503,"计算已中断");
        }
    }
}
