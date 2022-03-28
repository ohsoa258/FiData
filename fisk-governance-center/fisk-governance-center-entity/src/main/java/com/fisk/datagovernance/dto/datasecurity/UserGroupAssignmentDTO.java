package com.fisk.datagovernance.dto.datasecurity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


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
public class UserGroupAssignmentDTO {

    /**
     * 
     */
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
