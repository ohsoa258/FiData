package com.fisk.datamodel.dto.businessarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class BusinessPageResultDTO {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * 业务域名称
     */
    @ApiModelProperty(value = "业务域名称")
    public String businessName;

    /**
     * 业务域描述
     */
    @ApiModelProperty(value = "业务域描述")
    public String businessDes;

    /**
     * 业务需求管理员
     */
    @ApiModelProperty(value = "业务需求管理员")
    public String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    @ApiModelProperty(value = "应用负责人邮箱")
    public String businessEmail;

    /**
     * 业务域：数仓建模/分析建模表总数
     */
    @ApiModelProperty(value = "业务域：数仓建模/分析建模表总数")
    public int modelNumber;
    /**
     * 数仓建模发布成功数量
     */
    @ApiModelProperty(value = "数仓建模发布成功数量")
    public int numberPositionPublishNumber;
    /**
     * 分析建模总数量
     */
    @ApiModelProperty(value = "分析建模总数量")
    public int analysisNumber;
    /**
     * 分析建模发布成功数量
     */
    @ApiModelProperty(value = "分析建模发布成功数量")
    public int analysisPublishNumber;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    /**
     * 当前应用下表总数
     */
    @ApiModelProperty(value = "当前应用下表总数")
    public Integer tblCount;

    /**
     * 当前应用下维度总数
     */
    @ApiModelProperty(value = "当前应用下维度表总数")
    public Integer dimCount;

    /**
     * 当前应用下事实表总数 fact config help
     */
    @ApiModelProperty(value = "当前应用下事实表总数")
    public Integer factCount;

    /**
     * 当前应用下dwd表总数
     */
    @ApiModelProperty(value = "当前应用下事实表总数")
    public Integer dwdCount;

    /**
     * 当前应用下dws表总数
     */
    @ApiModelProperty(value = "当前应用下事实表总数")
    public Integer dwsCount;

    /**
     * 数仓连接类型
     */
    @ApiModelProperty(value = "数仓连接类型")
    public DataSourceTypeEnum dwType;

}
