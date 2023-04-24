package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 权限管理dto
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-29 17:59:34
 */
@Data
public class PermissionManagementDTO {

    /**
     * 表类型(0: 空  1: 表级  2: 行级  3: 列级)
     */
    @ApiModelProperty(value = "表类型(0: 空  1: 表级  2: 行级  3: 列级)")
    public Integer tableType;

    /**
     * 表级or行级or列级安全表id
     */
    @ApiModelProperty(value = "表级or行级or列级安全表id")
    public Long tableId;

    /**
     * 访问类型(0: 空 1:用户组   2: 用户)
     */
    @ApiModelProperty(value = "访问类型(0: 空 1:用户组   2: 用户)")
    public Long accessType;

    /**
     * 访问权限(0 :空  1: 编辑  2: 只读  3: 导入  4:导出)
     */
    @ApiModelProperty(value = "访问权限(0 :空  1: 编辑  2: 只读  3: 导入  4:导出)")
    public Long accessPermission;

}
