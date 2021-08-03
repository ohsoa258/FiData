package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/2 15:26
 */
@Data
public class UserDTO {
    public Long id;
    public String downSystemName;
    public String systemInfo;
    public String userName;
    public String password;
}
