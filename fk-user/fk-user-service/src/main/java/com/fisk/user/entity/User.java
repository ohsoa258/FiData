package com.fisk.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.constants.RegexPatterns;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author: Lock
 * @data: 2021/5/14 16:31
 */
@TableName("tb_user")
@Data
@EqualsAndHashCode(callSuper = false) // 确保对象比较时部分属性值不同,Lombok判定对象不相等
public class User extends BaseEntity {

    @TableId
    private Long id;

    @NotNull(message = "用户名不能为空")
//    @Pattern(regexp = , message = "用户名格式不正确")
    private String username;

    @NotNull(message = "密码不能为空")
    // 密码正则。4~32位的字母、数字、下划线
    @Pattern(regexp = RegexPatterns.PASSWORD_REGEX, message = "密码格式不正确")
    private String password;

    @NotNull(message = "密码不能为空")
    //手机号正则
    @Pattern(regexp = RegexPatterns.PHONE_REGEX, message = "手机号格式不正确")
    private String phone;


}
