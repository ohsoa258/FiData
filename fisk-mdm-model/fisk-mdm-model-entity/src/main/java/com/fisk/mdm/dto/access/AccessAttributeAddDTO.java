package com.fisk.mdm.dto.access;

import com.fisk.common.service.dbBEBuild.datamodel.dto.RelationDTO;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author jianwenyang
 */
@Data
public class AccessAttributeAddDTO{
    @ApiModelProperty(value = "accessId")
    public int accessId;
    /**
     * 是否发布
     */
    @ApiModelProperty(value = "是否发布")
    public boolean publish;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    private Integer dataSourceId;

    /**
     * 查询接入sql
     */
    @ApiModelProperty(value = "查询接入sql")
    public String accessSql;
    /**
     * 覆盖脚本
     */
    @ApiModelProperty(value = "覆盖脚本")
    public String coverScript;
    /**
     * 发布备注
     */
    @ApiModelProperty(value = "发布备注")
    public String remark;
    /**
     * 基于域字段更新脚本
     */
    @ApiModelProperty(value = "基于域字段更新脚本")
    public String domainUpdateSql;
    /**
     * 基于域字段关联映射关系
     */
    @ApiModelProperty(value = "基于域字段关联映射关系")
    public List<RelationDTO> tableSourceRelationsDTO;
    /**
     * 列字段对应关系
     */
    @ApiModelProperty(value = "列字段对应关系")
    public List<AccessAttributeDTO> list;
    /**
     * 增量配置
     */
    @ApiModelProperty(value = "增量配置")
    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    @ApiModelProperty(value = "是否同步")
    public boolean openTransmission;

    /**
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 自定义脚本集合
     */
    @ApiModelProperty(value = "自定义脚本集合")
    public List<CustomScriptDTO> customScriptList;

    /**
     * 表历史
     */
    @ApiModelProperty(value = "表历史")
    public List<TableHistoryDTO> tableHistorys;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    public Long userId;
    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    public Integer versionId;
}
