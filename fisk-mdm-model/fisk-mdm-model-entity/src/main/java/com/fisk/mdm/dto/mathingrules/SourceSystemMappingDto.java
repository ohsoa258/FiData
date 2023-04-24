package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class SourceSystemMappingDto {
    /**
     * 源系统ID
     */
    @ApiModelProperty(value = "源系统ID")
    public Integer sourceSystemId;
    /**
     * 源系统表
     */
    @ApiModelProperty(value = "源系统表")
    public Integer sourceSystemTableId;
    /**
     * 源系统字段映射
     */
    @ApiModelProperty(value = "源系统字段映射")
    public List<SourceSystemFiledMappingDto> sourceSystemFiledMappingDtoList;
}
