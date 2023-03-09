package com.fisk.datamodel.dto.fact;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDetailDTO {
    /**
     * sql脚本
     */
    public String sqlScript;

    public String dimensionKeyScript;

    public List<FactAttributeDTO> attributeDTO;

    public SyncModeDTO syncModeDTO;

    // public Integer appId;
    public Integer dataSourceId;

    public List<CustomScriptInfoDTO> customScriptList;

    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
}
