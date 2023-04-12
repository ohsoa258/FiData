package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据安全-智能发现-扫描结果白名单PO
 * @date 2023/2/1 12:05
 */
@Data
@TableName("tb_Intelligentdiscovery_whitelist")
public class IntelligentDiscovery_WhiteListPO extends BasePO {
    /**
     * 扫描的数据库ip
     */
    public String scanDatabaseIp;

    /**
     * 扫描的数据库名称
     */
    public String scanDatabase;

    /**
     * 扫描的数据库模式
     */
    public String scanSchema;

    /**
     * 扫描的表
     */
    public String scanTable;

    /**
     * 扫描的字段
     */
    public String scanField;

    /**
     * 有效性：1 生效中 2 已失效
     */
    public int validity;
}
