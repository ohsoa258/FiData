package com.fisk.task.dto.pgsql;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/17 18:28
 * Description:
 */
@Data
public class TableListDTO extends MQBaseDTO {
    public String tableName;
    public String tableAtlasId;
}
