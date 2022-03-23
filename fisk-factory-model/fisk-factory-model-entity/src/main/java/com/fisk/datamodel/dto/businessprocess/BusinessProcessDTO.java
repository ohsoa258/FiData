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
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    public int isPublish;
}
