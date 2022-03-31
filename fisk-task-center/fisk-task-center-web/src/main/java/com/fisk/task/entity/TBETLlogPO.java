package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 12:52
 * Description:
 */
@Data
@TableName("tb_etl_log")
public class TBETLlogPO {
    public int id;
    public String tablename;
    public DateTime startdate;
    public DateTime enddate;
    public int datarows;
    public int status;
    public String code;
    public String errordesc;
    public String topicName;
}
