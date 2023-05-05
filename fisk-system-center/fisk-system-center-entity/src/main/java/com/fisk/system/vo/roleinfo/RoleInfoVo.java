package com.fisk.system.vo.roleinfo;

import com.fisk.system.enums.UserTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-03-31
 * @Description: 角色
 */
@Data
public class RoleInfoVo {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 角色名
     */
    @ApiModelProperty(value = "角色名")
    public String roleName;

    /**
     * 标识类型
     */
    @ApiModelProperty(value = "标识类型")
    public int type = UserTypeEnum.ROLE.getValue();

    /**
     * 标识名称
     */
    @ApiModelProperty(value = "标识名称")
    public String typeName = UserTypeEnum.ROLE.getName();
    /**
     * 用户列表
     */
    @ApiModelProperty(value = "用户列表")
    private List<UserInfoVo> userInfoVos;
}
