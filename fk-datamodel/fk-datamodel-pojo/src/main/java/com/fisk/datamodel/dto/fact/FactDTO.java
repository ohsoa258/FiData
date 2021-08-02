package com.fisk.datamodel.dto.fact;

import lombok.Data;

import java.time.LocalDateTime;

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
     * 事实表中文名称
     */
    public String factTableCnName;
    /**
     * 事实表英文名称
     */
    public String factTableEnName;
    /**
     * 事实表描述
     */
    public String factTableDesc;

}
