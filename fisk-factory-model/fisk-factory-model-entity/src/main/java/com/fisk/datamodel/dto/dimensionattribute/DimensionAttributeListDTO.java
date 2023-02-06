package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeListDTO {
    /**
     * sql脚本
     */
    public String sqlScript;

    public List<DimensionAttributeDTO> attributeDTOList;

    public SyncModeDTO syncModeDTO;

    // public Integer appId;

    public Integer dataSourceId;

    public List<CustomScriptInfoDTO> customScriptList;

}
