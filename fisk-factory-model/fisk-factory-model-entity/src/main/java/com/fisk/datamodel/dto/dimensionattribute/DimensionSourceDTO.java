package com.fisk.datamodel.dto.dimensionattribute;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionSourceDTO {
    @ApiModelProperty(value = "id")
    @TableId
    public long id;
    @ApiModelProperty(value = "维度中文名")
    public String dimensionCnName;

    @ApiModelProperty(value = "数据")
    public List<DimensionDTO> data;
}