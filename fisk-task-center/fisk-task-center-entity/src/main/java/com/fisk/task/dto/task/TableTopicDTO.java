package com.fisk.task.dto.task;

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
public class TableTopicDTO  extends BasePO {
    public int tableId;
    public int tableType;
    public String topicName;
    public int topicType;
    public Integer componentId;
}
