package com.shw.dynamic.mapper;

import com.shw.dynamic.annotation.MyDataSource;
import com.shw.dynamic.enums.DataSourceType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 13:09
 * @description
 */

public interface HelloMapper {

    @MyDataSource(DataSourceType.SLAVE)
    List<Map> getCatalog();

    List<Map> getSchool();

    @MyDataSource(DataSourceType.SLAVE)
    void insertCatalog();

    void insertSchool();

}
