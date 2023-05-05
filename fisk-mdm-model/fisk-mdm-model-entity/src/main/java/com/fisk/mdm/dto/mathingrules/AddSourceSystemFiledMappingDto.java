package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class AddSourceSystemFiledMappingDto {
    @ApiModelProperty(value = "源系统映射id")
    public long sourceSystemMappingId;
    @ApiModelProperty(value = "源系统文件映射Dto列表")
    public List<SourceSystemFiledMappingDto> sourceSystemFiledMappingDtoList;
}
