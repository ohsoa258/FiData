package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/13 11:42
 */
@Data
@TableName("tb_fact")
public class FactPO extends BasePO {

    /**
     * 业务过程表id
     */
    private Integer businessProcessId;
    /**
     * 事实表中文名称
     */
    private String factTableCnName;
    /**
     * 事实表英文名称
     */
    private String factTableEnName;
    /**
     * 事实表描述
     */
    private String factTableDesc;
}
