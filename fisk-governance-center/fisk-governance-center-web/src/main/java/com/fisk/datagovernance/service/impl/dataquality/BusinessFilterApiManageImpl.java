package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiConfigDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiParmDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiResultDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiConfigPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiParmPO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterApiResultPO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiParmMap;
import com.fisk.datagovernance.map.dataquality.BusinessFilterApiResultMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiParmMapper;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterApiResultMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterApiManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterApiResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.fisk.dataaccess.enums.HttpRequestEnum.POST;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/10/8 17:05
 */
@Service
public class BusinessFilterApiManageImpl extends ServiceImpl<BusinessFilterApiMapper, BusinessFilterApiConfigPO> implements IBusinessFilterApiManageService {

    @Resource
    private BusinessFilterApiParmMapper businessFilterApiParmMapper;

    @Resource
    private BusinessFilterApiResultMapper businessFilterApiResultMapper;

    @Resource
    private BusinessFilterApiParmManageImpl businessFilterApiParmManageImpl;

    @Resource
    private BusinessFilterApiResultManageImpl businessFilterApiResultManageImpl;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

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
        QueryWrapper<BusinessFilterApiParmPO> apiParmPOQueryWrapper = new QueryWrapper<>();
        apiParmPOQueryWrapper.lambda()
                .in(BusinessFilterApiParmPO::getRuleId, ruleIds)
                .eq(BusinessFilterApiParmPO::getDelFlag, 1);
        List<BusinessFilterApiParmPO> businessFilterApiParmPOS = businessFilterApiParmMapper.selectList(apiParmPOQueryWrapper);
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
                List<BusinessFilterApiParmPO> parmList = businessFilterApiParmPOS.stream().filter(parm -> parm.getApiId() == t.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(parmList)) {
                    apiVO.setApiParmConfig(BusinessFilterApiParmMap.INSTANCES.poToVo(parmList));
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
            businessFilterApiParmMapper.updateByRuleId(ruleId);
            businessFilterApiResultMapper.updateByRuleId(ruleId);
        }
        int insertCount = 0;
        BusinessFilterApiConfigPO businessFilterApiConfigPO = BusinessFilterApiMap.INSTANCES.dtoToPo(dto.getApiConfig());
        if (businessFilterApiConfigPO != null) {
            businessFilterApiConfigPO.setRuleId(ruleId);
            insertCount = baseMapper.insert(businessFilterApiConfigPO);
        }
        if (insertCount > 0) {
            if (CollectionUtils.isNotEmpty(dto.getApiParmConfig())) {
                List<BusinessFilterApiParmPO> businessFilterApiParmPOS = BusinessFilterApiParmMap.INSTANCES.dtoToPo(dto.getApiParmConfig());
                //
                businessFilterApiParmPOS.forEach(t -> {
                    t.setRuleId(ruleId);
                });
                businessFilterApiParmManageImpl.saveBatch(businessFilterApiParmPOS);
            }
            if (CollectionUtils.isNotEmpty(dto.getApiResultConfig())) {
                List<BusinessFilterApiResultDTO> resultDTOS = saveApiRecursionResult(dto.getApiResultConfig());
                List<BusinessFilterApiResultPO> businessFilterApiResultPOS = BusinessFilterApiResultMap.INSTANCES.dtoToPo(resultDTOS);
                businessFilterApiResultPOS.forEach(t -> {
                    t.setRuleId(ruleId);
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
        businessFilterApiParmMapper.updateByRuleId(ruleId);
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
        BusinessFilterApiConfigDTO apiConfig = dto.getApiInfo().getApiConfig();
        List<BusinessFilterApiParmDTO> apiParmConfig = dto.getApiInfo().getApiParmConfig().stream().filter(t -> t.getApiParmType() == 1).collect(Collectors.toList());
        List<BusinessFilterApiResultDTO> apiResultConfigList = dto.getApiInfo().getApiResultConfig().stream().filter(t -> t.getResultParmType() == 1).collect(Collectors.toList());
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
            apiHttpRequestDto.httpRequestEnum = POST;
            // 身份验证地址
            apiHttpRequestDto.uri = apiConfig.getApiAuthAddress();
            // jwt账号&密码
            JSONObject rowDataParams = new JSONObject();
            Map<String, String> formDataParams = new IdentityHashMap<>();
            if (apiConfig.getApiAuthBodyType() == "row") {
                apiParmConfig.forEach(t -> {
                    rowDataParams.put(t.getApiParmKey(), t.getApiParmValue());
                });
            } else if (apiConfig.getApiAuthBodyType() == "from-data") {
                apiParmConfig.forEach(t -> {
                    formDataParams.put(t.getApiParmKey(), t.getApiParmValue());
                });
            }
            ResultEntity<String> httpRequestResult = dataAccessClient.getHttpRequestResult(apiHttpRequestDto);
            if (httpRequestResult.getCode() == ResultEnum.SUCCESS.getCode() && StringUtils.isNotEmpty(httpRequestResult.getData())) {
                JSONObject jsonObject = JSONObject.parseObject(httpRequestResult.getData());
                token = (String) jsonObject.get(apiResultConfig.getSourceField());
                if (StringUtils.isEmpty(token)) {
                    throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
                }
            }
        }
        return ResultEntityBuild.buildData(StringUtils.isNotEmpty(token) ? ResultEnum.SUCCESS : ResultEnum.AUTH_TOKEN_IS_NOTNULL, token);
    }

    @Override
    public ResultEnum collApi(BusinessFilterDTO dto) {
        if (dto == null || dto.getApiInfo() == null) {
            return ResultEnum.PARAMTER_ERROR;
        }
        BusinessFilterApiConfigDTO apiConfig = dto.getApiInfo().getApiConfig();
        List<BusinessFilterApiParmDTO> apiParmConfig = dto.getApiInfo().getApiParmConfig().stream().filter(t -> t.getApiParmType() == 2).collect(Collectors.toList());
        List<BusinessFilterApiResultDTO> apiResultConfig = dto.getApiInfo().getApiResultConfig().stream().filter(t -> t.getResultParmType() == 2).collect(Collectors.toList());
        if (apiConfig == null || CollectionUtils.isEmpty(apiParmConfig) || CollectionUtils.isEmpty(apiResultConfig)) {
            return ResultEnum.PARAMTER_ERROR;
        }
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
                    apiParmConfig.forEach(t -> {
                        if (StringUtils.isNotEmpty(t.getApiParmValueUnique())) {
                            FiDataMetaDataTreeDTO fiDataMetaDataDTO = fiDataMetaData_Fields.stream().filter(f -> f.getId().equals(t.getApiParmValueUnique())).findFirst().orElse(null);
                            if (fiDataMetaDataDTO != null) {
                                t.setApiParmValue(fiDataMetaDataDTO.getLabel());
                            }
                        }
                    });
                }
            }
        }

        // 获取源数据
        JSONArray jsonArray = null;

        // 数据拼接成参数调用API


        // 第四步：通过配置参数解析API数据

        // 第五步：保存更新数据
        return null;
    }

    public JSONArray getData(String tableName, DataSourceConVO dataSource, List<BusinessFilterApiParmDTO> apiParmConfig) {
        JSONArray array = new JSONArray();
        try {
            String sql = "SELECT ";
            apiParmConfig.forEach(t -> {
                if (StringUtils.isNotEmpty(t.getApiParmValueUnique())) {

                }
            });

            Connection conn = getStatement(dataSource.getConType().getDriverName(), dataSource.getConStr(), dataSource.getConAccount(), dataSource.getConPassword());
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    Object value = rs.getObject(columnName);
                    jsonObj.put(columnName, value);
                }
                array.add(jsonObj);
            }
            rs.close();
        } catch (Exception ex) {
            log.error("业务清洗API清洗模板查询数据源异常，详细报错:", ex);
        }
        return array;
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
        List<BusinessFilterApiResultDTO> authSourceList = source.stream().filter(t -> t.getResultParmType() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(authSourceList)) {
            resultList.addAll(saveApiRecursionResult("1", authSourceList));
        }
        List<BusinessFilterApiResultDTO> bodySourceList = source.stream().filter(t -> t.getResultParmType() == 2).collect(Collectors.toList());
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
        List<BusinessFilterApiResultVO> authSourceList = source.stream().filter(t -> t.getResultParmType() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(authSourceList)) {
            resultList.addAll(queryApiRecursionResult("1", authSourceList));
        }
        List<BusinessFilterApiResultVO> bodySourceList = source.stream().filter(t -> t.getResultParmType() == 2).collect(Collectors.toList());
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
            if (CollectionUtils.isNotEmpty(model.getChildren())) {
                model.setChildren(queryApiRecursionResult(model.getCode(), model.getChildren()));
            }
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

    /**
     * @return java.sql.Connection
     * @description 通过配置的数据信息，建立数据库连接通道
     * @author dick
     * @date 2022/7/21 11:55
     * @version v1.0
     * @params driver
     * @params url
     * @params username
     * @params password
     */
    private Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR);
        }
        return conn;
    }
}
