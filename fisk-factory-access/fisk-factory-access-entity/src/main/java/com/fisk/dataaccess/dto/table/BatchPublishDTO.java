package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BatchPublishDTO {

    @ApiModelProperty(value = "ids")
    public List<Long> ids;

    @ApiModelProperty(value = "开放传播")
    public boolean openTransmission;

    @ApiModelProperty(value = "当前用户名")
    public String currUserName;
    @ApiModelProperty(value = "表历史", required = true)
    public List<TableHistoryDTO> tableHistorys;

}
