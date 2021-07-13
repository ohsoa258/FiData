package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_role_service_assignment")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleServiceAssignmentPO extends BasePO {

    public int roleId;

    public int serviceId;

}
