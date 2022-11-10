package com.fisk.mdm.dto.modelVersion;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/6/10 10:31
 * @Version 1.0
 */
@Data
public class ModelVersionUpdateDTO {

    @NotNull
    private Integer id;

    /**
     * 模型id
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

    /**
     * 状态: 0：打开 1：锁定 2：已提交
     */
    private Integer status;

    /**
     * 类型: 0.用户手动 1.系统 job 自动
     */
    private Integer type;
}
