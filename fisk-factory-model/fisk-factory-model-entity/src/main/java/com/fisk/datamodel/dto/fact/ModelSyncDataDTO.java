package com.fisk.datamodel.dto.fact;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ModelSyncDataDTO implements Serializable {

    /**
     * tblId
     */
    @ApiModelProperty(value = "表id")
    public Long tblId;

    /**
     * 表类型：0维度表 1事实表（fact dwd dws help config）
     */
    @ApiModelProperty(value = "表类型：0维度表 1事实表（fact dwd dws help config）")
    public Integer tblType;

}
