package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 添加警告通知VO
 * @date 2022/3/22 15:38
 */
@Data
public class AddNoticeVO {
    /**
     * 组件通知列表
     */
    @ApiModelProperty(value = "组件通知列表")
    public List<ComponentNotificationVO> notificationVOList;

    /**
     * 邮件服务器列表
     */
    @ApiModelProperty(value = "邮件服务器列表")
    public List<EmailServerVO> emailServerVOS;
}
