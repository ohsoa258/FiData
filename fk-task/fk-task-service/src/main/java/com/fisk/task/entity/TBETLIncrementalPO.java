package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/3 00:43
 * Description:
 */
@Data
@TableName("tb_etl_Incremental")
public class TBETLIncrementalPO {
    public int id;
    public String object_name;
    public String enable_flag;
    public String incremental_objectivescore_batchNo;
    public DateTime incremental_objectivescore_start;
    public DateTime incremental_objectivescore_end;
}
