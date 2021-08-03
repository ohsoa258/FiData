package com.fisk.dataservice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public String createTime;
}
