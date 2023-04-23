package com.fisk.datagovernance.dto.datasecurity.usergroupinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserGroupInfoDropDTO {

    @ApiModelProperty(value = "ID")
    public long id;

    @ApiModelProperty(value = "用户组名称")
    public String userGroupName;

}
