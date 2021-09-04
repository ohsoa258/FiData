package com.fisk.datamodel.dto.businessprocess;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessPushListDTO extends ModelMetaDataDTO {

    public List<AtomicIndicatorPushDTO> atomicIndicatorPushDTOList;
}
