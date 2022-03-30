package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
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
@TableName("tb_tablesecurity_config")
@EqualsAndHashCode(callSuper = true)
public class TablesecurityConfigPO extends BasePO {

    /**
     * 缺省设置(0: 空 1: 所有可读  2: 所有不可读--此时无论权限如何,当前表不可读取),默认值: 1
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
     * 访问类型(0:用户组   1: 用户)
     */
    public long accessType;

    /**
     * 展示名称(用户组名or用户名)     存id
     */
    public long userGroupId;

    /**
     * 访问权限(0: 编辑  1: 只读  2: 导入  3:导出)
     */
    public long accessPermission;
}
