package com.shw.dynamic.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;
import com.shw.dynamic.enums.DataSourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.servlet.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 12:48
 * @description
 */
@Configuration
public class DataSourceConfig {

    private Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    /**
     * 如果使用DataSourceBuilder返回的话，则需要把配置文件中的url改成jdbc-url;
     * 如果是DruidDataSourceBuilder则不用
     */
    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    public DataSource masterDataSource() {
        logger.info("create master datasource...");
        return DruidDataSourceBuilder.create().build();
    }

    @Bean("slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
    @ConditionalOnProperty(prefix = "spring.datasource.druid.slave", name = "enabled", havingValue = "true")
    public DataSource slaveDataSource() {
        logger.info("create slave datasource...");
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 在众多相同的Bean中，使用@Primary表示这个实例被优先注入
     * 枚举类的name方法就是这个枚举本身的字面量意思
     */
    @Bean
    @Primary
    public DataSource primaryDataSource(@Autowired @Qualifier("masterDataSource") DataSource masterDataSource,
                                        @Autowired DataSource slaveDataSource) {
        logger.info("create routing datasource...");
        Map<Object,Object> map = new HashMap<>(4);
        map.put(DataSourceType.MASTER.name(),masterDataSource);
        map.put(DataSourceType.SLAVE.name(),slaveDataSource);
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(map);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        return routingDataSource;
    }


    /**
     * 去除监控页面底部的广告
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.statViewServlet.enabled", havingValue = "true")
    public FilterRegistrationBean removeDruidFilterRegistrationBean(DruidStatProperties properties) {
        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // 提取common.js的配置路径
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        final String filePath = "support/http/resources/js/common.js";
        // 创建filter进行过滤
        Filter filter = new Filter() {
            @Override
            public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                chain.doFilter(request, response);
                // 重置缓冲区，响应头不会被重置
                response.resetBuffer();
                // 获取common.js
                String text = Utils.readFromResource(filePath);
                // 正则替换banner, 除去底部的广告信息
                text = text.replaceAll("<a.*?banner\"></a><br/>", "");
                text = text.replaceAll("powered.*?shrek.wang</a>", "");
                response.getWriter().write(text);
            }

            @Override
            public void destroy() {
            }
        };
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(commonJsPattern);
        return registrationBean;
    }

    //填充数据源属性
//    @Bean("slaveDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
//    public DataSource slaveDataSource(DruidProperties druidProperties) {
//        logger.info("create slave datasource...");
//        DruidDataSource build = DruidDataSourceBuilder.create().build();
//        DruidDataSource dataSource = druidProperties.dataSource(build);
//        return dataSource;
//    }

}
