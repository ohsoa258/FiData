package com.fisk.datamodel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    public long id;

    /**
     * 业务域名称
     */
    public String businessName;

    /**
     * 业务域描述
     */
    public String businessDes;

    /**
     * 业务需求管理员
     */
    public String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    public String businessEmail;

    /**
     * 业务域：数仓建模/分析建模表总数
     */
    public int modelNumber;
    /**
     * 数仓建模发布成功数量
     */
    public int numberPositionPublishNumber;
    /**
     * 分析建模发布成功数量
     */
    public int analysisPublishNumber;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

}
