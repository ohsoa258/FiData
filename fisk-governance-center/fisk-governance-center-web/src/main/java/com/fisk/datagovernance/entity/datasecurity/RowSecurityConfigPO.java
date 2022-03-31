package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 数据脱敏字段配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Data
@TableName("tb_rowsecurity_config")
@EqualsAndHashCode(callSuper = true)
public class RowSecurityConfigPO extends BasePO {

    /**
     * 缺省设置(0: 所有可读  1: 所有不可读)
     */
    public long defaultConfig;

    /**
     * 数据源id
     */
    public String datasourceId;

    /**
     * 表id
     */
    public String tableId;

    /**
     * 权限名称
     */
    public String permissionsName;

    /**
     * 权限描述
     */
    public String permissionsDes;

    /**
     * 是否有效
     */
    public boolean valid;
}
