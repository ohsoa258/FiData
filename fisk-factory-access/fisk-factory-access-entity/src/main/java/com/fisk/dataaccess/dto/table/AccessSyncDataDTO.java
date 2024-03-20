package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AccessSyncDataDTO implements Serializable {

    /**
     * tblId
     */
    @ApiModelProperty(value = "表id")
    public long tblId;

}
