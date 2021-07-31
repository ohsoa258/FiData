package com.fisk.datamodel.dto.businessprocess;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessDTO {
    public int id;
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程名称
     */
    public String businessProcessCnName;
    /**
     * 业务过程英文名称
     */
    public String businessProcessEnName;
    /**
     * 业务过程描述
     */
    public String businessProcessDesc;
}
