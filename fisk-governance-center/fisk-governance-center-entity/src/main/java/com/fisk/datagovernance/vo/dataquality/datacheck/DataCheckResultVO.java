package com.fisk.datagovernance.vo.dataquality.datacheck;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验结果
 * @date 2022/4/12 18:17
 */
@Data
public class DataCheckResultVO
{
    /**
     * 检查的库
     */
    public String checkDataBase;

    /**
     * 检查的表
     */
    public String checkTable;

    /**
     * 检查的字段
     */
    public String checkField;

    /**
     * 检查的类型
     */
    public String checkType;

    /**
     * 检查的描述
     */
    public String checkDesc;

    /**
     * 检查的结果
     */
    public Object checkResult;
}
