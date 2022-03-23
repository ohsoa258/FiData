package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataModelTable {

    /**
     * 获取数据建模所有表以及字段配置数据
     * @param publishStatus
     * @return
     */
    List<SourceTableDTO> getDataModelTable(int publishStatus);

}
