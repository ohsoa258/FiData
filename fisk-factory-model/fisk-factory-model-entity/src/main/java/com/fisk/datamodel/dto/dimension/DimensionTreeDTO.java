package com.fisk.datamodel.dto.dimension;

import lombok.Data;

import java.util.List;

@Data
public class DimensionTreeDTO {

    //公共域维度
    private List<BusinessAreaDimDTO> publicDim;

    //按应用区分的维度
    private List<BusinessAreaDimDTO> otherDimsByArea;

}
