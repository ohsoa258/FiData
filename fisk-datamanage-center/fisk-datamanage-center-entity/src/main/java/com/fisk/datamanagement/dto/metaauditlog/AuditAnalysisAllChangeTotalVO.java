package com.fisk.datamanagement.dto.metaauditlog;

import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class AuditAnalysisAllChangeTotalVO {

    public Integer add;
    public Integer edit;
    public Integer delete;
}
