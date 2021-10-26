package com.fisk.datamodel.dto.businessLimited;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedUpdateDTO {
    public long id;
    /**
     * 业务限定名称
     */
    public String limitedName;
    /**
     * 业务限定描述
     */
    public String limitedDes;
}
