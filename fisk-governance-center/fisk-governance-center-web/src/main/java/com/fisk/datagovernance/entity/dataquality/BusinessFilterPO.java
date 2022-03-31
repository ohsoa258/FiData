package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 业务清洗
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_bizfilter_module")
public class BusinessFilterPO extends BasePO
{
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 数据源id
     */
    public int datasourceId;

    /**
     * 数据源类型
     */
    public int datasourceType;

    /**
     * 组件名称
     */
    public String moduleName;

    /**
     * 清洗步骤
     */
    public int filterStep;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 前置表名称
     */
    public String proTableName;

    /**
     * 组件规则（清洗脚本）
     */
    public String moduleRule;

    /**
     * 运行时间表达式
     */
    public String runTimeCron;

    /**
     * 组件执行顺序
     */
    public int moduleExecSort;

    /**
     * 组件状态
     */
    public int moduleState;
}


