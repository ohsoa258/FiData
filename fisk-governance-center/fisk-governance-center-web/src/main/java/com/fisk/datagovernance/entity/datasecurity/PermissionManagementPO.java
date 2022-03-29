package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 权限管理表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-29 17:59:34
 */
@Data
@TableName("tb_permission_management")
@EqualsAndHashCode(callSuper = true)
public class PermissionManagementPO extends BasePO {

    /**
     * 表类型(0: 空  1: 表级  2: 行级  3: 列级)
     */
    public Integer tableType;

    /**
     * 表级or行级or列级安全表id
     */
    public Long tableId;

    /**
     * 访问类型(0: 空 1:用户组   2: 用户)
     */
    public Long accessType;

    /**
     * 访问权限(0 :空  1: 编辑  2: 只读  3: 导入  4:导出)
     */
    public Long accessPermission;

}
