package com.fisk.mdm.dto.access;

import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjiang
 * @Description:
 **/
@Data
public class OverlayCodePreviewAccessDTO {

    @ApiModelProperty(value = "发布模式字段DTO列表")
    public List<AccessAttributeDTO> AccessMdmPublishFieldDTOList;

    @ApiModelProperty(value = "同步模式")
    public Integer syncMode;

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "versionId")
    public Integer versionId;

}
