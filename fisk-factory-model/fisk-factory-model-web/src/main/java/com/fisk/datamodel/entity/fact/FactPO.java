package com.fisk.datamodel.entity.fact;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_fact")
@EqualsAndHashCode(callSuper = true)
public class FactPO extends BasePO {
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程id
     */
    public int businessProcessId;
    /**
     * 事实表名称
     */
    public String factTabName;
    /**
     * 事实表中文名称
     */
    public String factTableCnName;
    /**
     * 事实表描述
     */
    public String factTableDesc;
    /**
     * 事实表脚本
     */
    public String sqlScript;
    /**
     * DW发布状态 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int isPublish;
    /**
     * Doris发布状态 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int dorisPublish;
    /**
     * 数据接入应用id
     */
    public int appId;

}
