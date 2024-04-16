package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
    /**
     * 是否启用添加
     */
    public int switchAdd;
    /**
     * 是否启用修改
     */
    public int switchUpdate;
    /**
     * 是否启用删除
     */
    public int switchDelete;

    /**
     * 是否授权(指标管理用)
     */
    public int switchAuthorization;
}
