package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.governance.BuildGovernanceHelper;
import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.enums.HttpRequestEnum;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiConfigDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiParamDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiResultDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParamPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiParamMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiResultMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiParamMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiResultMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
@Slf4j
public class BusinessFilterApiManageImpl extends ServiceImpl<BusinessFilterApiMapper, BusinessFilterApiConfigPO> implements IBusinessFilterApiManageService {

    @Resource
    private BusinessFilterApiParamMapper businessFilterApiParamMapper;

    @Resource
    private BusinessFilterApiResultMapper businessFilterApiResultMapper;

    @Resource
    private BusinessFilterApiParamManageImpl businessFilterApiParamManageImpl;

    @Resource
    private BusinessFilterApiResultManageImpl businessFilterApiResultManageImpl;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private UserHelper userHelper;

    @Override
    public List<BusinessFilterQueryApiVO> getApiListByRuleIds(List<Integer> ruleIds) {
        List<BusinessFilterQueryApiVO> queryApiVOS = new ArrayList<>();

        if (CollectionUtils.isEmpty(ruleIds)) {
            return queryApiVOS;
        }
        // 查询API基础配置信息
        QueryWrapper<BusinessFilterApiConfigPO> apiConfigPOQueryWrapper = new QueryWrapper<>();
        apiConfigPOQueryWrapper.lambda()
                .in(BusinessFilterApiConfigPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiConfigPO::getDelFlag, 1);
        List<BusinessFilterApiConfigPO> businessFilterApiConfigPOS = baseMapper.selectList(apiConfigPOQueryWrapper);
        if (CollectionUtils.isEmpty(businessFilterApiConfigPOS)) {
            return queryApiVOS;
        }
        // 查询API参数配置信息
        QueryWrapper<BusinessFilterApiParamPO> apiParmPOQueryWrapper = new QueryWrapper<>();
        apiParmPOQueryWrapper.lambda()
                .in(BusinessFilterApiParamPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiParamPO::getDelFlag, 1);
        List<BusinessFilterApiParamPO> businessFilterApiParmPOS = businessFilterApiParamMapper.selectList(apiParmPOQueryWrapper);
        // 查询API结果配置信息
        QueryWrapper<BusinessFilterApiResultPO> apiResultPOQueryWrapper = new QueryWrapper<>();
        apiResultPOQueryWrapper.lambda()
                .in(BusinessFilterApiResultPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiResultPO::getDelFlag, 1);
        List<BusinessFilterApiResultPO> businessFilterApiResultPOS = businessFilterApiResultMapper.selectList(apiResultPOQueryWrapper);

        businessFilterApiConfigPOS.forEach(t -> {
            BusinessFilterQueryApiVO apiVO = new BusinessFilterQueryApiVO();
            apiVO.setRuleId(Integer.valueOf(t.getRuleId()));
            apiVO.setApiConfig(BusinessFilterApiMap.INSTANCES.poToVo(t));
            if (CollectionUtils.isNotEmpty(businessFilterApiParmPOS)) {
                List<BusinessFilterApiParamPO> paramList = businessFilterApiParmPOS.stream().filter(parm -> parm.getApiId() == t.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(paramList)) {
                    apiVO.setApiParmConfig(BusinessFilterApiParamMap.INSTANCES.poToVo(paramList));
                }
            }
            if (CollectionUtils.isNotEmpty(businessFilterApiResultPOS)) {
                List<BusinessFilterApiResultPO> resultList = businessFilterApiResultPOS.stream().filter(result -> result.getApiId() == t.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(resultList)) {
                    List<BusinessFilterApiResultVO> businessFilterApiResultVOS = BusinessFilterApiResultMap.INSTANCES.poToVo(resultList);
                    List<BusinessFilterApiResultVO> apiResultVOS = queryApiRecursionResult(businessFilterApiResultVOS);
                    apiVO.setApiResultConfig(apiResultVOS);
                }
            }
            queryApiVOS.add(apiVO);
        });

        return queryApiVOS;
    }

    @Override
    public ResultEnum saveApiInfo(String operationType, int ruleId, BusinessFilterSaveDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        if (operationType == "edit") {
            baseMapper.updateByRuleId(ruleId);
            businessFilterApiParamMapper.updateByRuleId(ruleId);
            businessFilterApiResultMapper.updateByRuleId(ruleId);
        }
        int insertCount = 0;
        BusinessFilterApiConfigPO businessFilterApiConfigPO = BusinessFilterApiMap.INSTANCES.dtoToPo(dto.getApiConfig());
        if (businessFilterApiConfigPO != null) {
            UserInfo loginUserInfo = userHelper.getLoginUserInfo();
            businessFilterApiConfigPO.setCreateTime(LocalDateTime.now());
            businessFilterApiConfigPO.setCreateUser(String.valueOf(loginUserInfo.getId()));
            businessFilterApiConfigPO.setRuleId(ruleId);
            insertCount = baseMapper.insertOne(businessFilterApiConfigPO);
        }
        if (insertCount > 0) {
            int apiId = Math.toIntExact(businessFilterApiConfigPO.getId());
            if (CollectionUtils.isNotEmpty(dto.getApiParamConfig())) {
                List<BusinessFilterApiParamPO> businessFilterApiParamPOS = BusinessFilterApiParamMap.INSTANCES.dtoToPo(dto.getApiParamConfig());
                businessFilterApiParamPOS.forEach(t -> {
                    t.setRuleId(ruleId);
                    t.setApiId(apiId);
                });
                businessFilterApiParamManageImpl.saveBatch(businessFilterApiParamPOS);
            }
            if (CollectionUtils.isNotEmpty(dto.getApiResultConfig())) {
                List<BusinessFilterApiResultDTO> resultDTOS = saveApiRecursionResult(dto.getApiResultConfig());
                List<BusinessFilterApiResultPO> businessFilterApiResultPOS = BusinessFilterApiResultMap.INSTANCES.dtoToPo(resultDTOS);
                businessFilterApiResultPOS.forEach(t -> {
                    t.setRuleId(ruleId);
                    t.setApiId(apiId);
                });
                businessFilterApiResultManageImpl.saveBatch(businessFilterApiResultPOS);
            }
        }
        if (StringUtils.isNotEmpty(dto.getApiConfig().getApiAuthTicket())) {
            String authRedisKey = "BusinessFilterApiConfig:" + dto.getApiConfig().getRuleId();
            // token存Redis
            redisTemplate.opsForValue().set(authRedisKey, dto.getApiConfig().getApiAuthTicket(), dto.getApiConfig().getApiAuthExpirMinute(), TimeUnit.MINUTES);
        }
        return insertCount > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteApiInfo(int ruleId) {
        if (ruleId == 0) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        baseMapper.updateByRuleId(ruleId);
        businessFilterApiParamMapper.updateByRuleId(ruleId);
        businessFilterApiResultMapper.updateByRuleId(ruleId);
        String authRedisKey = "BusinessFilterApiConfig:" + ruleId;
        boolean flag = redisTemplate.hasKey(RedisKeyBuild.buildFiDataStructureKey(authRedisKey));
        if (flag) {
            redisTemplate.delete(authRedisKey);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEntity<String> collAuthApi(BusinessFilterDTO dto) {
        String token = "";
        if (dto == null) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, token);
        }
        try {
            BusinessFilterApiConfigDTO apiConfig = dto.getApiInfo().getApiConfig();
            List<BusinessFilterApiParamDTO> apiParmConfig = dto.getApiInfo().getApiParamConfig().stream().filter(t -> t.getApiParamType() == 1).collect(Collectors.toList());
            List<BusinessFilterApiResultDTO> apiResultConfigList = dto.getApiInfo().getApiResultConfig().stream().filter(t -> t.getResultParamType() == 1).collect(Collectors.toList());
            if (apiConfig == null || CollectionUtils.isEmpty(apiParmConfig) || CollectionUtils.isEmpty(apiResultConfigList)) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, token);
            }
            apiResultConfigList = saveApiRecursionResult(apiResultConfigList);
            BusinessFilterApiResultDTO apiResultConfig = apiResultConfigList.stream().filter(t -> t.getAuthField() == 1).findFirst().orElse(null);
            if (apiResultConfig == null) {
                return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, token);
            }

            // 验证授权票据是否过期
            String authRedisKey = "BusinessFilterApiConfig:" + apiConfig.getRuleId();
            boolean flag = redisTemplate.hasKey(RedisKeyBuild.buildFiDataStructureKey(authRedisKey));
            if (flag) {
                token = redisTemplate.opsForValue().get(authRedisKey).toString();
            }
            // 票据已过期，重新获取授权票据
            if (StringUtils.isEmpty(token)) {
                ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
                apiHttpRequestDto.httpRequestEnum = HttpRequestEnum.POST;
                // 身份验证地址
                apiHttpRequestDto.uri = apiConfig.getApiAuthAddress();
                // jwt账号&密码
                JSONObject rawDataParams = new JSONObject();
                Map<String, String> formDataParams = new IdentityHashMap<>();
                if (apiConfig.getApiAuthBodyType().equals("raw")) {
                    apiParmConfig.forEach(t -> {
                        rawDataParams.put(t.getApiParamKey(), t.getApiParamValue());
                    });
                    apiHttpRequestDto.setJsonObject(rawDataParams);
                } else if (apiConfig.getApiAuthBodyType().equals("form-data")) {
                    apiParmConfig.forEach(t -> {
                        formDataParams.put(t.getApiParamKey(), t.getApiParamValue());
                    });
                    apiHttpRequestDto.setFormDataParams(formDataParams);
                }
                String httpRequestResult = dataAccessClient.getHttpRequestResult(apiHttpRequestDto);
                String bearer = "Bearer ";
                if (StringUtils.isNotEmpty(httpRequestResult)) {
                    JSONObject jsonObject = JSONObject.parseObject(httpRequestResult);
                    token = jsonObject.getString(apiResultConfig.getSourceField());
                    if (StringUtils.isEmpty(token)) {
                        throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
                    }
                    if (!token.contains(bearer)) {
                        token = bearer + token;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[collAuthApi]-ex:" + ex);
            throw new FkException(ResultEnum.DATA_QUALITY_BUSINESS_API_AUTH_FILTER_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(StringUtils.isNotEmpty(token) ? ResultEnum.SUCCESS : ResultEnum.AUTH_TOKEN_IS_NOTNULL, token);
    }

    @Override
    public ResultEnum collApi(BusinessFilterDTO dto) {
        if (dto == null || dto.getApiInfo() == null) {
            return ResultEnum.PARAMTER_ERROR;
        }
        try {
            BusinessFilterApiConfigDTO apiConfig = dto.getApiInfo().getApiConfig();
            List<BusinessFilterApiParamDTO> apiParamConfig = dto.getApiInfo().getApiParamConfig().stream().filter(t -> t.getApiParamType() == 2).collect(Collectors.toList());
            List<BusinessFilterApiResultDTO> apiResultConfig = dto.getApiInfo().getApiResultConfig().stream().filter(t -> StringUtils.isNotEmpty(t.getTargetField()) && t.getResultParamType() == 2).collect(Collectors.toList());
            if (apiConfig == null || CollectionUtils.isEmpty(apiParamConfig) || CollectionUtils.isEmpty(apiResultConfig)) {
                return ResultEnum.PARAMTER_ERROR;
            }
            List<String> rspFieldKeys = apiResultConfig.stream().filter(t -> t.getPrimaryKeyField() == 1).map(f -> f.getTargetField()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(rspFieldKeys)) {
                return ResultEnum.DATA_QUALITY_UPDATE_PRIMARY_KEY_ISNOTSET;
            }

            apiResultConfig = tileApiRecursionResult(apiResultConfig);
            // 获取授权token
            String token = "";
            if (StringUtils.isNotEmpty(apiConfig.getApiAuthTicket())) {
                token = apiConfig.getApiAuthTicket();
            } else if (apiConfig.getRuleId() != 0) {
                String authRedisKey = "BusinessFilterApiConfig:" + apiConfig.getRuleId();
                boolean flag = redisTemplate.hasKey(RedisKeyBuild.buildFiDataStructureKey(authRedisKey));
                if (flag) {
                    token = redisTemplate.opsForValue().get(authRedisKey).toString();
                }
            } else {
                ResultEntity<String> result = collAuthApi(dto);
                token = result.getData();
            }
            if (StringUtils.isEmpty(token)) {
                return ResultEnum.AUTH_TOKEN_IS_NOTNULL;
            }
            DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource().stream().filter(t -> t.getId() == dto.getDatasourceId()).findFirst().orElse(null);

            // 获取请求参数实际的参数名称
            String tableName = dto.getTableUnique();
            if (dto.getSourceTypeEnum() == SourceTypeEnum.FiData) {
                List<DataTableFieldDTO> dataTableFieldDTOS = new ArrayList<>();
                DataTableFieldDTO dataTableFieldDTO = new DataTableFieldDTO();
                dataTableFieldDTO.setId(dto.getTableUnique());
                dataTableFieldDTO.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(dataSourceConVO.getDatasourceId()));
                dataTableFieldDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.getEnum(dto.getTableBusinessType()));
                dataTableFieldDTOS.add(dataTableFieldDTO);
                List<FiDataMetaDataDTO> fiDataMetaDatas = dataSourceConManageImpl.getTableFieldName(dataTableFieldDTOS);
                if (CollectionUtils.isNotEmpty(fiDataMetaDatas)) {
                    FiDataMetaDataTreeDTO fiDataMetaData_Table = fiDataMetaDatas.get(0).getChildren().get(0);
                    List<FiDataMetaDataTreeDTO> fiDataMetaData_Fields = fiDataMetaData_Table.getChildren();
                    if (fiDataMetaData_Table != null) {
                        tableName = fiDataMetaData_Table.getLabel();
                    }
                    if (CollectionUtils.isNotEmpty(fiDataMetaData_Fields)) {
                        apiParamConfig.forEach(t -> {
                            if (StringUtils.isNotEmpty(t.getApiParamValueUnique())) {
                                FiDataMetaDataTreeDTO fiDataMetaDataDTO = fiDataMetaData_Fields.stream().filter(f -> f.getId().equals(t.getApiParamValueUnique())).findFirst().orElse(null);
                                if (fiDataMetaDataDTO != null) {
                                    t.setApiParamValue(fiDataMetaDataDTO.getLabel());
                                }
                            }
                        });
                    }
                }
            }

            // 拼接sql语句，查询表数据
            List<String> reqFieldNames = apiParamConfig.stream().filter(t -> StringUtils.isNotEmpty(t.getApiParamValue()) && t.getApiParamKey() != "pageNum" && t.getApiParamKey() != "pageSize").map(f -> f.getApiParamValue()).collect(Collectors.toList());
            reqFieldNames.addAll(rspFieldKeys);
            BusinessFilterApiParamDTO pageNumDto = apiParamConfig.stream().filter(t -> t.getApiParamKey() == "pageNum").findFirst().orElse(null);
            BusinessFilterApiParamDTO pageSizeDto = apiParamConfig.stream().filter(t -> t.getApiParamKey() == "pageSize").findFirst().orElse(null);
            Integer pageNum = 0, pageSize = Integer.MAX_VALUE;
            if (pageNumDto != null) {
                pageNum = Integer.valueOf(pageNumDto.getApiParamValue());
            }
            if (pageSizeDto != null) {
                pageSize = Integer.valueOf(pageNumDto.getApiParamValue());
            }
            IBuildGovernanceSqlCommand dbCommand = BuildGovernanceHelper.getDBCommand(dataSourceConVO.getConType());
            String sql = dbCommand.buildPagingSql(tableName, reqFieldNames, "", pageNum, pageSize);
            log.info("[collApi]-sql:" + sql);
            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
            Connection conn = dbHelper.connection(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), dataSourceConVO.getConType());
            List<Map<String, Object>> data = dbHelper.batchExecQueryResultMaps(sql, conn);
            if (CollectionUtils.isEmpty(data)) {
                return ResultEnum.SUCCESS;
            }

            // 循环表数据拼接成参数调用API
            ApiHttpRequestDTO requestDTO = new ApiHttpRequestDTO();
            HttpRequestEnum apiRequestType = apiConfig.getApiRequestType() == "POST" ? HttpRequestEnum.POST : HttpRequestEnum.GET;
            requestDTO.setHttpRequestEnum(apiRequestType);
            requestDTO.setUri(apiConfig.getApiAddress());
            if (!StringUtils.isNotEmpty(token)) {
                requestDTO.setRequestHeader(token);
            }
            Map<String, String> headersParams = new IdentityHashMap<>();
            Map<String, String> formDataParams = new IdentityHashMap<>();
            JSONObject rawParams = new JSONObject();
            List<String> updateSqlList = new ArrayList<>();

            for (int i = 0; i < data.size(); i++) {
                headersParams.clear();
                formDataParams.clear();
                rawParams.clear();
                Map<String, Object> objectMap = data.get(i);
                Map<String, String> keyMap = new IdentityHashMap<>();
                for (Map.Entry entry : objectMap.entrySet()) {
                    // 如果是更新标识参数，跳过
                    String key = entry.getKey() != null ? entry.getKey().toString() : "";
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    boolean isContainsKey = RegexUtils.isContains(rspFieldKeys, key);
                    if (isContainsKey) {
                        keyMap.put(key, value);
                        continue;
                    }
                    if (apiConfig.getApiParamRange() == "Headers") {
                        headersParams.put(key, value);
                    } else if (apiConfig.getApiParamRange() == "Body") {
                        if (apiConfig.getApiBodyType() == "form-data") {
                            formDataParams.put(key, value);
                        } else if (apiConfig.getApiBodyType() == "raw") {
                            rawParams.put(key, value);
                        }
                    }
                }
                requestDTO.setJsonObject(rawParams);
                requestDTO.setHeadersParams(headersParams);
                requestDTO.setFormDataParams(formDataParams);
                String httpRequestResult = dataAccessClient.getHttpRequestResult(requestDTO);
                if (StringUtils.isNotEmpty(httpRequestResult)) {
                    JSONObject jsonObject = JSONObject.parseObject(httpRequestResult);
                    // 第四步：通过配置参数解析API数据
                    if (jsonObject != null) {
                        Map<String, Object> fieldMap = new IdentityHashMap<>();
                        for (int j = 0; j < apiResultConfig.size(); j++) {
                            BusinessFilterApiResultDTO apiResult = apiResultConfig.get(j);
                            if (!jsonObject.containsKey(apiResult.getSourceField())) {
                                continue;
                            }
                            if (apiResult.getPrimaryKeyField() == 1) {
                                continue;
                            }
                            String key = apiResult.getTargetField();
                            Object value = jsonObject.get(apiResult.getSourceField());
                            fieldMap.put(key, value);
                        }
                        String sqlWhere = "";
                        for (Map.Entry entry : keyMap.entrySet()) {
                            sqlWhere += " AND " + entry.getKey() + "=" + "'" + entry.getValue() + "'";
                        }
                        String updateSql = dbCommand.buildSingleUpdateSql(tableName, fieldMap, sqlWhere);
                        updateSqlList.add(updateSql);
                    }
                } else {
                    // 记录失败的数据
                    log.info("[collApi]-keyMap:", JSON.toJSONString(keyMap));
                }
            }

            // 保存更新数据
            if (CollectionUtils.isNotEmpty(updateSqlList)) {
                dbHelper.executeSql(updateSqlList, conn);
            }
        } catch (Exception ex) {
            log.error("[collApi]-ex:" + ex);
            throw new FkException(ResultEnum.DATA_QUALITY_BUSINESS_API_FILTER_EXEC_ERROR, ex);
        }
        return ResultEnum.SUCCESS;
    }

    public List<BusinessFilterApiResultDTO> tileApiRecursionResult(List<BusinessFilterApiResultDTO> source) {
        List<BusinessFilterApiResultDTO> resultList = saveApiRecursionResult(source);
        return resultList;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiResultDTO>
     * @description 递归保存API结果参数
     * @author dick
     * @date 2022/10/11 13:40
     * @version v1.0
     * @params source
     */
    public List<BusinessFilterApiResultDTO> saveApiRecursionResult(List<BusinessFilterApiResultDTO> source) {
        List<BusinessFilterApiResultDTO> resultList = new ArrayList<>();
        List<BusinessFilterApiResultDTO> authSourceList = source.stream().filter(t -> t.getResultParamType() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(authSourceList)) {
            resultList.addAll(saveApiRecursionResult("1", authSourceList));
        }
        List<BusinessFilterApiResultDTO> bodySourceList = source.stream().filter(t -> t.getResultParamType() == 2).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(bodySourceList)) {
            resultList.addAll(saveApiRecursionResult("1", bodySourceList));
        }
        return resultList;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiResultVO>
     * @description 递归查询API结果参数
     * @author dick
     * @date 2022/10/11 13:40
     * @version v1.0
     * @params source
     */
    public List<BusinessFilterApiResultVO> queryApiRecursionResult(List<BusinessFilterApiResultVO> source) {
        List<BusinessFilterApiResultVO> resultList = new ArrayList<>();
        List<BusinessFilterApiResultVO> authSourceList = source.stream().filter(t -> t.getResultParamType() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(authSourceList)) {
            resultList.addAll(queryApiRecursionResult("1", authSourceList));
        }
        List<BusinessFilterApiResultVO> bodySourceList = source.stream().filter(t -> t.getResultParamType() == 2).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(bodySourceList)) {
            resultList.addAll(queryApiRecursionResult("1", bodySourceList));
        }
        return resultList;
    }

    public List<BusinessFilterApiResultVO> queryApiRecursionResult(String parentCode, List<BusinessFilterApiResultVO> source) {
        List<BusinessFilterApiResultVO> result = new ArrayList<>();
        List<BusinessFilterApiResultVO> list = source.stream().filter(t -> t.getParentCode().equals(parentCode)).collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            BusinessFilterApiResultVO model = list.get(i);
            model.setChildren(queryApiRecursionResult(model.getCode(), source));
            result.add(model);
        }
        return result;
    }

    public List<BusinessFilterApiResultDTO> saveApiRecursionResult(String parentCode, List<BusinessFilterApiResultDTO> source) {
        List<BusinessFilterApiResultDTO> list = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            String code = UUID.randomUUID().toString().replace("-", "");
            BusinessFilterApiResultDTO model = new BusinessFilterApiResultDTO();
            model = source.get(i);
            model.setCode(code);
            model.setParentCode(parentCode);
            list.add(model);
            if (CollectionUtils.isNotEmpty(model.getChildren())) {
                list.addAll(saveApiRecursionResult(code, model.getChildren()));
            }
        }
        return list;
    }
}
