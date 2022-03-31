package com.fisk.system.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ChangePasswordDTO {
    public int id;
    /**
     * 原密码
     */
    public String originalPassword;
    public String password;
}
