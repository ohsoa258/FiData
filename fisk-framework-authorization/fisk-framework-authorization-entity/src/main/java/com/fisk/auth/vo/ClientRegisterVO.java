package com.fisk.auth.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 筛选器查询结果
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
@Data
public class ClientRegisterVO {

    @ApiModelProperty(value = "主键(添加时不需要传)")
    public Long id;

    @ApiModelProperty(value = "客户端名称",required = true)
    public String clientName;

    @ApiModelProperty(value = "token值")
    public String tokenValue;

    @ApiModelProperty(value = "token描述")
    public String tokenDes;

    @ApiModelProperty(value = "是否有效")
    public boolean valid;

    @ApiModelProperty(value = "过期时间(yyyy-MM-dd)",required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date expireTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;
}
