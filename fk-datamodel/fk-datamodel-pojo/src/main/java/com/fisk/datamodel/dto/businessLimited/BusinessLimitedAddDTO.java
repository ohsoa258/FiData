package com.fisk.datamodel.dto.businessLimited;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeAddDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import lombok.Data;

import java.util.List;
/**
 * @author cfk
 */
@Data
public class BusinessLimitedAddDTO extends BusinessLimitedDTO {
    public List<BusinessLimitedAttributeAddDTO> businessLimitedAttributeAddDTOList;
    /**
     * 下拉框字段
     */
    public List<FactAttributeListDTO> factAttributeListDtoList;
}
