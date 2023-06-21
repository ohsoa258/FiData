package com.fisk.dataservice.dto.logs;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 查询日志
 * @date 2022/3/7 12:14
 */
@Data
public class LogQueryDTO {
    /**
     * API ID
     */
    @ApiModelProperty(value = "apiId")
    public Integer apiId;

    /**
     * APP ID
     */
    @ApiModelProperty(value = "appId")
    public Integer appId;

    /**
     * createApiType
     * 1 创建新api、2 使用现有api、3 代理API
     */
    @ApiModelProperty(value = "createApiType：1 创建新api、2 使用现有api、3 代理API")
    public Integer createApiType;

    /**
     * 调用周期开始时间
     */
    @ApiModelProperty(value = "调用周期开始时间")
    public String callCycleStartDate;

    /**
     * 调用周期结束时间
     */
    @ApiModelProperty(value = "调用周期结束时间")
    public String callCycleEndDate;

    /**
     * keyword
     */
    @ApiModelProperty(value = "keyword")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<ApiLogVO> page;
}
