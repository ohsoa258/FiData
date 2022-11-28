package com.fisk.datamodel.dto.fact;

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

    public List<FactAttributeDTO> attributeDTO;

    public SyncModeDTO syncModeDTO;

    public Integer appId;

    public List<CustomScriptInfoDTO> customScriptList;
}
