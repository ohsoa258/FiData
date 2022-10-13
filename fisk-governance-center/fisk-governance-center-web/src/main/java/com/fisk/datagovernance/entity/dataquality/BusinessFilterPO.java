package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 业务清洗规则表
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_bizfilter_rule")
public class BusinessFilterPO extends BasePO
{
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 数据源表主键id
     */
    public int datasourceId;

    /**
     * 规则名称
     */
    public String ruleName;

    /**
     * 表名称/表Id
     */
    public String tableUnique;

    /**
     * 表类型 1：表  2：视图
     */
    public int tableType;

    /**
     * 表业务类型 1：dw维度表、2：dw事实表、3、doris维度表  4、doris事实表 5、宽表
     */
    public int tableBusinessType;

    /**
     * 生成规则（清洗脚本）
     */
    public String createRule;

    /**
     * 规则执行顺序
     */
    public int ruleSort;

    /**
     * 规则状态
     */
    public int ruleState;
}


