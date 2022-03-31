package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Data
public class RowUserAssignmentDTO {

    @ApiModelProperty(value = "主键(修改时必传)", required = true)
    public long id;

    @ApiModelProperty(value = "tb_rowsecurity_config表id (修改时必传)", required = true)
    public long rowsecurityId;

    /**
     * 类型(0: 空  1: 用户组  2: 用户)
     */
    @ApiModelProperty(value = "类型(0: 空  1: 用户组  2: 用户)", required = true)
    public Long type;

    /**
     * 用户id or 用户组id
     */
    @ApiModelProperty(value = "用户id or 用户组id", required = true)
    public long userId;

    /**
     * 权限(0: 空  1: 只读  2: 编辑)
     */
    @ApiModelProperty(value = "权限(0: 空  1: 只读  2: 编辑)", required = true)
    public Long permission;

}
