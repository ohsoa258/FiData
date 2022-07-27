package com.fisk.task.dto.dispatchlog;

import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class PipelTaskLogVO extends BasePO {
    @ApiModelProperty(value = "task批次号")
    public String taskTraceId;
    @ApiModelProperty(value = "绑定表的任务的id")
    public String taskId;
    @ApiModelProperty(value = "绑定表的任务的名称")
    public String taskName;
    @ApiModelProperty(value = "绑定表id")
    public String tableId;
    @ApiModelProperty(value = "绑定表名称")
    public String tableName;
    @ApiModelProperty(value = "内容")
    public String msg;
    @ApiModelProperty(value = "类型")
    public int type;
    @ApiModelProperty(value = "类型汉语")
    public String typeName;

}
