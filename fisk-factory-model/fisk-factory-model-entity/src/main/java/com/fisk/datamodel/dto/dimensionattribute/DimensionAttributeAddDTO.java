package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeAddDTO extends MQBaseDTO {
    public int dimensionId;
    /**
     * 是否发布
     */
    public boolean isPublish;
    public String dimensionName;
    /**
     * 业务域名称
     */
    public String businessAreaName;
    public int createType;
    /**
     * 维度key数据同步脚本
     */
    public String dimensionKeyScript;
    /**
     * 覆盖脚本
     */
    public String coverScript;
    /**
     * 发布备注
     */
    public String remark;
    public List<DimensionAttributeDTO> list;

    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    public boolean openTransmission;
    /**
     * 自定义脚本集合
     */
    public List<CustomScriptDTO> customScriptList;

    /**
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
}
