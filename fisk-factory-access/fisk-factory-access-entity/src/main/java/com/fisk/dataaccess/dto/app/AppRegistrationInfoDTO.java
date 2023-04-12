package com.fisk.dataaccess.dto.app;

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
    private long appId;

    /**
     * 目标源id
     */
    private Integer targetDbId;
}
