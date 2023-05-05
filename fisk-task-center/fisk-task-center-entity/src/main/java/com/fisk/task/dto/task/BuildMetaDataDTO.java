package com.fisk.task.dto.task;

import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BuildMetaDataDTO extends MQBaseDTO {

    @ApiModelProperty(value = "用户id")
    public Long userId;

    @ApiModelProperty(value = "数据")
    public List<MetaDataInstanceAttributeDTO> data;

}
