package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeSelectDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 2.5
 * @description 关联维度-下拉对象
 * @date 2022/6/6 17:28
 */
@Data
public class DimensionSelectDTO {

    @ApiModelProperty(value = "关联维度id")
    private Long id;

    @ApiModelProperty(value = "关联维度表名")
    private String dimensionTabName;

    @ApiModelProperty(value = "是否共享")
    private Boolean share;

    /**
     * 维度字段对象集合
     */
    private List<DimensionAttributeSelectDTO> list;
}
