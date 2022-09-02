package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.dataaccess.dto.apicondition.ApiConditionDTO;
import com.fisk.dataaccess.entity.ApiConditionPO;
import com.fisk.dataaccess.map.ApiConditionMap;
import com.fisk.dataaccess.mapper.ApiConditionMapper;
import com.fisk.dataaccess.service.IApiCondition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ApiConditionImpl implements IApiCondition {

    @Resource
    ApiConditionMapper mapper;

    @Override
    public List<ApiConditionDTO> getApiConditionList() {
        QueryWrapper<ApiConditionPO> queryWrapper = new QueryWrapper<>();
        List<ApiConditionPO> poList = mapper.selectList(queryWrapper);
        List<ApiConditionDTO> dtoList = ApiConditionMap.INSTANCES.poListToDtoList(poList);
        //获取父级
        List<String> collect = dtoList.stream()
                .filter(e -> "1".equals(e.parent))
                .map(e -> e.getTypeName())
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(collect)) {

        }


        return null;
    }

}
