package com.fisk.chartvisual.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author gy
 */
@Data
public class ChartPropertyVO {
    public int id;
    public Long fid;
    public String name;
    public String content;
    public String details;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public ChartQueryTypeEnum chartType;
    public String image;

}
