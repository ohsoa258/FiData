package com.fisk.datamodel.dto;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaGetDataDTO {
    public List<AtomicIndicatorFactDTO> atomicIndicatorList;
    public List<ModelMetaDataDTO> dimensionList;
}
