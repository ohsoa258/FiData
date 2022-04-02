package com.fisk.datamodel.dto.businesslimited;

import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedDataDTO {
    public long id;
    /**
     * 业务限定名称
     */
    public String limitedName;
    /**
     * 业务限定描述
     */
    public String limitedDes;
    /**
     * 业务限定字段列表
     */
    public List<BusinessLimitedAttributeDataDTO> dto;

}
