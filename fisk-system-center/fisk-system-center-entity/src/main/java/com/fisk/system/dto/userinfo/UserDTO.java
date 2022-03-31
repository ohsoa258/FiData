package com.fisk.system.dto.userinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Lock
 */
@Data
public class UserDTO {

    public Long id;

    public String email;

    public String userAccount;

    public String username;

    public String password;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    public String createUser;
    /**
     * 是否有效
     */
    public boolean valid;

}
