package com.fisk.mdm.dto.mathingrules;

import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class AddSourceSystemFiledMappingDto {
    public long sourceSystemMappingId;
    public List<SourceSystemFiledMappingDto> sourceSystemFiledMappingDtoList;
}
