package com.fisk.system.dto.systemlog;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @author lishiji
 */
@Data
public class SystemLogDTO implements Serializable {

    /**
     * 要查询的日志日期（哪天的日志）
     */
    @ApiModelProperty(value = "要查询的日志日期（哪天的日志）")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public String date;

    /**
     * 要查询的服务类型（哪个服务的日志）
     */
    @ApiModelProperty(value = "要查询的服务类型（哪个服务的日志）")
    public int serviceType;

    /**
     * 0：倒序
     * 1：正序
     */
    @ApiModelProperty(value = "排序方式：0：倒序 1：正序")
    public int orderBy;

}
