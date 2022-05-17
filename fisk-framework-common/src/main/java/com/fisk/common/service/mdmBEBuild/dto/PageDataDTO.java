package com.fisk.common.service.mdmBEBuild.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-05-16 22:07
 */
@Data
public class PageDataDTO {

    private String tableName;

    private Integer pageIndex;

    private Integer pageSize;

    /**
     * 批次号
     */
    private String batchCode;

    /**
     * 上传状态
     */
    private List<Integer> status;

    /**
     * 上传逻辑
     */
    private List<Integer> syncType;


}
