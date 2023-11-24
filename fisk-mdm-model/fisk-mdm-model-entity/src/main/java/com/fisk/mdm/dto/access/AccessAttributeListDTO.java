package com.fisk.mdm.dto.access;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "accessId")
    public Integer accessId;
    /**
     * sql脚本
     */
    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;
    /**
     * 基于域字段更新脚本
     */
    @ApiModelProperty(value = "基于域字段更新脚本")
    public String domainUpdateSql;
    /**
     * 基于域字段关联映射关系
     */
    @ApiModelProperty(value = "基于域字段关联映射关系")
    public List<TableSourceRelationsDTO> tableSourceRelationsDTO;

    @ApiModelProperty(value = "字段信息")
    public List<AttributeInfoDTO> attributeInfoDTOS;
    /**
     * 增量配置信息
     */
    @ApiModelProperty(value = "增量配置信息")
    public SyncModeDTO syncModeDTO;
    /**
     * 源系统数据源Id
     */
    @ApiModelProperty(value = "源系统数据源Id")
    public Integer dataSourceId;
    /**
     *转换过程自定义逻辑
     */
    @ApiModelProperty(value = "转换过程自定义逻辑")
    public List<CustomScriptInfoDTO> customScriptList;
    /**
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    @ApiModelProperty(value = "预览nifi调用SQL执行语句")
    public String execSql;
    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    public Integer versionId;

}
