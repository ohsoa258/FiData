package com.fisk.datagovernance.vo.dataops;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 执行结果VO
 * @date 2022/4/22 12:57
 */
@Data
public class ExecuteResultVO {

    /**
     * 查询/修改
     */
    @ApiModelProperty(value = "true:查询 false:修改")
    public boolean query;

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public JSONArray dataArray;

    /**
     * 受影响行数
     */
    @ApiModelProperty(value = "受影响行数")
    public int affectedCount;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<DataOpsTableFieldVO> dataOpsTableFieldVO;
}
