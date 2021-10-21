package com.fisk.datamodel.dto.businessprocess;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaContentDTO {

    /**
     * 业务域id
     */
    public long businessAreaId;

    /**
     * 事实表名称
     */
    public String factTableName;

}
