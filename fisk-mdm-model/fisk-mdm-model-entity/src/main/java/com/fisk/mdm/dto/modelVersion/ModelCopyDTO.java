package com.fisk.mdm.dto.modelVersion;

import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/6/10 14:10
 * @Version 1.0
 */
@Data
public class ModelCopyDTO {

    private Integer id;
    /**
     * 模型Id
     */
    private Integer modelId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;
}
