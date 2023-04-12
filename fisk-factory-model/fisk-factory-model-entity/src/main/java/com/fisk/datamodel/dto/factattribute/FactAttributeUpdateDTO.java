package com.fisk.datamodel.dto.factattribute;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import lombok.Data;

import java.util.List;

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
    /**
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 预览nifi调用SQL执行语句
     */
    public String execSql;
}
