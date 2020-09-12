# 若依-动态数据源
# 原理
我们的所有操作都是要扩展AbstractRoutingDataSource类，并重写其中的determineCurrentLookupKey()方法，来实现数据源的切换；
determineCurrentLookupKey()是AbstractRoutingDataSource类中的一个抽象方法，而它的返回值是当前线程要用的数据源dataSource的key值，有了这个key值，resolvedDataSource（这是个map,由配置文件中设置好后存入的）就从中取出对应的DataSource，如果找不到，就用配置默认的数据源。

# 思路
Spring内置了一个AbstractRoutingDataSource，它可以把多个数据源配置成一个Map，然后，根据不同的key返回不同的数据源。因为AbstractRoutingDataSource也是一个DataSource接口，因此，应用程序可以先设置好key， 访问数据库的代码就可以从AbstractRoutingDataSource拿到对应的一个真实的数据源，从而访问指定的数据库；

https://www.cnblogs.com/nxzblogs/p/11849797.html

# 功能使用
 * 测试1.放在方法上可以使用
 * 测试2.放在类上这个类中的所有方法都被AOP拦截切换
 * 测试3.方法上的优先级比类上的高
 * 测试4.特殊用法，放在需要执行的代码上面

# 测试代码

```java
@RestController
@MyDataSource(DataSourceType.SLAVE)
public class Hello {

    @Autowired
    private HelloMapper helloMapper;

    @GetMapping("/hello")
    @MyDataSource(DataSourceType.MASTER)
    public List<Map> hello() {
        return helloMapper.selectAllSchool();
    }

  
    @GetMapping("/hi")
    public List<Map> hi() {
        List<Map> maps = helloMapper.selectAllCatalog();
        return maps;
    }

    @GetMapping("/say")
    public List<Map> say() {
        RoutingDataSourceContext.setDataSourceRoutingKey(DataSourceType.SLAVE.name());
        List<Map> maps = helloMapper.selectAllCatalog();
        RoutingDataSourceContext.close();
        return maps;
    }
    
}
```

