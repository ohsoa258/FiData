package com.fisk.mdm.dto.masterdatalog;

import com.fisk.mdm.dto.masterdata.MasterDataBaseDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataLogQueryDTO extends MasterDataBaseDTO {

    private Integer pageIndex;

    private Integer pageSize;

    private String code;

}
