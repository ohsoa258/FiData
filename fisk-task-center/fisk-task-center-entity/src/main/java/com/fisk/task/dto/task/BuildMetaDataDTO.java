package com.fisk.task.dto.task;

import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BuildMetaDataDTO extends MQBaseDTO {

    public long userId;

    public List<MetaDataInstanceAttributeDTO> data;

}
