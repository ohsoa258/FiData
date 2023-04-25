package com.fisk.mdm.dto.accessmodel;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.accessmodel.AccessMdmPublishTableDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AccessPublishDataDTO extends MQBaseDTO {
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "开放传播")
    public boolean openTransmission;
    @ApiModelProperty(value = "modelId")
    public long modelId;
    @ApiModelProperty(value = "model名称")
    public String modelName;
    @ApiModelProperty(value = "维度列表")
    public List<AccessMdmPublishTableDTO> dimensionList;
    @ApiModelProperty(value = "nifi自定义管道Id")
    public String nifiCustomWorkflowId;
}
