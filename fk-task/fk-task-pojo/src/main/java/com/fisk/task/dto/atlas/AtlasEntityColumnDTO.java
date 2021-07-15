package com.fisk.task.dto.atlas;

import lombok.Data;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 14:07
 * Description:
 */
@Data
public class AtlasEntityColumnDTO {
    public String columnName;
    public long columnId;
    public String dataType;
    public String isKey;
    public String comment;
    //Atlas回写的时候赋值
    public String guid;
}
