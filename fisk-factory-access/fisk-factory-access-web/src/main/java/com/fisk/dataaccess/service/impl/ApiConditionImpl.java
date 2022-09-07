package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.ApiParameterDTO;
import com.fisk.dataaccess.dto.apicondition.ApiConditionDTO;
import com.fisk.dataaccess.dto.apicondition.ApiConditionInfoDTO;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.app.AppRegistrationDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.entity.ApiConditionPO;
import com.fisk.dataaccess.enums.ApiConditionEnum;
import com.fisk.dataaccess.map.ApiConditionMap;
import com.fisk.dataaccess.mapper.ApiConditionMapper;
import com.fisk.dataaccess.service.IApiCondition;
import com.fisk.dataaccess.utils.sql.PgsqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ApiConditionImpl implements IApiCondition {

    @Resource
    ApiConditionMapper mapper;
    @Resource
    ApiParameterServiceImpl apiParameterService;
    @Resource
    ApiConfigImpl apiConfig;
    @Resource
    AppDataSourceImpl appDataSource;
    @Resource
    AppRegistrationImpl appRegistration;
    @Resource
    TableAccessImpl tableAccess;
    @Resource
    PgsqlUtils pgsqlUtils;
    @Resource
    private RedisTemplate redisTemplate;

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

    @Override
    public List<ApiParameterDTO> apiConditionAppend(Long id) {

        //获取应用id
        ApiConfigDTO apiConfigData = apiConfig.getData(id);
        if (apiConfigData == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //获取应用简称
        AppRegistrationDTO appRegistrationData = appRegistration.getData(apiConfigData.appId);
        if (appRegistrationData == null) {
            throw new FkException(ResultEnum.API_APP_ISNULL);
        }

        //获取api参数列表
        List<ApiParameterDTO> parameterList = apiParameterService.getListByApiId(id);
        if (CollectionUtils.isEmpty(parameterList)) {
            return parameterList;
        }

        //循环替换value值
        for (ApiParameterDTO item : parameterList) {
            if (item.parameterType != 2) {
                continue;
            }
            String data = null;
            if (item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.TOKEN.getName()) > -1) {
                AppDataSourceDTO dataSource = appDataSource.getDataSourceByAppId(apiConfigData.appId);
                if (dataSource == null) {
                    throw new FkException(ResultEnum.AUTH_TOKEN_PARSER_ERROR);
                }
                Boolean exist = redisTemplate.hasKey("ApiConfig:" + dataSource.id);
                if (exist) {
                    item.parameterValue = redisTemplate.opsForValue().get("ApiConfig:" + dataSource.id).toString();
                } else {
                    item.parameterValue = appRegistration.getApiToken(dataSource);
                }
                continue;
            }

            //PageNum
            if (item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.PAGENUM.getName()) > -1) {
                continue;
            }

            //Max、Min等聚合函数
            if (item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.MAX.getName()) > -1
                    || item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.MIN.getName()) > -1
                    || item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.SUM.getName()) > -1) {
                TableAccessDTO tableAccess = this.tableAccess.getTableAccess(item.tableAccessId);
                if (StringUtils.isEmpty(tableAccess.tableName)) {
                    throw new FkException(ResultEnum.TASK_TABLE_NOT_EXIST);
                }
                data = "select " + item.parameterValue + " as datas from ods_"
                        + appRegistrationData.appAbbreviation + "_" + tableAccess.tableName;
            }

            //CurrentDate、CurrentTimeStamp
            if (item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.CURRENT_DATE.getName()) > -1
                    || item.parameterValue.toUpperCase().indexOf(ApiConditionEnum.CURRENT_TIMESTAMP.getName()) > -1) {
                data = "select " + item.parameterValue + " as datas";
            }
            List<Map<String, Object>> maps = pgsqlUtils.executePgSql(data.toLowerCase());
            if (CollectionUtils.isEmpty(maps)) {
                continue;
            }
            if (maps.get(0).get("datas") == null) {
                item.parameterValue = "";
                continue;
            }
            item.parameterValue = maps.get(0).get("datas").toString();
        }
        return parameterList;
    }

}
