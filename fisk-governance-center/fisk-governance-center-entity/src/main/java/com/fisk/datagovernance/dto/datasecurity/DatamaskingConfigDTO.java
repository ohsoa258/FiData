package com.fisk.datagovernance.dto.datasecurity;

import lombok.Data;

/**
 * <p>
 * 数据脱敏字段配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
@Data
public class DatamaskingConfigDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * 数据源id
     */
    public long datasourceId;

    /**
     * 表id
     */
    public long tableId;

    /**
     * 脱敏字段
     */
    public String fieldName;

    /**
     * 脱敏类型
     */
    public long maskingType;

    /**
     * 保留前几位文本
     */
    public long numberDigits;

    /**
     * 内容替换
     */
    public String contentReplace;

    /**
     * 加密方式
     */
    public long encryptType;

    /**
     * 是否有效
     */
    public Boolean valid;
}
