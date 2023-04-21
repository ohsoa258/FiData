package com.fisk.datamodel.dto.businessprocess;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessPushListDTO extends ModelMetaDataDTO {

    @ApiModelProperty(value = "将DTO推送到列表")
    public List<AtomicIndicatorPushDTO> atomicIndicatorPushDTOList;
}
