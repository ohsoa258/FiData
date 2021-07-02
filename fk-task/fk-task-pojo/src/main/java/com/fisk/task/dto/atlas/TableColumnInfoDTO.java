package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author DennyHui
 * CreateTime: 2021/7/1 15:08
 * Description:
 */
@Data
public class TableColumnInfoDTO extends MQBaseDTO {
    public String columnName;
    public String isKey;
    public String type;
    public String comment;
}
