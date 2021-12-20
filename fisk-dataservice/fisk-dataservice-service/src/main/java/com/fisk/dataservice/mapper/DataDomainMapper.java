package com.fisk.dataservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/10/22 17:57
 */
@Mapper
public interface DataDomainMapper {

    /**
     * 执行查询sql
     * @param filedName
     * @param tableName
     * @return
     */
    @Select("SELECT ${filedName} FROM ${tableName}")
    List<Object> queryData(@Param("filedName") String filedName, @Param("tableName")String tableName);
}
