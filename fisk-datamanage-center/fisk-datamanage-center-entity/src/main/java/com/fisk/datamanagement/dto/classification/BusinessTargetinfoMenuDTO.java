package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-04-11
 * @Description:
 */
@Data
public class BusinessTargetinfoMenuDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "类型")
    public String type;
    @ApiModelProperty(value = "指标状态")
    public String indicatorStatus;
    @ApiModelProperty(value = "上级指标Id")
    public Integer parentBusinessId;
}
