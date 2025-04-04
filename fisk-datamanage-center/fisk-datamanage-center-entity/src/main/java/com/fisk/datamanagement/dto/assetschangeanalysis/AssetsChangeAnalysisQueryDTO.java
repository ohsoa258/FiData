package com.fisk.datamanagement.dto.assetschangeanalysis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.enums.system.AuditServiceTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class AssetsChangeAnalysisQueryDTO implements Serializable {

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String endTime;

    /**
     * 类型
     * RDBMS_TABLE 表
     * RDBMS_COLUMN 字段
     */
    @ApiModelProperty(value = "类型:RDBMS_TABLE表 RDBMS_COLUMN字段")
    private EntityTypeEnum entityType;

    /**
     * 服务类型 ClassificationTypeEnum
     */
    @ApiModelProperty(value = "服务类型:ClassificationTypeEnum")
    private ClassificationTypeEnum serviceType;

}
