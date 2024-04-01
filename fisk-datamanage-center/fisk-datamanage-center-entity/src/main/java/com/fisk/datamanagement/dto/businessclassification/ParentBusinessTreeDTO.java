package com.fisk.datamanagement.dto.businessclassification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-03-28
 * @Description:
 */
@Data
public class ParentBusinessTreeDTO {

    @ApiModelProperty(value = "id")
    public String id;

    @ApiModelProperty(value = "pid")
    public String pid;


    @ApiModelProperty(value = "sort")
    public Integer sort;

    @ApiModelProperty(value = "名称")
    public String name;


    @ApiModelProperty(value = "1:目录2数据")
    public Integer type;

    List<ParentBusinessTreeDTO> child;
}
