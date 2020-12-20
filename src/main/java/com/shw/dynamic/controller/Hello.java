package com.shw.dynamic.controller;

import com.shw.dynamic.annotation.MyDataSource;
import com.shw.dynamic.config.RoutingDataSourceContext;
import com.shw.dynamic.enums.DataSourceType;
import com.shw.dynamic.mapper.HelloMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 13:08
 * @description 测试1.放在方法上个可以使用
 * 测试2.放在类上这个类中的所有方法走另外一个数据源
 * 测试3.方法上的优先级比类上的高
 * 测试4.特殊用法，放在需要执行的代码上面
 */
@Controller
@RestController
public class Hello {

    @Autowired
    private HelloMapper helloMapper;

    @GetMapping("/hello")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> hello() {
        List<Map> school = helloMapper.getSchool();
        System.out.println(school);
        List<Map> user = helloMapper.getCatalog();
        System.out.println(user);
        return null;
    }

    @GetMapping("/hi")
    @Transactional(rollbackFor = Exception.class)
    public List<Map> hi() {
        helloMapper.insertCatalog();
        helloMapper.insertSchool();
        return null;
    }

}
