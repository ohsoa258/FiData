package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.mapper.ApiConfigureFieldMapper;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.service.ApiFieldService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/8 9:58
 */
@Service
public class ApiFieldServiceImpl implements ApiFieldService {

    @Resource
    private ApiConfigureFieldMapper configureFieldMapper;

    @Resource
    private ApiConfigureMapper configureMapper;

    // todo 登录人

    @Override
    public List<Object> queryField(String apiRoute, Integer offset, Integer limit) {
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigurePO::getApiRoute, apiRoute);
        ApiConfigurePO apiConfigure = configureMapper.selectOne(queryWrapper);
        if (apiConfigure == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        QueryWrapper<ApiConfigureFieldPO> query = new QueryWrapper<>();
        query.lambda().eq(ApiConfigureFieldPO::getFieldId, apiConfigure.getId());
        List<ApiConfigureFieldPO> apiConfigureFieldList = configureFieldMapper.selectList(query);
        return this.filterData(apiConfigureFieldList, apiConfigure.getApiName(), offset, limit);
    }

    /**
     * 数据分组
     * @param apiConfigureFieldList
     */
    public List<Object> filterData(List<ApiConfigureFieldPO> apiConfigureFieldList,String apiName,Integer offset, Integer limit){
        List<ApiConfigureFieldPO> aggregationList = new ArrayList<>();
        List<ApiConfigureFieldPO> groupingList = new ArrayList<>();
        List<ApiConfigureFieldPO> conditionList = new ArrayList<>();
        for (ApiConfigureFieldPO field : apiConfigureFieldList) {
            switch (field.getFieldType()) {
                case GROUPING:
                    groupingList.add(field);
                    break;
                case AGGREGATION:
                    aggregationList.add(field);
                    break;
                case RESTRICT:
                    conditionList.add(field);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        }
        return this.splicingSql(aggregationList,groupingList,conditionList,offset,limit, apiName);
    }


    /**
     * 拼接sql
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     */
    public List<Object> splicingSql(List<ApiConfigureFieldPO> aggregationList, List<ApiConfigureFieldPO> groupingList,
                               List<ApiConfigureFieldPO> conditionList, Integer offset, Integer limit, String apiName){

        // 获取聚合的字段
        List<String> aggregationFieldList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aggregationList)){
            for (ApiConfigureFieldPO configureField : aggregationList) {
                aggregationFieldList.add(configureField.getFieldConditionValue() + "(" + configureField.getField() +")");
            }
        }

        // 获取分组的字段
        List<String> groupList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupingList)){
            for (ApiConfigureFieldPO configureField : groupingList) {
                groupList.add(configureField.getField());
            }
        }

        // 权限控制的字段
        List<String> whereList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(conditionList)){
            for (ApiConfigureFieldPO configureField : conditionList) {
                whereList.add(configureField.getField()+configureField.getFieldConditionValue()+configureField.getFieldValue());
            }
        }

        if (offset == null){
            offset = 0;
        }
        if (limit == null){
            limit = 50;
        }
        List<Object> objects = configureMapper.queryData(aggregationFieldList, groupList, apiName, whereList, offset, limit);
        return objects;
    }
}
