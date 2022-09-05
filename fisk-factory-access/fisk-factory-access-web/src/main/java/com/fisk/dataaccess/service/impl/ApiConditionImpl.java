package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.dataaccess.dto.apicondition.ApiConditionDTO;
import com.fisk.dataaccess.dto.apicondition.ApiConditionDataDTO;
import com.fisk.dataaccess.dto.apicondition.ApiConditionInfoDTO;
import com.fisk.dataaccess.entity.ApiConditionPO;
import com.fisk.dataaccess.enums.ApiConditionEnum;
import com.fisk.dataaccess.map.ApiConditionMap;
import com.fisk.dataaccess.mapper.ApiConditionMapper;
import com.fisk.dataaccess.service.IApiCondition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public List<ApiConditionInfoDTO> getApiConditionList() {
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
            return null;
        }
        List<ApiConditionInfoDTO> list = new ArrayList<>();
        for (String item : collect) {
            ApiConditionInfoDTO dto = new ApiConditionInfoDTO();
            dto.typeName = item;
            dto.parentTypeName = "";
            List<ApiConditionDTO> collect1 = dtoList.stream()
                    .filter(e -> item.equals(e.parent))
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect1)) {
                dto.data = new ArrayList<>();
                continue;
            }
            for (ApiConditionDTO child : collect1) {

            }

        }
        return list;
    }

    public ApiConditionDataDTO apiConditionAppend(String value, String tableName) {
        ApiConditionDataDTO dto = new ApiConditionDataDTO();
        String data = null;
        //Token
        if (value.toUpperCase().indexOf(ApiConditionEnum.TOKEN.name()) > -1) {
            dto.apiConditionEnum = ApiConditionEnum.TOKEN;
        }
        //PageNum
        if (value.toUpperCase().indexOf(ApiConditionEnum.PAGENUM.name()) > -1) {
            dto.apiConditionEnum = ApiConditionEnum.PAGENUM;
        }

        //Max、Min、Sum
        if (value.toUpperCase().indexOf(ApiConditionEnum.MAX.name()) > -1
                || value.toUpperCase().indexOf(ApiConditionEnum.Min.name()) > -1
                || value.toUpperCase().indexOf(ApiConditionEnum.SUM.name()) > -1) {
            dto.apiConditionEnum = ApiConditionEnum.PAGENUM;
            data = "select " + value + " from " + tableName;
        }

        //GetDate
        if (value.toUpperCase().indexOf(ApiConditionEnum.GETDATE.name()) > -1) {
            dto.apiConditionEnum = ApiConditionEnum.GETDATE;
            data = "select current_date";
        }

        //GetDateTime
        if (value.toUpperCase().indexOf(ApiConditionEnum.GETDATETIME.name()) > -1) {
            dto.apiConditionEnum = ApiConditionEnum.GETDATETIME;
            data = "select current_timestamp(0)";
        }
        dto.data = data;

        return dto;
    }

}
