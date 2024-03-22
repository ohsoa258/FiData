package com.fisk.datamanagement.dto.metaauditlog;

import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class AuditAnalysisDayChangeTotalVO {
    public Integer day;
    public Integer add;
    public Integer edit;
    public Integer delete;

}
