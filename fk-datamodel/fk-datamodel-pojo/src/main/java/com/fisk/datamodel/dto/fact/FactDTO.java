package com.fisk.datamodel.dto.fact;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FactDTO {
    public int id;
    /**
     * 业务过程id
     */
    public int businessProcessId;
    /**
     * 事实表名称
     */
    public String factTableName;
    /**
     * 事实表描述
     */
    public String factTableDesc;
}
