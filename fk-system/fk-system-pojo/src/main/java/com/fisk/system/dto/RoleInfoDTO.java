package com.fisk.system.dto;

import com.fisk.common.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

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

    public Date createTime;

    public String createUser;
}
