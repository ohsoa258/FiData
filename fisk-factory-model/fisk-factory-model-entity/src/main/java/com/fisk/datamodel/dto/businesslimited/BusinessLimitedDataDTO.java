package com.fisk.datamodel.dto.businesslimited;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedDataDTO {
    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 业务限定名称
     */
    @ApiModelProperty(value = "业务限定名称")
    public String limitedName;
    /**
     * 业务限定描述
     */
    @ApiModelProperty(value = "业务限定描述")
    public String limitedDes;
    /**
     * 业务限定字段列表
     */
    @ApiModelProperty(value = "业务限定字段列表")
    public List<BusinessLimitedAttributeDataDTO> dto;

}
