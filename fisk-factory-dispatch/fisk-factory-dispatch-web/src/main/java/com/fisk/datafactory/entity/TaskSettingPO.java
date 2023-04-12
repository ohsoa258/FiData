package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_task_setting")
public class TaskSettingPO extends BasePO {

    public String taskId;

    public String settingKey;

    public String value;

    public String settingGroupId;

}
