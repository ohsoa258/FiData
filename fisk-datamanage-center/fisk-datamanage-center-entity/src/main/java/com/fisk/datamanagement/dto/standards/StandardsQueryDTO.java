package com.fisk.datamanagement.dto.standards;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-07
 * @Description:
 */
@Data
public class StandardsQueryDTO {

    @ApiModelProperty(value = "标签id")
    public Integer menuId;

    @ApiModelProperty(value = "查询关键字")
    public String keyWord;

    @ApiModelProperty(value = "页")
    public Page<StandardsMenuDTO> page;
}
