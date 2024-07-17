package com.fisk.dataservice.vo.tableapi;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2024-07-10
 * @Description:
 */
@Data
public class RecentApiGatewayCallsVO {
    private String apiCode;
    private String apiName;
    private LocalDateTime date;
}
