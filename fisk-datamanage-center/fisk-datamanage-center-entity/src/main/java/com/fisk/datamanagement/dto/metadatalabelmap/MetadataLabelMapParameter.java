package com.fisk.datamanagement.dto.metadatalabelmap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class MetadataLabelMapParameter {

    @ApiModelProperty(value = "元数据实体id")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "标签ID")
    public List<Integer> labelIds;

}
