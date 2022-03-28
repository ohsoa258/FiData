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
public class UsergroupInfoDTO {

    /**
     *
     */
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
