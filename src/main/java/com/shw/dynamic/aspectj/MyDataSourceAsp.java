package com.shw.dynamic.aspectj;

import com.shw.dynamic.annotation.MyDataSource;
import com.shw.dynamic.config.RoutingDataSourceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 17:22
 * @description
 * 这个Order(1)值得是AOP的执行优先级 与事务冲突时使用
 */
@Aspect
@Component
@Order(1)
public class MyDataSourceAsp {



    /**
     * 扫描所有与这个注解有关的
     * ：@within：用于匹配所有持有指定注解类型内的方法和类；
     * 也就是说只要有一个类上的有这个,使用@within这个注解，就能拿到下面所有的方法
     *：@annotation：用于匹配当前执行方法持有指定注解的方法，而这个注解只针对方法
     *
     * 不添加扫描路径，应该是根据启动类的扫描范围执行的
     */
    @Pointcut("@annotation(com.shw.dynamic.annotation.MyDataSource) " +
            "|| @within(com.shw.dynamic.annotation.MyDataSource)")
    public void doPointCut() {
    }

    @Around("doPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {


        MyDataSource dataSource = getDataSource(joinPoint);

        if (dataSource != null) {
            RoutingDataSourceContext.setDataSourceRoutingKey(dataSource.value().name());
        }

        try {
            // 继续执行
            return joinPoint.proceed();
        } finally {
            //关闭线程资源 在执行方法之后
            RoutingDataSourceContext.close();
        }

    }

    /**
     * 获取类或者方法上的注解
     * 先获取方法上的注解，然后在获取类上的注解，这就实现了方法上数据源切换优先于类上的
     * @param joinPoint 正在执行的连接点
     * @return 注解
     */
    private MyDataSource getDataSource(ProceedingJoinPoint joinPoint) {
        MethodSignature method = (MethodSignature) joinPoint.getSignature();
        // 获取方法上的注解
        MyDataSource annotation = method.getMethod().getAnnotation(MyDataSource.class);
        if (annotation != null) {
            return annotation;
        } else {
            // 获取到这个注解上的类
            Class<?> aClass = joinPoint.getTarget().getClass();
            // 获取到这个类上的注解
            MyDataSource dataSource = aClass.getAnnotation(MyDataSource.class);
            // 返回类上的注解
            return dataSource;
        }

    }

}
