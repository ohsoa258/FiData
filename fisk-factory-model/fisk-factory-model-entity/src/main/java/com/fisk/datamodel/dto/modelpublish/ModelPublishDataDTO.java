package com.fisk.datamodel.dto.modelpublish;

import com.fisk.common.core.enums.datamodel.DataModelTblTypeEnum;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishDataDTO extends MQBaseDTO {
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "开放传播")
    public boolean openTransmission;

    /**
     * 是否删除目标表
     */
    @ApiModelProperty(value = "是否删除目标表")
    public boolean ifDropTargetTbl;

    @ApiModelProperty(value = "业务域Id")
    public long businessAreaId;
    @ApiModelProperty(value = "业务域名称")
    public String businessAreaName;
    @ApiModelProperty(value = "维度列表")
    public List<ModelPublishTableDTO> dimensionList;
    @ApiModelProperty(value = "nifi自定义管道Id")
    public String nifiCustomWorkflowId;

    /**
     * dim表 or fact表
     */
    @ApiModelProperty(value = "dim表 or fact表")
    public DataModelTblTypeEnum dimOrFact;
}
