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
    /**
     * 应用简称
     */
    public String appAbbreviation;
    public String tableId;
    /**
     * 数据同步类型；全量、增量
     */
    public String syncType;
    /**
     * 增量时间戳字段
     */
    public String syncField;
    public String createUser;
    public List<AtlasEntityColumnDTO> columns;
}
