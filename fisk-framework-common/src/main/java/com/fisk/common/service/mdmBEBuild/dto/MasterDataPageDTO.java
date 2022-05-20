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

    /**
     * 导出
     */
    //private Boolean export;

}
