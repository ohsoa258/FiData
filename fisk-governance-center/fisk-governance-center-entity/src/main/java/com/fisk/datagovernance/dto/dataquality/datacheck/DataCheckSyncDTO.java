package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验DTO_同步中
 * @date 2022/5/16 20:44
 */
@Data
public class DataCheckSyncDTO {
    /**
     * FiData平台数据源ID
     */
    @ApiModelProperty(value = "FiData平台数据源ID")
    @NotNull()
    public String fiDataDataSourceId;

    /**
     * 表唯一标识：表ID
     */
    @ApiModelProperty(value = "表唯一标识：表ID")
    @NotNull()
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    @NotNull()
    public String tableName;

    /**
     * 表业务类型
     */
    @ApiModelProperty(value = "表业务类型")
    @NotNull()
    public TableBusinessTypeEnum tableBusinessType;

    /**
     * 校验通过修改字段集合，key：字段名称 value：字段值
     */
    @ApiModelProperty(value = "校验成功修改字段集合，key：字段名称 value：字段值")
    public HashMap<String,Object> updateFieldMap_Y;

    /**
     * 校验不通过修改字段集合，key：字段名称 value：字段值
     */
    @ApiModelProperty(value = "校验不通过修改字段集合，key：字段名称 value：字段值")
    public HashMap<String,Object> updateFieldMap_N;

    /**
     * 校验不通过但校验规则为弱类型规则修改字段集合，key：字段名称 value：字段值
     */
    @ApiModelProperty(value = "校验不通过但校验规则为弱类型规则修改字段集合，key：字段名称 value：字段值")
    public HashMap<String,Object> updateFieldMap_R;

    /**
     * 校验/更新依据字段集合，key：字段名称 value：字段值
     */
    @ApiModelProperty(value = "校验依据字段集合，key：字段名称 value：字段值")
    public HashMap<String,Object> checkByFieldMap;

    /**
     * 消息字段，用于拼接消息内容
     */
    @ApiModelProperty(value = "消息字段，用于拼接消息内容")
    public String msgField;
}
