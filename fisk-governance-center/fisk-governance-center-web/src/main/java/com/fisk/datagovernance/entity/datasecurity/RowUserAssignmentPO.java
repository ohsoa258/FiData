package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName("tb_row_user_assignment")
@EqualsAndHashCode(callSuper = true)
public class RowUserAssignmentPO extends BasePO {

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
     * 权限(0: 只读  1: 编辑)
     */
    public long permission;

    /**
     * 创建人
     */
    public String createUser;

            }
