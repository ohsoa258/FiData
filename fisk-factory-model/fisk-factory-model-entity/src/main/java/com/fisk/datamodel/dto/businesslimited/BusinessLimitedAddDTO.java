package com.fisk.datamodel.dto.businesslimited;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeAddDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
/**
 * @author cfk
 */
@Data
public class BusinessLimitedAddDTO extends BusinessLimitedDTO {

    @ApiModelProperty(value = "业务有限属性添加DTO列表\n")
    public List<BusinessLimitedAttributeAddDTO> businessLimitedAttributeAddDTOList;
    /**
     * 下拉框字段
     */
    @ApiModelProperty(value = "事实属性列表Dto列表")
    public List<FactAttributeListDTO> factAttributeListDtoList;
}
