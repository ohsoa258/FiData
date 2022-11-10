package com.fisk.common.service.dbBEBuild.datamodel.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableSourceTableConfigDTO {

    public int tableId;

    public String tableName;
    /**
     * 0:维度表、1:事实表
     */
    public int tableType;

    public List<TableSourceFieldConfigDTO> columnConfig;

}
