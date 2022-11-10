package com.fisk.mdm.dto.attributelog;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/6/14 17:12
 * @Version 1.0
 */
@Data
public class AttributeLogUpdateDTO extends AttributeLogDTO {

    @NotNull
    private Integer id;
}
