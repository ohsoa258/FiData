package com.fisk.datamanagement.dto.modelAndIndex;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


@Data
public class ModelAndIndexMappingDTO implements Serializable {
    public Integer id;

    @ApiModelProperty(value = "指标id")
    public Integer indexId;

    @ApiModelProperty(value = "业务域id/维度文件夹id")
    public String areaOrDimFolderId;

    @ApiModelProperty(value = "业务域名称/维度文件夹名称")
    public String areaOrDimFolderName;

    @ApiModelProperty(value = "维度表id/事实表id")
    public String tblId;

    @ApiModelProperty(value = "维度表名称/事实表名称")
    public String tblName;

    @ApiModelProperty(value = "字段id")
    public String fieldId;

    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    @ApiModelProperty(value = "表类型：0公共域维度 1其他域维度 2事实表")
    public Integer tblType;

    public String createUser;

}
