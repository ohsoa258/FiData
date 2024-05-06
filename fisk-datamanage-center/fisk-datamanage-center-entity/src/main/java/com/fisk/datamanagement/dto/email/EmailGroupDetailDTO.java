package com.fisk.datamanagement.dto.email;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmailGroupDetailDTO {
    /**
     * 邮件组id
     */
    @ApiModelProperty(value = "邮件组id")
    public long id;

    /**
     * 邮件组名称
     */
    @ApiModelProperty(value = "邮件组名称")
    private String groupName;

    /**
     * 组描述
     */
    @ApiModelProperty(value = "组描述")
    private String groupDesc;

    /**
     * 组关联的邮件服务器id
     */
    @ApiModelProperty(value = "组关联的邮件服务器id")
    private Integer emailServerId;

    /**
     * 组关联的邮件服务器名称
     */
    private String emailServerName;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 邮件组关联的用户
     */
    @ApiModelProperty(value = "邮件组关联的用户")
    private List<EmailUserDTO> users;

}
