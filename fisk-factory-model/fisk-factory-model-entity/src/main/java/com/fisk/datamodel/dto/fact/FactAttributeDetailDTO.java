package com.fisk.datamodel.dto.fact;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    @ApiModelProperty(value = "维度键脚本")
    public String dimensionKeyScript;

    @ApiModelProperty(value = "DTO属性")
    public List<FactAttributeDTO> attributeDTO;

    @ApiModelProperty(value = "DTO同步模式")
    public SyncModeDTO syncModeDTO;

    @ApiModelProperty(value = "数据源Id")
    // public Integer appId;
    public Integer dataSourceId;

    @ApiModelProperty(value = "定制脚本列表")
    public List<CustomScriptInfoDTO> customScriptList;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;

    /**
     * 预览覆盖脚本
     */
    @ApiModelProperty(value = "预览覆盖脚本")
    public String execSql;
}
