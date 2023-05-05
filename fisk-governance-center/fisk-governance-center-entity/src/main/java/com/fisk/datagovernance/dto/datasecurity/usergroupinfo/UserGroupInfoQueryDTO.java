package com.fisk.datagovernance.dto.datasecurity.usergroupinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserGroupInfoQueryDTO {

    /**
     *当前页数
     */
    @ApiModelProperty(value = "当前页数")
    public int page;
    /**
     *每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int size;
    /**
     *用户组名称
     */
    @ApiModelProperty(value = "用户组名称")
    public String name;

}
