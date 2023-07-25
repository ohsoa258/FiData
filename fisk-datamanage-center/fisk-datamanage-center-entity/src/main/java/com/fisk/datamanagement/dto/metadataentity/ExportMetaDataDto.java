package com.fisk.datamanagement.dto.metadataentity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class ExportMetaDataDto {
    @ApiModelProperty(value = "所属业务分类Id")
    public List<Long> businessClassificationId;
    @ApiModelProperty(value = "关联实体类型  1 所有父级 2所有子集")
    public List<Integer> associatedType;
}
