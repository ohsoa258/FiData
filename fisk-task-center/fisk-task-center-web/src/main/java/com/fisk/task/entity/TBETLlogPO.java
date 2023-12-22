package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime startdate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime enddate;
    public int datarows;
    public int status;
    public String code;
    public String errordesc;
    public String topicName;
    public String querySql;
    @TableField(value = "createtime", fill = FieldFill.INSERT)
    public LocalDateTime createtime;
}
