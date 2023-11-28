package com.fisk.datamanagement.dto.businessclassification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName:
 * @Author: xgf
 * @Date: 2023
 * @Copyright: 2023 by xgf
 * @Description:
 **/
@Data
public class BusinessCategoryTreeDTO {

    @ApiModelProperty(value = "id")
    public String id;

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "pid")
    public String pid;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "创建时间")
    @JsonIgnore
    public LocalDateTime createTime;

    @ApiModelProperty(value = "子类")
    public List<BusinessCategoryTreeDTO> child;

}
