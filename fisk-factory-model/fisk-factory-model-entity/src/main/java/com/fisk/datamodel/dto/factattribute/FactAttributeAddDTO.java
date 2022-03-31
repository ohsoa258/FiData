package com.fisk.datamodel.dto.factattribute;

import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeAddDTO {
    public int factId;
    public boolean isPublish;
    /**
     * 发布备注
     */
    public String remark;
    public List<FactAttributeDTO> list;
    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    public boolean openTransmission;
}
