package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import lombok.Data;

import java.util.List;

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
    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
}
