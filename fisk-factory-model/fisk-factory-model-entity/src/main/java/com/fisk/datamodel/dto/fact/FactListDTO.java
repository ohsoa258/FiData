package com.fisk.datamodel.dto.fact;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactListDTO extends FactDTO {
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 维度字段列表
     */
    @ApiModelProperty(value = "维度字段列表")
    public List<FactAttributeDTO> attributeList;
}
