package com.fisk.datagovernance.dto.datasecurity;

import lombok.Data;

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
public class TablesecurityConfigDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * 缺省设置(0: 所有可读  1: 所有不可读)
     */
    public long defaultConfig;

    /**
     * 数据源id
     */
    public long datasourceId;

    /**
     * 表id
     */
    public long tableId;

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
