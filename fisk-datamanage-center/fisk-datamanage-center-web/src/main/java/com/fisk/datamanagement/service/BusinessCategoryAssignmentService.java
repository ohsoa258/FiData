package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.BusinessCategoryAssignmentPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-04-11 16:45:35
 */
public interface BusinessCategoryAssignmentService extends IService<BusinessCategoryAssignmentPO> {


    List<Integer> getCategoryIds(List<Integer> roleIds);
}

