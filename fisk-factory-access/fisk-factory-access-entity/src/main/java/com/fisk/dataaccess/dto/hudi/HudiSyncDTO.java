package com.fisk.dataaccess.dto.hudi;

import lombok.Data;

@Data
public class HudiSyncDTO {

    private Long appId;

    /**
     * 同步方式 1全量覆盖（全部表重新同步）  2增量（只同步新表，不删除旧表）
     */
    private Integer syncType;

}
