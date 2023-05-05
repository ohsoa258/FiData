package com.fisk.mdm.dto.accessmodel;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.accessmdm.AccessMdmPublishTableDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author wangjian
 */
@Data
public class AccessPublishDataDTO extends MQBaseDTO {
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "开放传播")
    public boolean openTransmission;
    @ApiModelProperty(value = "accessId")
    public long accessId;
    @ApiModelProperty(value = "modelId")
    public long modelId;
    @ApiModelProperty(value = "entityId")
    public long entityId;
    @ApiModelProperty(value = "model名称")
    public String modelName;
    @ApiModelProperty(value = "entity名称")
    public String entityName;
    @ApiModelProperty(value = "接入实体列表")
    public AccessMdmPublishTableDTO access;
    @ApiModelProperty(value = "nifi自定义管道Id")
    public String nifiCustomWorkflowId;
}
