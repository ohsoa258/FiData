package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 数据脱敏字段配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Data
public class ColumnsecurityConfigDTO {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * 缺省设置(0: 所有可读  1: 所有不可读)
     */
    @ApiModelProperty(value = "缺省设置(0: 所有可读  1: 所有不可读)")
    public long defaultConfig;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public long datasourceId;

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    public long tableId;

    /**
     * 权限名称
     */
    @ApiModelProperty(value = "权限名称")
    public String permissionsName;

    /**
     * 权限描述
     */
    @ApiModelProperty(value = "权限描述")
    public String permissionsDes;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    public Boolean valid;
}
