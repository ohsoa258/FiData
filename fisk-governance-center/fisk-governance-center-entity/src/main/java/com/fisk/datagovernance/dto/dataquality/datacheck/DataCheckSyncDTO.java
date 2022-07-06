package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验_同步DTO
 * @date 2022/5/16 20:44
 */
@Data
public class DataCheckSyncDTO {
    /**
     * FiData系统数据源ID
     */
    @ApiModelProperty(value = "FiData系统数据源ID")
    public String dataSourceId;

    /**
     * 表前缀
     */
    @ApiModelProperty(value = "表前缀")
    public String tablePrefix;

    /**
     * 表唯一标识：表名称/表ID
     */
    @ApiModelProperty(value = "表唯一标识：表名称/表ID")
    public String tableUnique;

    /**
     * 表业务类型
     */
    @ApiModelProperty(value = "表业务类型")
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
     * 校验依据字段集合，key：字段名称 value：字段值
     */
    @ApiModelProperty(value = "校验依据字段集合，key：字段名称 value：字段值")
    public HashMap<String,Object> checkByFieldMap;

    /**
     * 消息字段，用于拼接消息内容
     */
    @ApiModelProperty(value = "消息字段，用于拼接消息内容")
    public String msgField;
}
