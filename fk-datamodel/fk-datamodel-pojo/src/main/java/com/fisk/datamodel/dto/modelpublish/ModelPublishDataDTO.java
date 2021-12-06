package com.fisk.datamodel.dto.modelpublish;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishDataDTO extends MQBaseDTO {
    public long businessAreaId;
    public String businessAreaName;
    public List<ModelPublishTableDTO> dimensionList;
    public String nifiCustomWorkflowId;
}
