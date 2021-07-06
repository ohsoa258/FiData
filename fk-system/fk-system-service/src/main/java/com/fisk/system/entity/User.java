package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.constants.RegexPatterns;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author Lock
 *
 * 确保对象比较时部分属性值不同,Lombok判定对象不相等
 */
@TableName("tb_user_info")
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {

    @TableId
    private Long id;

    @NotNull(message = "用户名不能为空")
    private String username;

    @NotNull(message = "密码不能为空")
    @Pattern(regexp = RegexPatterns.PASSWORD_REGEX, message = "密码格式不正确")
    private String password;

}
