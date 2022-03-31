package com.fisk.datagovernance.dto.dataquality.emailserver;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器查询DTO
 * @date 2022/3/24 13:56
 */
public class EmailServerQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<EmailServerVO> page;
}
