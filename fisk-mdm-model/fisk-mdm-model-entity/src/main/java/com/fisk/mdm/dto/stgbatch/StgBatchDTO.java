package com.fisk.mdm.dto.stgbatch;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class StgBatchDTO {

    public int entityId;

    public int versionId;

    public String batchCode;

    public int totalCount;

    public int errorCount;

    public int status;

}
