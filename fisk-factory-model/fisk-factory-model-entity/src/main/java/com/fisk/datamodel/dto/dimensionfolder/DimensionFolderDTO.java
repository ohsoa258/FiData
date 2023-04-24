package com.fisk.datamodel.dto.dimensionfolder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    public int businessId;
    /**
     * 维度文件夹中文名称
     */
    @ApiModelProperty(value = "维度文件夹中文名称")
    public String dimensionFolderCnName;
    /**
     * 维度文件夹英文名称
     */
    @ApiModelProperty(value = "维度文件夹英文名称")
    public String dimensionFolderEnName;
    /**
     * 维度文件夹描述
     */
    @ApiModelProperty(value = "维度文件夹描述")
    public String dimensionFolderDesc;
    /**
     * 是否共享
     */
    @ApiModelProperty(value = "是否共享")
    public boolean share;

}
