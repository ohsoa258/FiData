package com.fisk.task.vo.tableservice;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wangjian
 * @Date: 2023-07-18
 * @Description:
 */
@Data
public class TableGanttChartTaskVO {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date startDateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date endDateTime;
}
