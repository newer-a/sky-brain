package com.zfy.controller;

import com.zfy.exception.EnvironmentException;
import com.zfy.exception.BusinessException;
import com.zfy.pojo.vo.ApiErrorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/*
 * @RestControllerAdvice作用于指定Controller包；@ExceptionHandler按异常类型选择处理方法。
 * ResponseEntity<ApiErrorVO>同时携带HTTP状态码和JSON响应体，不是只返回一个VO。
 */

/**
 * 统一处理com.zfy.controller包内接口的异常，保持detail错误响应格式。
 */
@RestControllerAdvice(basePackages = "com.zfy.controller")
// @Slf4j由Lombok生成log日志对象，用于在IDEA控制台记录数据库异常的具体原因。
@Slf4j
public class CheckExceptionHandler {

    /**
     * 返回业务异常携带的HTTP状态码和原因。
     *
     * @param exception Spring捕获并交给本方法处理的BusinessException异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorVO> handleBusiness(BusinessException exception) {
        return error(exception.getStatus(), exception.getMessage());
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 把DTO校验失败信息去重并合并，返回422。
     *
     * @param exception Spring捕获并交给本方法处理的MethodArgumentNotValidException异常
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorVO> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException exception) {
        return error(422, exception.getBindingResult().getAllErrors().stream()
            .map(/* e：校验结果中的一条字段或对象错误。 */ e -> e.getDefaultMessage()).distinct().collect(java.util.stream.Collectors.joining("；")));
    }

    /**
     * 把缺失或无法解析的JSON请求体转换为422响应。
     *
     * @param exception Spring捕获并交给本方法处理的HttpMessageNotReadableException异常
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorVO> handleJson(org.springframework.http.converter.HttpMessageNotReadableException exception) {
        return error(422, "请求体缺失或JSON格式/字段类型不正确");
    }

    /**
     * 把数据库并发冲突转换为409响应。
     *
     * @param exception Spring捕获并交给本方法处理的ConcurrencyFailureException异常
     */
    @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
    public ResponseEntity<ApiErrorVO> handleConflict(org.springframework.dao.ConcurrencyFailureException exception) {
        return error(409, "数据正被其他请求修改，请重新查询后重试");
    }

    /**
     * 把数据库约束冲突转换为409响应。
     *
     * @param exception Spring捕获并交给本方法处理的DataIntegrityViolationException异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorVO> handleIntegrity(org.springframework.dao.DataIntegrityViolationException exception) {
        return error(409, "数据不满足关联关系、长度或数据库约束");
    }

    /**
     * 把数据库访问失败转换为503响应。
     *
     * @param exception Spring捕获并交给本方法处理的DataAccessException异常
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiErrorVO> handleDatabase(org.springframework.dao.DataAccessException exception) {
        log.error("数据库访问失败", exception);
        return error(503, "数据库查询失败，请检查数据库连接和建表状态");
    }

    /**
     * 返回环境查询异常的状态码和说明。
     *
     * @param exception Spring捕获并交给本方法处理的EnvironmentException异常
     */
    @ExceptionHandler(EnvironmentException.class)
    public ResponseEntity<ApiErrorVO> handleEnvironment(EnvironmentException exception) {
        return error(exception.getStatus(), exception.getMessage());
    }

    /**
     * 把URL参数类型转换失败转换为422响应。
     *
     * @param exception Spring捕获并交给本方法处理的MethodArgumentTypeMismatchException异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorVO> handleParameter(MethodArgumentTypeMismatchException exception) {
        return error(422, "查询参数" + exception.getName() + "的类型不正确");
    }

    /**
     * 统一构造HTTP状态码与detail响应体。
     *
     * @param status 要返回的HTTP状态码，如422参数错误、409业务冲突
     * @param message 向接口调用方解释失败原因的文字
     */
    private ResponseEntity<ApiErrorVO> error(int status, String message) {
        // body：统一错误响应VO，detail字段保存失败原因。
        ApiErrorVO body = new ApiErrorVO();
        body.setDetail(message);
        return ResponseEntity.status(status).body(body);
    }
}
