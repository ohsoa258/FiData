package com.fisk.datamodel.dto.dimension;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DimTransDTO {

    @ApiModelProperty(value = "事实表id")
    public int dimId;

    /**
     * 业务域id
     */
    @ApiModelProperty(value = "当前业务域id")
    public int curBusinessId;

    /**
     * 业务域id
     */
    @ApiModelProperty(value = "要移动到的业务域id")
    public int toBusinessId;

    /**
     * 业务过程id
     */
    @ApiModelProperty(value = "要移动到的业务文件夹id")
    public int toDimensionFolderId;

}