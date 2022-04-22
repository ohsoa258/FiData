package com.fisk.datagovernance.vo.dataops;

import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据源实例
 * @date 2022/4/22 21:47
 */
@Data
public class DataOpsSourceVO {
    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public DataSourceTypeEnum conType;

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
    public List<DataOpsDataBaseVO> children;
}
