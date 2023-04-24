package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/26 11:01
 * @Version 1.0
 */
@Data
public class ViwGroupQueryDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    /**
     * 视图组id
     */
    @ApiModelProperty(value = "视图组id")
    private Integer groupId;
}
