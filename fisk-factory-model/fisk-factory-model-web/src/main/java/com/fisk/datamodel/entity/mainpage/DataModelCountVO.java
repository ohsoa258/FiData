package com.fisk.datamodel.entity.mainpage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataModelCountVO {

    /**
     * 维度文件夹个数
     */
    @ApiModelProperty(value = "维度文件夹个数")
    private Integer dimFolderCount;

    /**
     * dim 维度表个数
     */
    @ApiModelProperty(value = "dim 维度表个数")
    private Integer dimCount;

    /**
     * 业务文件夹个数
     */
    @ApiModelProperty(value = "业务文件夹个数")
    private Integer businessProcessCount;

    /**
     * fact 事实表个数
     */
    @ApiModelProperty(value = "fact 事实表个数")
    private Integer factCount;

    /**
     * help 帮助表个数
     */
    @ApiModelProperty(value = "help 帮助表个数")
    private Integer helpCount;

    /**
     * config 配置表个数
     */
    @ApiModelProperty(value = "config 配置表个数")
    private Integer configCount;

    /**
     * DWD个数
     */
    @ApiModelProperty(value = "DWD个数")
    private Integer dwdCount;

    /**
     * DWS个数
     */
    @ApiModelProperty(value = "DWS个数")
    private Integer dwsCount;

    /**
     * 六种类型表数据量对象
     */
    @ApiModelProperty(value = "六种类型表数据量对象")
    private DataCountVO dataCountVO;

    /**
     * 六种类型表数据量对象 TOP5
     */
    @ApiModelProperty(value = "六种类型表数据量对象 TOP5")
    private Top5DataCount top5DataCount;

}
