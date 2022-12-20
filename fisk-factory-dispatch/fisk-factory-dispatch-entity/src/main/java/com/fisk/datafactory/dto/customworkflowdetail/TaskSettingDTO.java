package com.fisk.datafactory.dto.customworkflowdetail;

import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class TaskSettingDTO extends BasePO {

    public String taskId;

    public String key;

    public String value;

    public String settingGroupId;
}
