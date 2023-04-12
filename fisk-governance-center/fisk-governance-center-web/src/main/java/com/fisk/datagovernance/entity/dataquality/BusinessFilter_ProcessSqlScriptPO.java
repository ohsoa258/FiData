package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

@Data
@TableName("tb_bizfilter_process_sql_script")
public class BusinessFilter_ProcessSqlScriptPO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public int ruleId;

    /**
     * tb_bizfilter_process_task表task_code
     */
    public String taskCode;

    /**
     * sql脚本
     */
    public String sqlScript;

    /**
     * 自定义描述
     */
    public String customDescribe;
}
