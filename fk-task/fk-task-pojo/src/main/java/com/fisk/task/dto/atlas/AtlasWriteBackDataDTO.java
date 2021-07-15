package com.fisk.task.dto.atlas;

import lombok.Data;

import java.util.List;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/13 13:39
 * Description:
 */
@Data
public class AtlasWriteBackDataDTO {
    public String appId;
    public String tableId;
    public String atlasTableId;
    public String dorisSelectSqlStr;
    public List<AtlasEntityColumnDTO> columnsKeys;
}
