package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName:
 * @Author: SongJianJian
 * @Date: 2023
 * @Copyright: 2023 by SongJianJian
 * @Description:
 **/
@Data
public class AppRegistrationInfoDTO {

    /**
     * 应用id
     */
    @ApiModelProperty(value = "appId")
    private long appId;

    /**
     * 目标源id
     */
    @ApiModelProperty(value = "目标数据库Id")
    private Integer targetDbId;
}
