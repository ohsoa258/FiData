package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;
import sun.plugin.javascript.navig.LinkArray;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataModelTable {

    /**
     * 获取数据建模所有表以及字段配置数据
     * @return
     */
    List<SourceTableDTO> getDataModelTable();

}
