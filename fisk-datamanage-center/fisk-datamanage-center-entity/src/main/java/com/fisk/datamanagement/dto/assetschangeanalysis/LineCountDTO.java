package com.fisk.datamanagement.dto.assetschangeanalysis;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class LineCountDTO {

    /**
     * 日期
     */
    @ApiModelProperty(value ="日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime date;

    /**
     * 个数
     */
    @ApiModelProperty(value ="个数")
    private Integer count;

}
