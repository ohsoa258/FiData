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
public class TablesecurityConfigDTO {

    @ApiModelProperty(value = "主键(修改时必传)", required = true)
    public long id;

    /**
     * 缺省设置(0: 所有可读  1: 所有不可读--此时无论权限如何,当前表不可读取),默认值: 0
     */
    @ApiModelProperty(value = "缺省设置(0: 空 1: 所有可读  2: 所有不可读--此时无论权限如何,当前表不可读取),默认值: 1", required = true)
    public Long defaultConfig;

    @ApiModelProperty(value = "数据源id", required = true)
    public String datasourceId;

    @ApiModelProperty(value = "表id", required = true)
    public String tableId;

    @ApiModelProperty(value = "访问类型(0: 空  1:用户组  2: 用户)", required = true)
    public Long accessType;

    @ApiModelProperty(value = "用户组id or 用户id", required = true)
    public Long userGroupId;

    @ApiModelProperty(value = "回显的用户组名 or 用户名")
    public String name;

    @ApiModelProperty(value = "访问权限(0: 空  1: 编辑  2: 只读  3: 导入  4:导出)", required = true)
    public long accessPermission;
}
