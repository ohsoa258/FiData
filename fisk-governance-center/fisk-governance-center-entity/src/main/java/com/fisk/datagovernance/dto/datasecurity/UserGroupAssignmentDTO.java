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
public class UserGroupAssignmentDTO {

    /**
     *
     */
    @ApiModelProperty(value = "id")
    public long id;

    /**
     * 用户组id
     */
    @ApiModelProperty(value = "用户组id")
    public long userGroupId;

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    public long userId;

}
