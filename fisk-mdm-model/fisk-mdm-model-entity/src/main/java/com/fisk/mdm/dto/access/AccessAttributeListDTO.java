package com.fisk.mdm.dto.access;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author jianwenyang
 */
@Data
public class AccessAttributeListDTO {
    /**
     * sql脚本
     */
    public String sqlScript;

    public List<AccessAttributeDTO> attributeDTOList;

    public List<AttributeInfoDTO> attributeInfoDTOS;

    public SyncModeDTO syncModeDTO;

    public Integer dataSourceId;

    public String accessKeyScript;

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
