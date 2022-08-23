package com.fisk.dataaccess.dto.output.datatarget;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class DataTargetPageResultDTO extends BaseUserInfoVO {

    public Long id;

    @ApiModelProperty(value = "名称", required = true)
    public String name;

    @ApiModelProperty(value = "负责人", required = true)
    public String principal;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;

    @ApiModelProperty(value = "服务地址")
    public String host;

    @ApiModelProperty(value = "api地址")
    public String apiAddress;

    @ApiModelProperty(value = "目标数据类型：1:Oracle 2:SqlServer 3:MySql 4:FTP 5:API")
    public Integer type;

}
