package com.fisk.task.vo.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Data
public class GanttChartTaskVO {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date startDateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date endDateTime;
}
