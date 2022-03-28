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
@TableName("tb_usergroup_info")
@EqualsAndHashCode(callSuper = true)
public class UsergroupInfoPO extends BasePO {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    public long id;

    /**
     * 用户组名称
     */
    public String userGroupName;

    /**
     * 用户组描述
     */
    public String userGroupDesc;

            }
