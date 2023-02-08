package com.fisk.datamodel.entity.dimension;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_dimension")
@Data
@EqualsAndHashCode(callSuper = true)
public class DimensionPO extends BasePO {

    /**
     * 维度文件夹id
     */
    public int dimensionFolderId;
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 维度名称
     */
    public String dimensionCnName;
    /**
     * 维度逻辑表名称
     */
    public String dimensionTabName;
    /**
     * 维度描述
     */
    public String dimensionDesc;
    /**
     * 是否共享
     */
    public Boolean share;
    /**
     * DW发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int isPublish;
    /**
     * Doris发布状态 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public int dorisPublish;
    /**
     * 维度sql脚本
     */
    public String sqlScript;
    /**
     * 是否为日期维度表
     */
    public Boolean isDimDateTbl;
    /**
     * 是否为时间表
     */
    public Boolean timeTable;
    /**
     * 开始时间
     */
    public String startTime;
    /**
     * 截止时间
     */
    public String endTime;
    /**
     * 数据接入应用id
     */
    // public Integer appId;

    /**
     * 数据接入数据来源库id
     */
    public Integer dataSourceId;
    /**
     * 临时表名称
     */
    public String prefixTempName;
    /**
     * 维度key数据同步脚本
     */
    public String dimensionKeyScript;
}
