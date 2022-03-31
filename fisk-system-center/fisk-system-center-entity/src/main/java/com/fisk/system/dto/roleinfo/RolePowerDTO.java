package com.fisk.system.dto.roleinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Data
public class RolePowerDTO {

    public int id;
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
}
