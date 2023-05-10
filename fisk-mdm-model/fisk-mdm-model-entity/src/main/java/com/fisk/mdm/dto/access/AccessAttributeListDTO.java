package com.fisk.mdm.dto.access;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author jianwenyang
 */
@Data
public class AccessAttributeListDTO {
    /**
     * accessId
     */
    public Integer accessId;
    /**
     * sql脚本
     */
    public String sqlScript;
    /**
     * 源表与目标表对应关系列
     */
    public List<AccessAttributeDTO> attributeDTOList;
    /**
     * 增量配置信息
     */
    public SyncModeDTO syncModeDTO;
    /**
     * 源系统数据源Id
     */
    public Integer dataSourceId;
    /**
     *转换过程自定义逻辑
     */
    public List<CustomScriptInfoDTO> customScriptList;
    /**
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    public String execSql;

}
