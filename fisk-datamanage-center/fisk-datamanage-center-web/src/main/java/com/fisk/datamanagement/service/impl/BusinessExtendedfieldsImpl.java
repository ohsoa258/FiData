package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategoryTreeDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDefsDTO;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.entity.BusinessCategoryPO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.BusinessCategoryMapper;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsMapper;
import com.fisk.datamanagement.mapper.FactTreeListMapper;
import com.fisk.datamanagement.service.BusinessCategoryService;
import com.fisk.datamanagement.service.BusinessExtendedfieldsService;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimension.DimensionTreeDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Service
public class BusinessExtendedfieldsImpl implements BusinessExtendedfieldsService {

    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;
    /**
     * 展示维度数据
     *
     * @param
     */
    @Override
    public List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String categoryId) {
        // 查询数据
        List<BusinessExtendedfieldsPO> po = businessExtendedfieldsMapper.selectParentpId(categoryId);
        System.out.println(po);
        return po;
    }

}
