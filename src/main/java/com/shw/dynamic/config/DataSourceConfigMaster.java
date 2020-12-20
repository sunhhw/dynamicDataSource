package com.shw.dynamic.config;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.shw.dynamic.enums.DataSourceType;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author : JCccc
 * @CreateTime : 2019/12/10
 * @Description :多数据源配置
 **/

@Configuration
@MapperScan(basePackages = DataSourceConfigMaster.BASE_PACKAGES, sqlSessionTemplateRef = "sqlSessionTemplate")
public class DataSourceConfigMaster {

    static final String BASE_PACKAGES = "com.shw.dynamic.mapper";

    private static final String MAPPER_LOCATION = "classpath:mybatis/mapper/*.xml";

    /***
     * 创建 DruidXADataSource mydbone 用@ConfigurationProperties 自动配置属性
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.master")
    public DataSource druidDataSourceMaster(DruidProperties properties) {
        DruidXADataSource druidXADataSource = new DruidXADataSource();
        return properties.dataSource(druidXADataSource);
    }

    /***
     * 创建 DruidXADataSource mydbtwo
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    public DataSource druidDataSourceSlave(DruidProperties properties) {
        DruidXADataSource druidXADataSource = new DruidXADataSource();
        return properties.dataSource(druidXADataSource);
    }

    /**
     * 创建支持 XA 事务的 Atomikos 数据源 mydbone
     */
    @Bean
    public DataSource dataSourceMaster(DataSource druidDataSourceMaster) {
        AtomikosDataSourceBean sourceBean = new AtomikosDataSourceBean();
        sourceBean.setXaDataSource((DruidXADataSource) druidDataSourceMaster);
        // 必须为数据源指定唯一标识
        sourceBean.setPoolSize(5);
        sourceBean.setTestQuery("SELECT 1");
        sourceBean.setUniqueResourceName("master");
        return sourceBean;
    }

    /**
     * 创建支持 XA 事务的 Atomikos 数据源 mydbtwo
     */
    @Bean
    public DataSource dataSourceSlave(DataSource druidDataSourceSlave) {
        AtomikosDataSourceBean sourceBean = new AtomikosDataSourceBean();
        sourceBean.setXaDataSource((DruidXADataSource) druidDataSourceSlave);
        sourceBean.setPoolSize(5);
        sourceBean.setTestQuery("SELECT 1");
        sourceBean.setUniqueResourceName("slave");
        return sourceBean;
    }


    /**
     * @param dataSourceMaster 数据源 mydbone
     * @return 数据源 mydbone 的会话工厂
     */
    @Bean
    public SqlSessionFactory sqlSessionFactoryMaster(DataSource dataSourceMaster)
            throws Exception {
        return createSqlSessionFactory(dataSourceMaster);
    }


    /**
     * @param dataSourceSlave 数据源 mydbtwo
     * @return 数据源 mydbtwo 的会话工厂
     */
    @Bean
    public SqlSessionFactory sqlSessionFactorySlave(DataSource dataSourceSlave)
            throws Exception {
        return createSqlSessionFactory(dataSourceSlave);
    }


    /***
     * sqlSessionTemplate 与 Spring 事务管理一起使用，以确保使用的实际 SqlSession 是与当前 Spring 事务关联的,
     * 此外它还管理会话生命周期，包括根据 Spring 事务配置根据需要关闭，提交或回滚会话
     * @param sqlSessionFactoryMaster 数据源 mydbone
     * @param sqlSessionFactorySlave 数据源 mydbtwo
     */
    @Bean
    public CustomSqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactoryMaster, SqlSessionFactory sqlSessionFactorySlave) {
        Map<Object, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();
        sqlSessionFactoryMap.put(DataSourceType.MASTER.name(), sqlSessionFactoryMaster);
        sqlSessionFactoryMap.put(DataSourceType.SLAVE.name(), sqlSessionFactorySlave);
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(sqlSessionFactoryMaster);
        customSqlSessionTemplate.setTargetSqlSessionFactories(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }

    /***
     * 自定义会话工厂
     * @param dataSource 数据源
     * @return :自定义的会话工厂
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //配置驼峰命名
        configuration.setMapUnderscoreToCamelCase(true);
        //配置sql日志
        configuration.setLogImpl(StdOutImpl.class);
        factoryBean.setConfiguration(configuration);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //配置读取mapper.xml路径
        factoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATION));
        return factoryBean.getObject();
    }
}