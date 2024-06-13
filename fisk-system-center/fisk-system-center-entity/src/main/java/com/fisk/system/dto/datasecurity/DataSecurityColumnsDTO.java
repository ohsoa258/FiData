package com.fisk.system.dto.datasecurity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class DataSecurityColumnsDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public Long id;

    /**
     * 角色id
     */
    @ApiModelProperty(value = "角色id")
    private Integer roleId;

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    private Integer appId;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    private String appName;

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    private Integer tblId;

    /**
     * 表类型 OlapTableEnum
     */
    @ApiModelProperty(value = "表类型 OlapTableEnum")
    private Integer tblType;

    /**
     * 可读字段id
     */
    @ApiModelProperty(value = "可读字段id")
    private List<Integer> readableFieldIds;

    /**
     * 可读字段id
     */
    @ApiModelProperty(value = "可读字段id")
    private Map<Long,String> readableFieldMap;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    private String tblName;

    /**
     * 表显示名称
     */
    @ApiModelProperty(value = "表显示名称")
    private String tblDisName;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
