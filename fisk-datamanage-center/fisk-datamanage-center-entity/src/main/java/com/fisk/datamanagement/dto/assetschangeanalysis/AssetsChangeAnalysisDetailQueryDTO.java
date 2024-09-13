package com.fisk.datamanagement.dto.assetschangeanalysis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AssetsChangeAnalysisDetailQueryDTO implements Serializable {

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
     * ALL 全部
     * ADD 新增
     * EDIT 修改
     * DELETE 删除
     */
    @ApiModelProperty(value = "类型 ALL全部 ADD新增 EDIT修改 DELETE删除")
    private MetadataAuditOperationTypeEnum operationType;

    @ApiModelProperty(value = "元数据类型")
    private EntityTypeEnum entityType;

    @ApiModelProperty(value = "当前页")
    private Integer currentPage;

    @ApiModelProperty(value = "每页条数")
    private Integer size;

    /**
     * 类型
     * RDBMS_TABLE 表
     * RDBMS_COLUMN 字段
     */
    @ApiModelProperty(value = "类型 RDBMS_TABLE表 RDBMS_COLUMN字段")
    private ClassificationTypeEnum serviceType;

}
