package com.fisk.datamanagement.dto.businessclassification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
public class BusinessClassificationTreeDTO {

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
    public List<BusinessClassificationTreeDTO> child;

}
