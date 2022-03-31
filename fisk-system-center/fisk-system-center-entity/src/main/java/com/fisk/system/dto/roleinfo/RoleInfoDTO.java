package com.fisk.system.dto.roleinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class RoleInfoDTO {

    public long id;
    /**
     *角色名称
     */
    public String roleName;

    /**
     *角色描述
     */
    public String roleDesc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    public String createUser;
}
