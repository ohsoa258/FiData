package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.constants.RegexPatterns;
import com.fisk.common.core.baseObject.entity.BasePO;
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
@EqualsAndHashCode(callSuper = true)
public class UserPO extends BasePO {

    @NotNull(message = "用户名不能为空")
    public String username;

    @NotNull(message = "密码不能为空")
    @Pattern(regexp = RegexPatterns.PASSWORD_REGEX, message = "密码格式不正确")
    public String password;

    public String email;

    public String userAccount;
    /**
     * 是否有效
     */
    public boolean valid;


}
