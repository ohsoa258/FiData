package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.SlicerDTO;
import com.fisk.dataservice.dto.DataDoFieldDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/23 16:36
 */
public interface DataDomainService {

    /**
     * 拼接Sql方法
     * @param apiConfigureFieldList
     * @param currentPage
     * @param pageSize
     * @return
     */
    Object query(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize);

    /**
     * 根据字段获取数据
     * @param dto
     * @return
     */
    Object getSlicer(SlicerDTO dto);

    /**
     * 执行SQL
     * @param filedName
     * @param tableName
     * @return
     */
    Object executeToSql(String filedName,String tableName);
}
