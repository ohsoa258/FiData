package com.fisk.task.dto.pgsql;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/15 10:44
 * Description:
 */
@Data
public class PgsqlDelTableDTO extends MQBaseDTO {
    public List<String> tableList;
}
