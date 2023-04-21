package com.fisk.dataaccess.dto.taskschedule;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataAccessIdsDTO {

    @ApiModelProperty(value = "应用id")
    public Long appId;

    @ApiModelProperty(value = "表id")
    public Long tableId;
    /**
     * 区分维度 事实 指标;区分数据湖表  ftp  api
     */
    @ApiModelProperty(value = "区分维度 事实 指标;区分数据湖表  ftp  api")
    public int flag;
}
