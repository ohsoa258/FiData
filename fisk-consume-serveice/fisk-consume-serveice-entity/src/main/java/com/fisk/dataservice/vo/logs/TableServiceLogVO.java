package com.fisk.dataservice.vo.logs;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表服务日志VO
 * @date 2023/1/10 14:50
 */
@Data
public class TableServiceLogVO {
    /**
     * 当前页，起始页为第一页
     */
    @ApiModelProperty(value = "当前页")
    public int current;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小")
    public int size;

    /**
     * 总条数
     */
    @ApiModelProperty(value = "total")
    public int total;

    /**
     * 总页数
     */
    @ApiModelProperty(value = "page")
    public int page;

    /**
     * 查询数据集
     */
    @ApiModelProperty(value = "查询数据集")
    public List<TableServiceLogDetailVO> dataArray;
}
