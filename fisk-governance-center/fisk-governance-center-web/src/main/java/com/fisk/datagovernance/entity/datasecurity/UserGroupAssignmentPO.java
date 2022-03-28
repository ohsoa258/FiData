package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("tb_user_group_assignment")
@EqualsAndHashCode(callSuper = true)
public class UserGroupAssignmentPO extends BasePO {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * 用户组id
     */
    public long userGroupId;

    /**
     * 用户id
     */
    public long userId;

            }
