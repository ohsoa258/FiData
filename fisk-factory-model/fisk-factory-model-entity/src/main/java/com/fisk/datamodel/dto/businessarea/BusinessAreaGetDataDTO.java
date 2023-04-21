package com.fisk.datamodel.dto.businessarea;

import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaGetDataDTO extends MQBaseDTO {
    @ApiModelProperty(value = "业务区域Id")
    public int businessAreaId;

    @ApiModelProperty(value = "原子指示器列表")
    public List<AtomicIndicatorFactDTO> atomicIndicatorList;

    @ApiModelProperty(value = "维度列表")
    public List<ModelMetaDataDTO> dimensionList;
}
