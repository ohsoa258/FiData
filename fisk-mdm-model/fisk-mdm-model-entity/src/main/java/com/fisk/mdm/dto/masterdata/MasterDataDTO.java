package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDTO extends MasterDataBaseDTO {

    @ApiModelProperty(value = "描述")
    private String description;
    private String fidataId;

    @ApiModelProperty(value = "成员")
    private List<Map<String, Object>> members;

}
