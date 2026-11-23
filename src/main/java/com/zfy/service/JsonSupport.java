package com.zfy.service;

import tools.jackson.databind.json.JsonMapper;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;



/**
 * JSON、Map、数据库时间和HTTP响应之间的转换工具，不负责业务查询。
 */
public final class JsonSupport {
    // JSON：共享的Jackson JSON转换器，负责Java对象和JSON之间的转换。
    public static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * 私有构造器，阻止实例化这个静态工具类。
     */
    private JsonSupport() {
    }

    /*
     * Object... pairs是可变参数，调用obj("id", 1, "name", "任务")时会自动打包成Object数组。
     * 循环i+=2每次处理一对键和值；(String)pairs[i]是强制类型转换，不会把任意对象自动转成字符串。
     * 调用方必须保证数量为偶数且键为String，否则会发生越界或类型转换异常。
     */

    /**
     * 把交替出现的键和值组装为有序Map，供请求、响应或数据库字段使用。
     *
     * @param pairs 按键、值、键、值顺序传入的参数；数量必须为偶数，键必须为String
     */
    public static Map<String,Object> obj(Object... pairs) {
        // result：保持键插入顺序的Map，可保存null值。
        Map<String,Object> result = new LinkedHashMap<>();
        for (/* i：当前键在pairs数组中的下标，每次加2跳到下一对键值。 */ int i=0;i<pairs.length;i+=2) result.put((String)pairs[i], pairs[i+1]);
        return result;
    }

    /*
     * Map<?, ?>中的?是通配符，表示暂不限定键和值的具体类型。
     * Java泛型有类型擦除，运行时通常只能检查“是不是Map”，不能充分验证每个键值的泛型类型。
     * 强转为Map<String,Object>依赖当前JSON结构约定；@SuppressWarnings("unchecked")只压制编译警告，不增加校验。
     */

    /**
     * 将对象转换为字符串键的Map；已经是Map时直接返回同一引用。
     *
     * @param value 待转换的对象；若本来是Map则直接返回原引用
     */
    @SuppressWarnings("unchecked")
    public static Map<String,Object> map(Object value) {
        if(value instanceof Map<?,?>) return (Map<String,Object>)value;
        return JSON.convertValue(value, Map.class);
    }

    /*
     * 嵌套泛型List<Map<String,Object>>表示“每个元素都是字段Map的列表”。
     * 此处强制转换只适用于已经确认结构的数据；不是把任意对象逐条变成Map。
     * @SuppressWarnings("unchecked")不会避免结构错误引起的ClassCastException。
     */

    /**
     * 将已知结构的对象按Map列表读取，不逐项转换或校验。
     *
     * @param value 调用方确认结构为List&lt;Map&lt;String,Object&gt;&gt;的对象
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String,Object>> rows(Object value) {
        return (List<Map<String,Object>>)value;
    }

    /**
     * 把Java对象序列化为JSON字符串。
     *
     * @param value 需要序列化的Java对象
     */
    public static String json(Object value) {
        return JSON.writeValueAsString(value);
    }

    /**
     * 将JSON文本解析为Java对象，具体可能是Map、List或标量。
     *
     * @param value 包含合法JSON文本的非空对象，通常为数据库JSON字段字符串
     */
    public static Object parse(Object value) {
        return JSON.readValue(value.toString(), Object.class);
    }

    /**
     * 解析内容为JSON对象的字符串并返回Map。
     *
     * @param value 内容是JSON对象的数据库字段或字符串
     */
    public static Map<String,Object> objectJson(Object value) {
        return map(parse(value));
    }

    /**
     * 把Number类型的ID等数值统一转换为long。
     *
     * @param value 实际类型为Number的数值，如数据库ID
     */
    public static long number(Object value) {
        return ((Number)value).longValue();
    }

    /*
     * instanceof Number n是模式匹配：类型判断成功后，把已转换的Number绑定为变量n。
     * &&是短路运算，只有前面匹配成功才执行n.intValue()；||表示左侧为真时不用计算右侧。
     */

    /**
     * 兼容Boolean和数据库数字布尔值；true或非零数字视为真。
     *
     * @param value 待转换的布尔或数字值
     */
    public static boolean yes(Object value) {
        return Boolean.TRUE.equals(value) || value instanceof /* n：instanceof匹配成功后绑定的Number数值。 */ Number n && n.intValue()!=0;
    }

    /*
     * instanceof LocalDateTime t / Timestamp t同时完成类型判断与变量绑定。
     * 两个t属于各自if的有效范围，因此可以同名，不能在任意后续位置直接使用。
     * try先按带偏移量的格式解析；catch只处理这种格式解析失败，再尝试本地日期时间格式。
     */

    /**
     * 兼容数据库时间和ISO文本；带时区文本转换到UTC后去掉偏移信息。
     *
     * @param value 待转换的时间对象或非空时间字符串
     */
    public static LocalDateTime time(Object value) {
        if(value instanceof /* t：instanceof匹配到的时间对象；每个if中的t只在各自匹配范围内有效。 */ LocalDateTime t) return t;
        if(value instanceof /* t：instanceof匹配到的时间对象；每个if中的t只在各自匹配范围内有效。 */ Timestamp t) return t.toLocalDateTime();
        // s：把日期和时间之间的空格替换为T后的文本。
        String s=value.toString().replace(' ', 'T');
        try {
            return OffsetDateTime.parse(s).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }
        catch (/* ignored：按带时区格式解析失败的异常；转而尝试不带时区格式。 */ java.time.format.DateTimeParseException ignored) {
            return LocalDateTime.parse(s);
        }
    }

    /*
     * 三元表达式：条件 ? 值A : 值B表示条件成立取A，否则取B。
     * 这里只计算被选中的分支，常用于处理可空字段或选择响应结构。
     */

    /**
     * 把可空时间转换为ISO文本。
     *
     * @param value 待输出为ISO字符串的可空时间
     */
    public static String iso(Object value) {
        return value==null?null:time(value).toString();
    }

    /**
     * 返回当前UTC时间，使用不含时区字段的LocalDateTime。
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
