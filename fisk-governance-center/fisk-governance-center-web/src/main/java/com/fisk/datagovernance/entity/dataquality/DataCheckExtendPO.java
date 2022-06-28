package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展表
 * @date 2022/5/16 12:35
 */
@Data
@TableName("tb_datacheck_rule_extend")
public class DataCheckExtendPO extends BasePO {
    /**
     * 数据校验规则id
     */
    public int ruleId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 实际字段名称/字段Id
     */
    public String useFieldName;

    /**
     * 字段条件值
     */
    public String fieldWhere;

    /**
     * 字段聚合波动阈值模板；
     * 字段聚合函数：
     * SUM、COUNT、AVG、MAX、MIN
     */
    public String fieldAggregate;

    /**
     * 字段校验模板；
     * 校验类型，多选逗号分割：
     * 1、唯一校验
     * 2、非空校验
     * 3、数据校验
     */
    public String checkType;

    /**
     * 数据校验类型：
     * 1、文本长度校验
     * 2、日期格式校验
     * 3、序列范围校验
     */
    public int dataCheckType;

    /**
     * 相似度对比模板；
     * 权重、比例；
     */
    public float scale;

    /**
     * 表血缘断裂校验模板；
     * 上下游血缘关系范围：
     * 1、上游 2、下游 3、上下游
     */
    public int consanguinityRange;

    /**
     * 表行数波动阈值模板；
     * 表行数；
     */
    public int tbaleRow;
}
