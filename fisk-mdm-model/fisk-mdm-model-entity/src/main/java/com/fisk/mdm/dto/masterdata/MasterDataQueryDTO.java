package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.List;

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

    /**
     * 是否导出
     */
    private Boolean export;

    /**
     * 属性组
     */
    private List<Integer> attributeGroups;

}
