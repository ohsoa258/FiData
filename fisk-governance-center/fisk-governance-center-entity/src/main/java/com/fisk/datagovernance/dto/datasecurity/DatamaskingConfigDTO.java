package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "主键,修改时必传", required = true)
    public long id;

    @ApiModelProperty(value = "数据源id", required = true)
    public String datasourceId;

    @ApiModelProperty(value = "表id", required = true)
    public String tableId;

    @ApiModelProperty(value = "脱敏字段", required = true)
    public String fieldName;

    @ApiModelProperty(value = "脱敏类型(0: 保留  1:值加密)", required = true)
    public Long maskingType;

    @ApiModelProperty(value = "保留前几位文本", required = true)
    public Long numberDigits;

    @ApiModelProperty(value = "内容替换", required = true)
    public String contentReplace;

    @ApiModelProperty(value = "加密方式(0: 对称加密  1: 非对称加密)", required = true)
    public Long encryptType;

    @ApiModelProperty(value = "生成秘钥(16位字母+数字的随机值)", required = true)
    public String secretKey;

    @ApiModelProperty(value = "是否有效(添加时默认有效)", required = true)
    public Boolean valid;
}
