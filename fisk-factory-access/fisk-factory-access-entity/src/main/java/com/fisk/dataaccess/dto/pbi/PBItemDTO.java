package com.fisk.dataaccess.dto.pbi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class PBItemDTO {

    /**
     * item id
     */
    @ApiModelProperty(value = "item id")
    public String guid;

    /**
     * item name
     */
    @ApiModelProperty(value = "item name")
    public String name;

    /**
     * item type
     */
    @ApiModelProperty(value = "item type")
    public String type;

    /**
     * createUser
     */
    @ApiModelProperty(value = "createUser")
    public String createUser;
    
}
