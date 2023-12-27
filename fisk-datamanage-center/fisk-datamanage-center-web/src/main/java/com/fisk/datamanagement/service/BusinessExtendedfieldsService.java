package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONArray;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;

import java.util.List;

public interface BusinessExtendedfieldsService {




    /**
     * 展示维度数据
     * @param
     * @return
     */
    List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String indexid);

}
