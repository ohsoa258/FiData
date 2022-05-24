package com.fisk.datagovernance.dto.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
     * 服务器IP
     */
    @ApiModelProperty(value = "服务器IP")
    @NotNull()
    public String ip;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    @NotNull()
    public String dbName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    @NotNull()
    public String tableName;

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
