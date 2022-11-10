package com.fisk.datamodel.service;

import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.datamodel.dto.tableconfig.SourceTableDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDataModelTable {

    /**
     * 获取数据建模所有表以及字段配置数据
     *
     * @param publishStatus
     * @return
     */
    List<SourceTableDTO> getDataModelTable(int publishStatus);

    /**
     * 获取数仓中每个表中的业务元数据配置
     *
     * @param parameterDto
     * @return
     */
    TableRuleInfoDTO setTableRule(TableRuleParameterDTO parameterDto);

}
