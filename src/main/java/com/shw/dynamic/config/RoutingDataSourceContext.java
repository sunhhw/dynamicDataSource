package com.shw.dynamic.config;

import com.shw.dynamic.enums.DataSourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 13:02
 * @description
 */
public class RoutingDataSourceContext  {

    private static Logger logger = LoggerFactory.getLogger(RoutingDataSourceContext.class);
    /**
     * 使用ThreadLocal维护变量，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     *  所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    private static final ThreadLocal<String> THREAD_LOCAL_DATA_SOURCE_KEY = new ThreadLocal<>();

    /**
     * 得到数据源名称
     * @return
     */
    static String getDataSourceRoutingKey() {
        String key = THREAD_LOCAL_DATA_SOURCE_KEY.get();
        return key == null ? DataSourceType.MASTER.name() : key;
    }

    /**
     * 设置数据源
     * @param key
     */
    public static void setDataSourceRoutingKey(String key) {
        logger.info("切换到{}数据源",key);
        THREAD_LOCAL_DATA_SOURCE_KEY.set(key);
    }

    /**
     * 清空数据源设置
     */
    public static void close() {
        THREAD_LOCAL_DATA_SOURCE_KEY.remove();
    }

}
