package com.fisk.mdm.dto.viwGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:41
 * @Version 1.0
 */
@Data
public class ViwGroupDetailsDTO {

    private Integer id;
    @NotNull
    private Integer groupId;
    private Integer attributeId;
    private String aliasName;
}
