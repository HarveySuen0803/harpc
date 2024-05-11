package com.harvey.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author Harvey Suen
 */
public class ConfigUtil {
    /**
     * 加载配置文件, 将以 prefix 开头配置项封装成指定的 clazz 类型
     */
    public static <T> T loadConfig(Class<T> clazz, String prefix) {
        return loadConfig(clazz, prefix, "");
    }
    
    /**
     * 加载配置文件, 将以 prefix 开头配置项封装成指定的 clazz 类型
     */
    public static <T> T loadConfig(Class<T> clazz, String prefix, String env) {
        // 根据 env 拼接一个配置文件名 (eg: application.properties, application-dev.properties)
        StringBuilder configFileNameBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(env)) {
            configFileNameBuilder.append("-").append(env);
        }
        configFileNameBuilder.append(".properties");
        
        String configFileName = configFileNameBuilder.toString();
        Props props = new Props(configFileName);
        return props.toBean(clazz, prefix);
    }
}
