package com.fisk.mdm.dto.stgbatch;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class StgBatchDTO {

    private Integer entityId;

    private Integer versionId;

    private String batchCode;

    private Integer totalCount;

    private Integer errorCount;

    private Integer status;

    private Integer addCount;

    private Integer updateCount;

}
