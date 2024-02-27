package com.fisk.dataservice.redisdata;

import com.fisk.dataservice.entity.LogPO;
import com.fisk.dataservice.vo.apiservice.ResponseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-02-26
 * @Description:
 */
@Data
public class RedisData {
    @ApiModelProperty(value = "返回数据")
    public ResponseVO responseVO;
    @ApiModelProperty(value = "日志")
    public LogPO logPO;
}
