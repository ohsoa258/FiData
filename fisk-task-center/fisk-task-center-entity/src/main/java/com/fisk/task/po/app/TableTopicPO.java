package com.fisk.task.po.app;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_table_topic")
public class TableTopicPO extends BasePO {
    public int tableId;
    public int tableType;
    public String topicName;
    public int topicType;
    public Integer componentId;

}
