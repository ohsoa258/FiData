package com.fisk.datagovernance.vo.dataquality.datasource;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据源实例
 * @date 2022/4/13 11:24
 */
@Data
public class DataExampleSourceVO {
    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    public DataSourceTypeEnum conType;

    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    public String name;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    public String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * 实例下数据库
     */
    @ApiModelProperty(value = "实例下数据库")
    public List<DataBaseSourceVO> children;
}
