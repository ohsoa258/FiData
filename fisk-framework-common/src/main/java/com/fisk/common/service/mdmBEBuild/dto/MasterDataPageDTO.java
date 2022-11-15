package com.fisk.common.service.mdmBEBuild.dto;

import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-05-17 16:42
 */
@Data
public class MasterDataPageDTO extends PageDataDTO {

    private Integer versionId;

    private String columnNames;

    private Integer isValidity;
    /**
     * 是否导出
     */
    private Boolean export;

    /**
     * 筛选条件
     */
    private String conditions;

}
