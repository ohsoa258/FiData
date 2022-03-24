package com.fisk.datamanagement.dto.labelcategory;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LabelCategoryDTO {
    public int id;
    public String categoryCode;
    public String categoryParentCode;
    public String categoryCnName;
    public String categoryEnName;
}
