package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDataDTO {

    public long id;
    /**
     * 事实字段中文名称
     */
    public String factFieldCnName;
    /**
     * 事实字段英文名称
     */
    public String factFieldEnName;
    /**
     * 事实字段描述
     */
    public String factFieldDes;

}
