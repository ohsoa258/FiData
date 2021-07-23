package com.fisk.system.dto;

import lombok.Data;

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

    public Date createTime;
}
