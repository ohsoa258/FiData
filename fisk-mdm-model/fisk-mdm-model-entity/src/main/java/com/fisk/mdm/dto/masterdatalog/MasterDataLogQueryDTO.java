package com.fisk.mdm.dto.masterdatalog;

import com.fisk.mdm.dto.masterdata.MasterDataBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataLogQueryDTO extends MasterDataBaseDTO {

    @ApiModelProperty(value = "页码索引")
    private Integer pageIndex;

    @ApiModelProperty(value = "页码大小")
    private Integer pageSize;

    @ApiModelProperty(value = "fiDataId")
    private Integer fiDataId;

}
