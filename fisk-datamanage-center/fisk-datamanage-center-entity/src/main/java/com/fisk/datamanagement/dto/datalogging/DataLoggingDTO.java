package com.fisk.datamanagement.dto.datalogging;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-28 15:59
 * @description 数据资产报表记录数
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DataLoggingDTO {

    @ApiModelProperty(value = "总记录数")
    private Integer totalNumberOfRecords; //总记录数

    @ApiModelProperty(value = "日增量")
    private Integer dailyGain; //日增量
}
