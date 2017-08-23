package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by yao on 2017/8/21.
 */
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);
    private static Properties p = null;
    static{
        p = new Properties();
        try {
            p.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream("mmall.properties"),"UTF-8"));
        } catch (IOException e) {
            logger.error("mmall.properties配置文件读取异常",e);
        }
    }

    public static String getParam(String key){
        String result = p.getProperty(key);
        if(StringUtils.isBlank(result)){
            return null;
        }
        return result.trim();
    }

    public static String getDefaultParam(String key,String defaultValue){
        String result = p.getProperty(key);
        if(StringUtils.isBlank(result)){
            return defaultValue.trim();
        }
        return result.trim();
    }
}
