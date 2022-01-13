package com.fisk.dataservice.vo.datasource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.dataservice.enums.ChartQueryTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description ChartPropertyVO
 * @date 2022/1/6 14:51
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
    public String backgroundImage;

}
