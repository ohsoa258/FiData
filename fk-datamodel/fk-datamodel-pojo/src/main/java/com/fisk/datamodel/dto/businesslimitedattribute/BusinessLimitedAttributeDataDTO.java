package com.fisk.datamodel.dto.businesslimitedattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedAttributeDataDTO {
    public int id;
    /**
     * 业务限定id
     */
    public int businessLimitedId;
    /**
     *事实字段表id
     */
    public int factAttributeId;
    /**
     *计算逻辑
     */
    public String calculationLogic;
    /**
     *计算值
     */
    public String calculationValue;
}
