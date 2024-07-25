package com.fisk.datamanagement.dto.standards;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.dataaccess.dto.tablefield.TableFieldDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-07-24
 * @Description:
 */
@Data
public class SearchColumnDTO {
    @ApiModelProperty(value = "表id")
    public String tableId;

    @ApiModelProperty(value = "架构名称")
    public String schemaName;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "表类型")
    public TableBusinessTypeEnum tableBusinessTypeEnum;
    @ApiModelProperty(value = "列值")
    private List<TableFieldDTO> columnDTOList;
}
