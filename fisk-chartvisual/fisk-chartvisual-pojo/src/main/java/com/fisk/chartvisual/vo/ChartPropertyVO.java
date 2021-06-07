package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class ChartPropertyVO {
    public int id;
    public String name;
    public String content;
    public String details;
    public LocalDateTime createTime;
    public ChartQueryTypeEnum chartType;
}
