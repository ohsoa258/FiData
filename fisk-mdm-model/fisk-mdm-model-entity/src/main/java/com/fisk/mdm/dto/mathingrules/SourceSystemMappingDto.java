package com.fisk.mdm.dto.mathingrules;

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
    public Integer sourceSystemId;
    /**
     * 源系统表
     */
    public Integer sourceSystemTableId;
    /**
     * 源系统字段映射
     */
    public List<SourceSystemFiledMappingDto> sourceSystemFiledMappingDtoList;
}
