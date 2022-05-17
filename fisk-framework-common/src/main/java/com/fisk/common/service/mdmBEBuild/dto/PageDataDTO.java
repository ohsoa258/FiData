package com.fisk.common.service.mdmBEBuild.dto;

import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-05-16 22:07
 */
@Data
public class PageDataDTO {

    private String tableName;

    private Integer pageIndex;

    private Integer pageSize;
}
