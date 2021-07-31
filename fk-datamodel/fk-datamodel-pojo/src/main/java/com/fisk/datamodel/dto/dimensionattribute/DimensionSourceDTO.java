package com.fisk.datamodel.dto.dimensionattribute;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionSourceDTO {
    @TableId
    public long id;
    public String dimensionCnName;
    public List<DimensionDTO> data;
}