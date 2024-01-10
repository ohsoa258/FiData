package com.fisk.datagovernance.vo.dataops;

import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表信息
 * @date 2022/4/22 21:48
 */
@Data
public class DataOpsDataBaseVO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public DataSourceTypeEnum conType;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * 表
     */
    @ApiModelProperty(value = "表")
    public List<DataOpsDataTableVO> children;
}
