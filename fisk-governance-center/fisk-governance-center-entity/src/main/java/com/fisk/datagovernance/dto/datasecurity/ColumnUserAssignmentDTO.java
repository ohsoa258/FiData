package com.fisk.datagovernance.dto.datasecurity;

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
public class ColumnUserAssignmentDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * tb_rowsecurity_config表  id
     */
    public long rowsecurityId;

    /**
     * 类型: 0: 用户组  1: 用户
     */
    public long type;

    /**
     * 用户id or 用户组id
     */
    public long userId;

    /**
     * 权限(0: 只读  1: 禁止访问)
     */
    public long permission;

}
