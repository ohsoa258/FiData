package com.fisk.mdm.dto.entity;

import com.fisk.mdm.enums.WhetherTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/4/2 18:17
 */
@Data
public class UpdateEntityDTO extends EntityDTO {

    @NotNull
    private Integer id;
}
