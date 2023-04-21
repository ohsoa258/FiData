package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.syncmode.SyncModeDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    @ApiModelProperty(value = "DTO属性列表")
    public List<DimensionAttributeDTO> attributeDTOList;

    @ApiModelProperty(value = "DTO同步模式")
    public SyncModeDTO syncModeDTO;

    // public Integer appId;
    @ApiModelProperty(value = "数据资源Id")
    public Integer dataSourceId;

    @ApiModelProperty(value = "维度键脚本")
    public String dimensionKeyScript;

    @ApiModelProperty(value = "定制脚本")
    public List<CustomScriptInfoDTO> customScriptList;
    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "时间增量")
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    @ApiModelProperty(value = "预览nifi调用SQL执行语句")
    public String execSql;

}
