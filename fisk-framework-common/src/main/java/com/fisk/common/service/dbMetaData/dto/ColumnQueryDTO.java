package com.fisk.common.service.dbMetaData.dto;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-21
 * @Description:
 */
@Data
public class ColumnQueryDTO {
//    @ApiModelProperty(value = "数据库id")
//    public Integer dbId;

    @ApiModelProperty(value = "表id")
    public String tableId;

    @ApiModelProperty(value = "架构名称")
    public String schemaName;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "表类型")
    public TableBusinessTypeEnum tableBusinessTypeEnum;
}
