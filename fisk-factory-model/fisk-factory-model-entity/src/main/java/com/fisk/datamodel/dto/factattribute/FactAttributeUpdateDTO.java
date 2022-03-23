package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeUpdateDTO {
    public int id;
    public String factFieldCnName;
    public String factFieldType;
    public int factFieldLength;
    public String factFieldDes;
    public String factFieldEnName;
}
