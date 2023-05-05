package com.fisk.system.vo.roleinfo;

import com.fisk.system.enums.UserTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-03-31
 * @Description: 用户
 */
@Data
public class UserInfoVo {

    /**
     * 行号id
     */
    @ApiModelProperty(value = "rowid")
    public int rowid;

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    public String username;

    /**
     * 标识类型
     */
    @ApiModelProperty(value = "标识类型")
    public int type = UserTypeEnum.USER.getValue();

    /**
     * 标识名称
     */
    @ApiModelProperty(value = "标识名称")
    public String typeName = UserTypeEnum.USER.getName();
}
