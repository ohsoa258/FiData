package com.fisk.common.core.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author gy
 */
@Data
@AllArgsConstructor(staticName = "of")
public class UserInfo {
    public UserInfo() {

    }

    public Long id;
    public String username;
    public String token;
}
