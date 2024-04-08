package com.fisk.datamanagement.dto.businessclassification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-04-08
 * @Description:
 */
@Data
public class BusinessMetaDataTreeDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "pid")
    public Integer pid;
    @ApiModelProperty(value = "类型")
    public Integer type;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "排序")
    public Integer sort;
    @ApiModelProperty(value = "子类")
    public List<BusinessMetaDataTreeDTO> Children;
}
