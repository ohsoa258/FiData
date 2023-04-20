package com.fisk.datamanagement.dto.metadataentityoperationLog;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:43
 * @description
 */
@Data
public class MetaDataEntityOperationLogDTO {

    @ApiModelProperty(value = "id")
    private long id;

    @ApiModelProperty(value = "元数据实体对象id")
    private String metadataEntityId;

    @ApiModelProperty(value = "操作类型")
    private String operationType;   //操作类型

    @ApiModelProperty(value = "操作前")
    private String beforeChange;    //操作前

    @ApiModelProperty(value = "操作后")
    private String afterChange;     //操作后

    @ApiModelProperty(value = "创建者")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "逻辑删除")
    private int delFlag;
}
