package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_role_info")
public class RoleInfoPO extends BasePO {

    public String roleName;

    public String roleDesc;

}
