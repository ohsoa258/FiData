package com.fisk.datamodel.dto.fact;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactDTO {
    public int id;
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程名称
     */
    public String factName;
    /**
     * 事实表名称
     */
    public String factTableName;
    /**
     * 业务过程描述
     */
    public String factDesc;
}
