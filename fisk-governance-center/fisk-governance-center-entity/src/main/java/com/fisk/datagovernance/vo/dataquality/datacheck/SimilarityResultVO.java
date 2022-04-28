package com.fisk.datagovernance.vo.dataquality.datacheck;

/**
 * @author dick
 * @version 1.0
 * @description 检查相似度结果VO
 * @date 2022/4/27 16:22
 */
public class SimilarityResultVO {
    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段值
     */
    public Object fieldValue;

    /**
     * 相似度
     */
    public double similaritValue;

    /**
     * 权重、比例
     */
    public int scale;
}
