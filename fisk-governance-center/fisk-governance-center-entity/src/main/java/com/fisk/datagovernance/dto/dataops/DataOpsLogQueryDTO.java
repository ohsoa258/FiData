package com.fisk.datagovernance.dto.dataops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataops.DataOpsLogVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维日志查询DTO
 * @date 2022/4/22 11:47
 */
public class DataOpsLogQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件：支持数据库名称、数据库类型（mysql、sqlserver、cube、tabular、postgre、pg）、" +
            "执行sql、执行结果（成功/失败）、执行人")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<DataOpsLogVO> page;
}
