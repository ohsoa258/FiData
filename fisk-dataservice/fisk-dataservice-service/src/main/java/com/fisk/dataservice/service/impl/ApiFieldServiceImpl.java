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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public List<Map> queryField(String apiRoute, Integer currentPage, Integer pageSize) {
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigurePO::getApiRoute, apiRoute);
        ApiConfigurePO apiConfigure = configureMapper.selectOne(queryWrapper);
        if (apiConfigure == null) {
            throw new FkException(ResultEnum.NOTFOUND);
        }

        QueryWrapper<ApiConfigureFieldPO> query = new QueryWrapper<>();
        query.lambda().eq(ApiConfigureFieldPO::getConfigureId, apiConfigure.getId());
        List<ApiConfigureFieldPO> apiConfigureFieldList = configureFieldMapper.selectList(query);
        return this.filterData(apiConfigureFieldList, apiConfigure.getTableName(), currentPage, pageSize);
    }

    /**
     * 数据分组
     * @param apiConfigureFieldList
     * @param tableName
     * @param currentPage
     * @param pageSize
     * @return
     */
    public List<Map> filterData(List<ApiConfigureFieldPO> apiConfigureFieldList,String tableName, Integer currentPage, Integer pageSize){
        List<ApiConfigureFieldPO> aggregationList = new ArrayList<>();
        List<ApiConfigureFieldPO> groupingList = new ArrayList<>();
        List<ApiConfigureFieldPO> conditionList = new ArrayList<>();
        List<ApiConfigureFieldPO> queryFieldList = new ArrayList<>();
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
                case QUERY:
                    queryFieldList.add(field);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        }
        return this.splicingSql(queryFieldList,aggregationList,groupingList,conditionList,currentPage,pageSize, tableName);
    }


    /**
     * 拼接sql
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param currentPage    分页
     * @param pageSize
     * @param tableName  表名
     * @return
     */
    public List<Map> splicingSql(List<ApiConfigureFieldPO> queryFieldList,
                                 List<ApiConfigureFieldPO> aggregationList,
                                 List<ApiConfigureFieldPO> groupingList,
                                 List<ApiConfigureFieldPO> conditionList,
                                 Integer currentPage, Integer pageSize, String tableName){
        // 获取查询的字段
        List<String> queryList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(queryFieldList)){
            for (ApiConfigureFieldPO apiConfigureField : queryFieldList) {
                queryList.add(apiConfigureField.getField());
            }
        }

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
                whereList.add(configureField.getField()+configureField.getFieldConditionValue()+"'"+configureField.getFieldValue()+"'");
            }
        }

        if (currentPage == null){
            currentPage = 1;
        }
        if (pageSize == null){
            pageSize = 50;
        }
        List<Map> objects = configureMapper.queryData(queryList,aggregationFieldList, groupList, tableName, whereList, (currentPage-1)*pageSize, pageSize);
        return objects;
    }
}
