package com.fisk.dataaccess.dto.apistate;

import lombok.Data;

@Data
public class ApiStateDTO {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 实时同步api开关状态：0关闭  1开启
     */
    private Integer apiState;


}
