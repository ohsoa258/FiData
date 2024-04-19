package com.fisk.datamanagement.dto.metamap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MetaMapTblDTO {

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    private Integer tblId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    private String tblName;

}
