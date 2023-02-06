package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-扫描配置PO
 * @date 2023/2/1 12:04
 */
@Data
@TableName("tb_Intelligentdiscovery_scan")
public class IntelligentDiscovery_ScanPO extends BasePO {
    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    public int ruleId;

    /**
     * FiData数据源ID
     */
    public int datasourceId;

    /**
     * 扫描的数据库模式
     */
    public String scanSchema;

    /**
     * 扫描的表
     */
    public String scanTable;
}
