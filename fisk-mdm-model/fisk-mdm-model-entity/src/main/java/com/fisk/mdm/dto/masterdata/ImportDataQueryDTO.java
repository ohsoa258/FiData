package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * date 2022/05/07 11:06
 */
@Data
public class ImportDataQueryDTO {

    private Integer pageSize;

    private Integer pageIndex;

    private Integer entityId;

    private String key;

    /**
     * 上传状态
     */
    private List<Integer> status;

    /**
     * 上传逻辑
     */
    private List<Integer> syncType;

}
