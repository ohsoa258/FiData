package com.fisk.dataservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public String createUser;
}
