package com.fisk.datamanagement.dto.metadataentity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JinXingWang
 */
@Data
public class UpdateMetadataEmailGroupDTO {

    /**
     * 元数据id
     */
    @ApiModelProperty(value = "元数据id")
    public Integer entityId;

    /**
     * 邮件组id
     */
    @ApiModelProperty(value = "邮件组id")
    public Integer emailGroupId;
}
