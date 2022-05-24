package com.fisk.datamodel.dto.businessarea;

import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessAreaQueryTableDTO {

    public Integer businessId;

    public OlapTableEnum tableEnum;

    public Integer tableId;

}
