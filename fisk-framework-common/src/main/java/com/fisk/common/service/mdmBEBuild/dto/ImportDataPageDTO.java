package com.fisk.common.service.mdmBEBuild.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-05-17 16:35
 */
@Data
public class ImportDataPageDTO extends PageDataDTO {

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
