package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:42
 * @Version 1.0
 */
@Data
public class ViwGroupUpdateDTO {
    @ApiModelProperty(value = "id")
    @NotNull
    private Integer id;
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "详细信息")
    private String details;
}
