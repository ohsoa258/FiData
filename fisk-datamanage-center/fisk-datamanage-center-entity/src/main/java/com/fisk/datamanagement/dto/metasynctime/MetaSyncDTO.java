package com.fisk.datamanagement.dto.metasynctime;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class MetaSyncDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public Long id;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime updateTime;

    /**
     * ClassificationTypeEnum
     * 服务类型：
     * (1, 数据接入),
     * (2, 数仓建模)，
     * (3, API网关服务),
     * (4, 数据库分发服务),
     * (5, 数据分析试图服务),
     * (6, 主数据),
     * (7,外部数据源)
     */
    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    /**
     * 完成状态（1成功，2失败）
     */
    @ApiModelProperty(value = "完成状态 （1成功 2失败 3同步中）")
    private Integer status;

}
