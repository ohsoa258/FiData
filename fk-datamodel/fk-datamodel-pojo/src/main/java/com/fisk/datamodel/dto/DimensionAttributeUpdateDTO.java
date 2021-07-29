package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeUpdateDTO {
    public int id;
    public String dimensionFieldCnName;
    public String dimensionFieldType;
    public int dimensionFieldLength;
    public String dimensionFieldDes;
    public String dimensionFieldEnName;
}
