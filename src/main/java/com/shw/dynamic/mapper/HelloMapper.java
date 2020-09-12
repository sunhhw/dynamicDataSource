package com.shw.dynamic.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author shw
 * @version 1.0
 * @date 2020/9/12 13:09
 * @description
 */
@Mapper
public interface HelloMapper {

    /**
     * 查询所有学校
     * @return String字符串
     */
    @Select("select * from school")
    List<Map> selectAllSchool();

    /**
     * fa
     * @return String
     */
    @Select("select * from catalog ")
    List<Map> selectAllCatalog();

}
