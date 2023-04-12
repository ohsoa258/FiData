package com.fisk.common.core.utils.dbutils.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableNameDTO {

    public String tableName;

    public List<TableColumnDTO> columnList;

}
