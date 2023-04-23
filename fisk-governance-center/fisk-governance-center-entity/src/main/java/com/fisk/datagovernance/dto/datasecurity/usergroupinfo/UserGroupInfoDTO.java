package com.fisk.datagovernance.dto.datasecurity.usergroupinfo;

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
public class UserGroupInfoDTO {

    /**
     *
     */
    @ApiModelProperty(value = "ID")
    public long id;

    /**
     * 用户组名称
     */
    @ApiModelProperty(value = "用户组名称")
    public String userGroupName;

    /**
     * 用户组描述
     */
    @ApiModelProperty(value = "用户组描述")
    public String userGroupDesc;

}
