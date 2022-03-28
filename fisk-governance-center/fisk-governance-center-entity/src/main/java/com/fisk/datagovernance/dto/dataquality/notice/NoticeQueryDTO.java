package com.fisk.datagovernance.dto.dataquality.notice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知查询DTO
 * @date 2022/3/24 14:30
 */
public class NoticeQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<NoticeVO> page;
}
