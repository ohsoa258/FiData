package com.fisk.mdm.dto.viwGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:42
 * @Version 1.0
 */
@Data
public class ViwGroupUpdateDTO {

    @NotNull
    private Integer id;
    private Integer entityId;
    private String name;
    private String details;
}
