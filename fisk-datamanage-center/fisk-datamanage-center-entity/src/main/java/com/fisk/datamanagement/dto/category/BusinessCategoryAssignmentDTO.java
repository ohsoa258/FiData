package com.fisk.datamanagement.dto.category;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-04-12
 * @Description:
 */
@Data
public class BusinessCategoryAssignmentDTO {
    public Integer menuId;
    public List<Integer> roleIds;
}
