package com.fisk.dataservice.vo.tableapi;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-18
 * @Description:
 */
@Data
public class ApiLogPageDTO {
    @ApiModelProperty(value = "apiId")
    public Integer apiId;
    @ApiModelProperty(value = "页")
    public Page<ApiLogVO> page;
}
