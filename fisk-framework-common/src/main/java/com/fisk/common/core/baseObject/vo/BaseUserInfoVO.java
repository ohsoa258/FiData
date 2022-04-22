package com.fisk.common.core.baseObject.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 * @version 1.0
 * @description 用户信息基类
 * @date 2022/4/22 12:39
 */
@Data
public class BaseUserInfoVO {

    @ApiModelProperty(value = "创建人")
    public String createUser;

    @ApiModelProperty(value = "更新人")
    public String updateUser;
}
