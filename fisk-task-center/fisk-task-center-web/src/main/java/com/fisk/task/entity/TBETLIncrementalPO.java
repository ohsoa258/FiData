package com.fisk.task.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/3 00:43
 * Description:
 */
@Data
@TableName("tb_etl_Incremental")
public class TBETLIncrementalPO implements Serializable {
    @TableId(type = IdType.AUTO)
    public int id;
    public String objectName;
    public String enableFlag;
    public String incrementalObjectivescoreBatchno;
    public Date incrementalObjectivescoreStart;
    public Date incrementalObjectivescoreEnd;
}
