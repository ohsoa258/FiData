package com.fisk.datamodel.dto.businessarea;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaGetDataDTO extends MQBaseDTO {
    public int businessAreaId;
    public List<AtomicIndicatorFactDTO> atomicIndicatorList;
    public List<ModelMetaDataDTO> dimensionList;
}
