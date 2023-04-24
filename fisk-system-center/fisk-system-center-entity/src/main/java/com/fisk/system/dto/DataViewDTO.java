package com.fisk.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.system.enums.serverModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/3 15:29
 */
@Data
public class DataViewDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "视图名称")
    public String viewName;
    @ApiModelProperty(value = "服务器模块")
    private serverModuleTypeEnum serverModule;
    @ApiModelProperty(value = "用户Id")
    private Integer userId;
    @ApiModelProperty(value = "视图类型")
    private String viewType;
    @ApiModelProperty(value = "过滤列表")
    private List<DataViewFilterDTO> filterList;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;
}
