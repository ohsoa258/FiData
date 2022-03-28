package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@TableName("tb_datamasking_config")
@EqualsAndHashCode(callSuper = true)
public class DatamaskingConfigPO extends BasePO {

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
     * 生成秘钥(16位字母+数字的随机值)
     */
    public String secretKey;

    /**
     * 是否有效
     */
    public boolean valid;
}
