package com.fisk.datagovernance.vo.dataquality.notice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 通知组件
 * @date 2022/3/25 22:07
 */
@Data
public class NoticeModuleVO {
    /**
     * 通知id
     */
    @ApiModelProperty(value = "通知id")
    public int noticeId;

    /**
     * 通知组件名称
     */
    @ApiModelProperty(value = "通知名称")
    public String noticeName;

    /**
     * 通知组件
     */
    @ApiModelProperty(value = "通知组件")
    public List<NoticeModuleVO> noticeModuleVOS;
}
