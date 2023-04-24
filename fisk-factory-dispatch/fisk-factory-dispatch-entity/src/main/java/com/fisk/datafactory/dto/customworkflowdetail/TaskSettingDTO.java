package com.fisk.datafactory.dto.customworkflowdetail;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class TaskSettingDTO extends BasePO {

    @ApiModelProperty(value = "任务Id")
    public String taskId;

    @ApiModelProperty(value = "设置键")
    public String settingKey;
//    public String key;

    @ApiModelProperty(value = "值")
    public String value;

    @ApiModelProperty(value = "设置组Id")
    public String settingGroupId;
}
