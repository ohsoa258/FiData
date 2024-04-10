package com.fisk.common.core.utils.dbutils.dto;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableNameDTO {

    public String tableId;
    public String tableName;
    public String schemaName;
    public TableBusinessTypeEnum tableBusinessTypeEnum;

    public List<TableColumnDTO> columnList;

}
