package com.fisk.mdm.dto.masterdata;

import lombok.Data;

/**
 * @author JianWenYang
 * date 2022/05/07 11:06
 */
@Data
public class ImportDataQueryDTO {

    public int pageSize;

    public int pageIndex;

    public int entityId;

    public String key;

}
