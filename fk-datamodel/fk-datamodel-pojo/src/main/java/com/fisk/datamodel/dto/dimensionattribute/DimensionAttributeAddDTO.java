package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
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
     * 发布备注
     */
    public String remark;
    public List<DimensionAttributeDTO> list;

    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    public boolean openTransmission;
}
