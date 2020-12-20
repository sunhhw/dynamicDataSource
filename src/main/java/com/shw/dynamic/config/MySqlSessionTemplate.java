package com.shw.dynamic.config;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.getSqlSession;
import static org.mybatis.spring.SqlSessionUtils.isSqlSessionTransactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;


/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 13:02
 * @description
 */
    public class MySqlSessionTemplate extends SqlSessionTemplate {
        private final SqlSessionFactory sqlSessionFactory;
        private final ExecutorType executorType;
        private final SqlSession sqlSessionProxy;
        private final PersistenceExceptionTranslator exceptionTranslator;
        private Map<Object, SqlSessionFactory> targetSqlSessionFactories;
        private SqlSessionFactory defaultTargetSqlSessionFactory;

        /**
         * 通过Map传入
         * @param targetSqlSessionFactories
         */
        public void setTargetSqlSessionFactories(Map<Object, SqlSessionFactory> targetSqlSessionFactories) {
            this.targetSqlSessionFactories = targetSqlSessionFactories;
        }
        public void setDefaultTargetSqlSessionFactory(SqlSessionFactory defaultTargetSqlSessionFactory) {
            this.defaultTargetSqlSessionFactory = defaultTargetSqlSessionFactory;
        }
        public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
            this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
        }
        public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
            this(sqlSessionFactory, executorType, new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration()
                    .getEnvironment().getDataSource(), true));
        }
        public MySqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
                                    PersistenceExceptionTranslator exceptionTranslator) {
            super(sqlSessionFactory, executorType, exceptionTranslator);
            this.sqlSessionFactory = sqlSessionFactory;
            this.executorType = executorType;
            this.exceptionTranslator = exceptionTranslator;
            this.sqlSessionProxy = (SqlSession) newProxyInstance(
                    SqlSessionFactory.class.getClassLoader(),
                    new Class[] { SqlSession.class },
                    new SqlSessionInterceptor());
            this.defaultTargetSqlSessionFactory = sqlSessionFactory;
        }
        //通过DataSourceContextHolder获取当前的会话工厂
        @Override
        public SqlSessionFactory getSqlSessionFactory() {
            String dataSourceKey = RoutingDataSourceContext.getDataSourceRoutingKey();
            SqlSessionFactory targetSqlSessionFactory = targetSqlSessionFactories.get(dataSourceKey);
            if (targetSqlSessionFactory != null) {
                return targetSqlSessionFactory;
            } else if (defaultTargetSqlSessionFactory != null) {
                return defaultTargetSqlSessionFactory;
            } else {
                Assert.notNull(targetSqlSessionFactories, "Property 'targetSqlSessionFactories' or 'defaultTargetSqlSessionFactory' are required");
                Assert.notNull(defaultTargetSqlSessionFactory, "Property 'defaultTargetSqlSessionFactory' or 'targetSqlSessionFactories' are required");
            }
            return this.sqlSessionFactory;
        }


        @Override
        public Configuration getConfiguration() {
            return this.getSqlSessionFactory().getConfiguration();
        }
        @Override
        public ExecutorType getExecutorType() {
            return this.executorType;
        }
        @Override
        public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
            return this.exceptionTranslator;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T selectOne(String statement) {
            return this.sqlSessionProxy.<T> selectOne(statement);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T selectOne(String statement, Object parameter) {
            return this.sqlSessionProxy.<T> selectOne(statement, parameter);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
            return this.sqlSessionProxy.<K, V> selectMap(statement, mapKey);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
            return this.sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds) {
            return this.sqlSessionProxy.<K, V> selectMap(statement, parameter, mapKey, rowBounds);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <E> List<E> selectList(String statement) {
            return this.sqlSessionProxy.<E> selectList(statement);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <E> List<E> selectList(String statement, Object parameter) {
            return this.sqlSessionProxy.<E> selectList(statement, parameter);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
            return this.sqlSessionProxy.<E> selectList(statement, parameter, rowBounds);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void select(String statement, ResultHandler handler) {
            this.sqlSessionProxy.select(statement, handler);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void select(String statement, Object parameter, ResultHandler handler) {
            this.sqlSessionProxy.select(statement, parameter, handler);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
            this.sqlSessionProxy.select(statement, parameter, rowBounds, handler);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int insert(String statement) {
            return this.sqlSessionProxy.insert(statement);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int insert(String statement, Object parameter) {
            return this.sqlSessionProxy.insert(statement, parameter);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int update(String statement) {
            return this.sqlSessionProxy.update(statement);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int update(String statement, Object parameter) {
            return this.sqlSessionProxy.update(statement, parameter);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int delete(String statement) {
            return this.sqlSessionProxy.delete(statement);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int delete(String statement, Object parameter) {
            return this.sqlSessionProxy.delete(statement, parameter);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T getMapper(Class<T> type) {
            return getConfiguration().getMapper(type, this);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void commit() {
            throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void commit(boolean force) {
            throw new UnsupportedOperationException("Manual commit is not allowed over a Spring managed SqlSession");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void rollback() {
            throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void rollback(boolean force) {
            throw new UnsupportedOperationException("Manual rollback is not allowed over a Spring managed SqlSession");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            throw new UnsupportedOperationException("Manual close is not allowed over a Spring managed SqlSession");
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void clearCache() {
            this.sqlSessionProxy.clearCache();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Connection getConnection() {
            return this.sqlSessionProxy.getConnection();
        }
        /**
         * {@inheritDoc}
         * @since 1.0.2
         */
        @Override
        public List<BatchResult> flushStatements() {
            return this.sqlSessionProxy.flushStatements();
        }
        /**
         * Proxy needed to route MyBatis method calls to the proper SqlSession got from Spring's Transaction Manager It also
         * unwraps exceptions thrown by {@code Method#invoke(Object, Object...)} to pass a {@code PersistenceException} to
         * the {@code PersistenceExceptionTranslator}.
         */
        private class SqlSessionInterceptor implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final SqlSession sqlSession = getSqlSession(
                        MySqlSessionTemplate.this.getSqlSessionFactory(),
                        MySqlSessionTemplate.this.executorType,
                        MySqlSessionTemplate.this.exceptionTranslator);
                try {
                    Object result = method.invoke(sqlSession, args);
                    if (!isSqlSessionTransactional(sqlSession, MySqlSessionTemplate.this.getSqlSessionFactory())) {
                        sqlSession.commit(true);
                    }
                    return result;
                } catch (Throwable t) {
                    Throwable unwrapped = unwrapThrowable(t);
                    if (MySqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
                        Throwable translated = MySqlSessionTemplate.this.exceptionTranslator
                                .translateExceptionIfPossible((PersistenceException) unwrapped);
                        if (translated != null) {
                            unwrapped = translated;
                        }
                    }
                    throw unwrapped;
                } finally {
                    closeSqlSession(sqlSession, MySqlSessionTemplate.this.getSqlSessionFactory());
                }
            }
        }
    }