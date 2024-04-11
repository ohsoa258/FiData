package com.fisk.datamanagement.dto.assetschangeanalysis;

import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssetsChangeAnalysisDTO implements Serializable {

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endTime;

    /**
     * 类型 0全部 1 新增 2 修改 3 删除
     */
    @ApiModelProperty(value = "类型 0全部 1 新增 2 修改 3 删除")
    private MetadataAuditOperationTypeEnum operationType;

    /**
     * 新增线
     */
    @ApiModelProperty(value = "新增线")
    private List<LineCountDTO> addLine;

    /**
     * 删除线
     */
    @ApiModelProperty(value = "删除线")
    private List<LineCountDTO> delLine;

    /**
     * 修改线
     */
    @ApiModelProperty(value = "修改线")
    private List<LineCountDTO> updateLine;

    /**
     * 时间区间内元数据新增个数
     */
    @ApiModelProperty(value = "时间区间内元数据新增个数")
    private Integer addPercent;

    /**
     * 时间区间内元数据删除个数
     */
    @ApiModelProperty(value = "时间区间内元数据删除个数")
    private Integer delPercent;

    /**
     * 时间区间内元数据修改个数
     */
    @ApiModelProperty(value = "时间区间内元数据修改个数")
    private Integer updatePercent;

    /**
     * 时间区间内元数据变更表个数
     */
    @ApiModelProperty(value = "时间区间内元数据变更表个数")
    private Integer tblCount;

    /**
     * 时间区间内元数据变更字段个数
     */
    @ApiModelProperty(value = "时间区间内元数据变更字段个数")
    private Integer fieldCount;

    /**
     * 分类统计变更情况明细
     */
    @ApiModelProperty(value = "分类统计变更情况明细")
    private List<CategoryDetailChangesDTO> categoryDetailChanges;

}
