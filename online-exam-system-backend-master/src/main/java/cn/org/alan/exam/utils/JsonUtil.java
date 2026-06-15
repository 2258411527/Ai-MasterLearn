package cn.org.alan.exam.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类 - 基于FastJSON
 */
@Slf4j
public class JsonUtil {

    /**
     * 将JSON字符串解析为List<Map<String, Object>>
     *
     * @param jsonString JSON字符串
     * @return List<Map<String, Object>>对象
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseList(String jsonString) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return null;
            }
            return (List<Map<String, Object>>) (Object) JSON.parseArray(jsonString, Map.class);
        } catch (Exception e) {
            log.error("解析JSON列表失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串解析为List（兼容旧版本）
     *
     * @param jsonString JSON字符串
     * @param clazz      元素类型（未使用，仅为兼容）
     * @return List对象
     * @deprecated 请使用 parseList(String jsonString)
     */
    @Deprecated
    public static <T> List<T> parseList(String jsonString, Class<T> clazz) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return null;
            }
            return (List<T>) (Object) parseList(jsonString);
        } catch (Exception e) {
            log.error("解析JSON列表失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串解析为Map
     *
     * @param jsonString JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> parseMap(String jsonString) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return null;
            }
            return JSON.parseObject(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析JSON对象失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJson(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return JSON.toJSONString(object);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串解析为指定类型的对象
     *
     * @param jsonString JSON字符串
     * @param clazz      目标类型
     * @return 对象
     */
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return null;
            }
            return JSON.parseObject(jsonString, clazz);
        } catch (Exception e) {
            log.error("解析JSON对象失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
