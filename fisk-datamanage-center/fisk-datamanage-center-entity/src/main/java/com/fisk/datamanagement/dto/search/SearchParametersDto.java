package com.fisk.datamanagement.dto.search;

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

    public boolean includeSubClassifications;

    public boolean includeClassificationAttributes;

    public boolean excludeDeletedEntities;

    public boolean includeSubTypes;

    public String termName;

    public Integer offset;

    public Integer limit;

    public Integer totalCount;

    public Integer pageCount;

    //获取总页数
    public void setTotalCount(Integer totalCount){
        this.totalCount=totalCount;
        this.pageCount=totalCount%limit==0?totalCount/limit:totalCount/limit+1;
    }
}
