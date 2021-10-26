package com.fisk.datagovern.dto.category;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CategoryDTO {
    public int id;
    public String categoryCode;
    public String categoryParentCode;
    public String categoryCnName;
    public String categoryEnName;
}
