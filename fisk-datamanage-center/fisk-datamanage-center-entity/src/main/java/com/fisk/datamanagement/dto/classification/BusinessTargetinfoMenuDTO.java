package com.fisk.datamanagement.dto.classification;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2024-04-11
 * @Description:
 */
@Data
public class BusinessTargetinfoMenuDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "类型")
    public String type;
    @ApiModelProperty(value = "所属分类id")
    public String categoryId;
    @ApiModelProperty(value = "所属分类")
    public String categoryName;
    @ApiModelProperty(value = "指标状态")
    public String indicatorStatus;
    @ApiModelProperty(value = "上级指标Id")
    public Integer parentBusinessId;
    @ApiModelProperty(value = "上级指标目录节点Id")
    public String parentBusinessCategoryId;
    @ApiModelProperty(value = "上级指标名称")
    public String parentBusinessName;
    @ApiModelProperty(value = "指标编码")
    public String indicatorCcode;
    @ApiModelProperty(value = "指标描述/口径")
    public String indicatorDescription;
    @ApiModelProperty(value = "应用")
    public String largeScreenLink;
    @ApiModelProperty(value = "数据源系统")
    public String sourceSystem;
    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
}
