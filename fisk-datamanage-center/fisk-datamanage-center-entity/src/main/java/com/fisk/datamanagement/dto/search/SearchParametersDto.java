package com.fisk.datamanagement.dto.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class SearchParametersDto {

    @ApiModelProperty(value = "包括子分类")
    public boolean includeSubClassifications;
    @ApiModelProperty(value = "包括分类属性")
    public boolean includeClassificationAttributes;
    @ApiModelProperty(value = "排除已删除实体")
    public boolean excludeDeletedEntities;
    @ApiModelProperty(value = "包括子类型")
    public boolean includeSubTypes;
    @ApiModelProperty(value = "术语名称")
    public String termName;
    @ApiModelProperty(value = "补偿")
    public Integer offset;
    @ApiModelProperty(value = "限制")
    public Integer limit;
    @ApiModelProperty(value = "总条数")
    public Integer totalCount;
    @ApiModelProperty(value = "总页面数")
    public Integer pageCount;

    //获取总页数
    public void setTotalCount(Integer totalCount){
        this.totalCount=totalCount;
        this.pageCount=totalCount%limit==0?totalCount/limit:totalCount/limit+1;
    }
}
