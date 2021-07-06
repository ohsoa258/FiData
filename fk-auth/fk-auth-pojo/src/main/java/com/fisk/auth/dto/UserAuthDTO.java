package com.fisk.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lock
 *
 * 用于接收白泽登录对象
 */
@Data
@NoArgsConstructor
public class UserAuthDTO {

    private String username;
    private String password;

}


