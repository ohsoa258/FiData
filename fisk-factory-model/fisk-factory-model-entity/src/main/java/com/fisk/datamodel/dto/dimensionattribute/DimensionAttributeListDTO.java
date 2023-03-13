package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
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

    public String dimensionKeyScript;

    public List<CustomScriptInfoDTO> customScriptList;
    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    public String execSql;

}
