package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * date 2022/05/07 11:26
 */
@Data
public class ImportDataSubmitDTO {

    @ApiModelProperty(value = "键")
    private String key;

    @ApiModelProperty(value = "实体Id")
    private Integer entityId;

}
