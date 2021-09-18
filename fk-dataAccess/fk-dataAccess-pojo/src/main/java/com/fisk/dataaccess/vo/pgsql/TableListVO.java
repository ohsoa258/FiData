package com.fisk.dataaccess.vo.pgsql;

import lombok.Data;

/**
 * <p>
 *      表数组
 * </p>
 * @author Lock
 */
@Data
public class TableListVO {
    /**
     * 用户id
     */
    public Long userId;
    /**
     * 回写的表名(存放在tb_nifi_setting)
     */
    public String nifiSettingTableName;
    /**
     * atlasId
     */
    public String tableAtlasId;
    /**
     * 物理表id
     */
    public Long tableId;
    /**
     * nifi流程回写的物理表组件
     */
    public String tableComponentId;
}
