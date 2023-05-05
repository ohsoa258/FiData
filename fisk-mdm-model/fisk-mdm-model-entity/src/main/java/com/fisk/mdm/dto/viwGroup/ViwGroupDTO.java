package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:42
 * @Version 1.0
 */
@Data
public class ViwGroupDTO {

    @ApiModelProperty(value = "Id")
    private Integer id;
    @ApiModelProperty(value = "实体Id")
    private Integer entityId;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "详细信息")
    private String details;
}
