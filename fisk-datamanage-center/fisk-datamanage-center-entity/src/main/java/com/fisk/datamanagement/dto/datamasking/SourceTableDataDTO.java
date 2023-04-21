package com.fisk.datamanagement.dto.datamasking;

import com.fisk.datamanagement.enums.TableTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceTableDataDTO {

    @ApiModelProperty(value = "表类型Enum")
    public TableTypeEnum tableTypeEnum;

    @ApiModelProperty(value = "表ID")
    public long tableId;

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "表guid")
    public String tableGuid;

}
