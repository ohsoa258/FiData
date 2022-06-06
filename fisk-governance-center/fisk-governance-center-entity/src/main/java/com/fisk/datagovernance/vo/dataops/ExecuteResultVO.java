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
     * 1、select
     * 2、insert、update、delete
     * 3.truncate、drop、other
     */
    @ApiModelProperty(value = "1、select 2、insert、update、delete 3.truncate、drop、other")
    public int executeType;

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public List<Object> dataArray;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<DataOpsTableFieldVO> dataOpsTableFieldVO;

    /**
     * executeType=2才有受影响行数
     */
    @ApiModelProperty(value = "executeType=2才有受影响行数")
    public int affectedCount;

    /**
     * 执行状态 true：成功 false：失败
     */
    @ApiModelProperty(value = "执行状态 true：成功 false：失败")
    public boolean executeState;

    /**
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public Integer current;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小")
    public Integer size;

    /**
     * 总条数
     */
    @ApiModelProperty(value = "total")
    public Integer total;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "page")
    public Integer page;
}
