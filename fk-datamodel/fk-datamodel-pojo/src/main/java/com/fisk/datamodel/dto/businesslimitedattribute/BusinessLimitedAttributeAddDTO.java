package com.fisk.datamodel.dto.businesslimitedattribute;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BusinessLimitedAttributeAddDTO extends BusinessLimitedAttributeDTO {
    /**
     * 操作类型,状态:0原有,1新增,2修改,3删除
     */
    public int funcType;


}
