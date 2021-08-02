package com.fisk.dataservice.vo;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/30 14:59
 */
@Data
public class UserVO {
    public Long id;
    public String configureName;
    public String userName;
    public String password;
    public String createUser;
    public String createTime;
}
