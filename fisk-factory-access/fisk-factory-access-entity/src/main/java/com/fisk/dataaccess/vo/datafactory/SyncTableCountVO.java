package com.fisk.dataaccess.vo.datafactory;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class SyncTableCountVO {

    @ApiModelProperty("总表数量")
    private Integer totalCount;

    @ApiModelProperty("追加覆盖")
    private Integer appendCoverCount;

    @ApiModelProperty("全量覆盖")
    private Integer fullCoverCount;

    @ApiModelProperty("业务主键覆盖")
    private Integer businessKeyCoverCount;

    @ApiModelProperty("业务时间覆盖")
    private Integer businessTimeCoverCount;

}
