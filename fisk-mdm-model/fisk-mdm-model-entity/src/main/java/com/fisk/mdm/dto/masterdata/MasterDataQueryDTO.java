package com.fisk.mdm.dto.masterdata;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataQueryDTO extends MasterDataBaseDTO {

    /**
     * 当前页数
     */
    private Integer pageIndex;

    /**
     * 每页条数
     */
    private Integer pageSize;

}
