package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 13:59
 * Description:
 */
@Data
public class AtlasEntityDbTableColumnDTO extends MQBaseDTO {
public String dbId;
public String tableName;
public String createUser;
public List<AtlasEntityColumnDTO> columns;
}
