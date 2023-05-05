package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeAddDTO extends MQBaseDTO {
    @ApiModelProperty(value = "维度Id")
    public int dimensionId;
    /**
     * 是否发布
     */
    @ApiModelProperty(value = "是否发布")
    public boolean isPublish;
    @ApiModelProperty(value = "维度名称")
    public String dimensionName;
    /**
     * 业务域名称
     */
    @ApiModelProperty(value = "业务域名称")
    public String businessAreaName;
    @ApiModelProperty(value = "创建类型")
    public int createType;
    /**
     * 维度key数据同步脚本
     */
    @ApiModelProperty(value = "维度key数据同步脚本")
    public String dimensionKeyScript;
    /**
     * 覆盖脚本
     */
    @ApiModelProperty(value = "覆盖脚本")
    public String coverScript;
    /**
     * 发布备注
     */
    @ApiModelProperty(value = "发布备注")
    public String remark;

    @ApiModelProperty(value = "列表")
    public List<DimensionAttributeDTO> list;

    @ApiModelProperty(value = "dto同步模式")
    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "是否同步")
    public boolean openTransmission;
    /**
     * 自定义脚本集合
     */
    @ApiModelProperty(value = "自定义脚本集合")
    public List<CustomScriptDTO> customScriptList;

    /**
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;
}
