package com.fisk.datamanagement.dto.label;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class LabelDataDTO {
    @ApiModelProperty(value = "id")
    public int id;

    @ApiModelProperty(value = "标签中文名")
    public String labelCnName;

    @ApiModelProperty(value = "标签英文名")
    public String labelEnName;
    @ApiModelProperty(value = "标签des")
    public String labelDes;
    @ApiModelProperty(value = "服务名称")
    public String serverName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;

    @ApiModelProperty(value = "更新者")
    public String updateUser;
}
