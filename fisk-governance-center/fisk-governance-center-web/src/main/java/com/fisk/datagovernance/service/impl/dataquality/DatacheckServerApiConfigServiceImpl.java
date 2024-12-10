package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.core.utils.office.pdf.component.PDFHeaderFooter;
import com.fisk.common.core.utils.office.pdf.component.PDFKit;
import com.fisk.common.core.utils.office.pdf.exception.PDFException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import com.fisk.datagovernance.map.dataquality.DatacheckServerFieldConfigMap;
import com.fisk.datagovernance.mapper.dataquality.DataCheckExtendMapper;
import com.fisk.datagovernance.mapper.dataquality.DatacheckServerApiConfigMapper;
import com.fisk.datagovernance.mapper.dataquality.DatacheckServerAppConfigMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.DatacheckServerApiConfigService;
import com.fisk.datagovernance.service.dataquality.DatacheckServerFieldConfigService;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiFieldServerVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.ApiSeverSubVO;
import com.fisk.datagovernance.dto.dataquality.datacheck.apidoc.*;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppServiceCountVO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.RequstDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.api.TokenDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.api.ResponseVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.common.core.constants.ApiConstants.*;

@Service("datacheckServerApiConfigService")
public class DatacheckServerApiConfigServiceImpl extends ServiceImpl<DatacheckServerApiConfigMapper, DatacheckServerApiConfigPO> implements DatacheckServerApiConfigService {


    @Resource
    DatacheckServerAppConfigMapper appConfigMapper;

    @Resource
    DatacheckServerApiConfigMapper apiConfigMapper;

    @Resource
    DatacheckServerFieldConfigService fieldConfigService;

    @Resource
    DataCheckManageImpl dataCheckManage;
    @Resource
    private AuthClient authClient;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserHelper userHelper;
    @Value("${dataquality.pdf.path}")
    private String templatePath;
    @Value("${dataquality.pdf.api_address}")
    private String api_address;
    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        String token = null;
        // 第一步：验证账号密码是否有效
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        String pwd = new String(base64Encrypt);
        Integer id = (Integer)redisUtil.get(RedisKeyEnum.DATA_CHECK_SERVER_APP_ID  +":"+  dto.getAppAccount() + pwd);
        Long uniqueId = null;
        if (id != null){
            Long appId = Long.valueOf(id);
            uniqueId = appId + RedisTokenKey.DATA_CHECK_SERVICE_TOKEN;
        }else {
            DatacheckServerAppConfigPO byAppInfo = appConfigMapper.getByAppInfo(dto.appAccount, pwd);
            if (byAppInfo == null)
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS, token);
            redisUtil.set(RedisKeyEnum.DATA_SERVER_APP_ID +":"+ dto.getAppAccount() + pwd,byAppInfo.id, RedisKeyEnum.AUTH_USERINFO.getValue());
            uniqueId = byAppInfo.id + RedisTokenKey.DATA_CHECK_SERVICE_TOKEN;
        }
        // 第二步：获取缓存中的token，未过期则刷新过期时间
        UserInfo userInfo = (UserInfo) redisUtil.get(RedisKeyBuild.buildLoginUserInfo(uniqueId));
        if (userInfo != null && StringUtils.isNotEmpty(userInfo.getToken())) {
            boolean isRefresh = redisUtil.expire(RedisKeyBuild.buildLoginUserInfo(uniqueId), RedisKeyEnum.AUTH_USERINFO.getValue());
            if (isRefresh)
                return ResultEntityBuild.buildData(ResultEnum.REQUEST_SUCCESS, userInfo.getToken());
        }
        // 第三步：调用授权接口，根据账号密码生成token
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(dto.appAccount);
        userAuthDTO.setPassword(dto.appPassword);
        userAuthDTO.setTemporaryId(uniqueId);
        ResultEntity<String> tokenEntity = authClient.getToken(userAuthDTO);
        if (tokenEntity.code == 0 && !tokenEntity.data.isEmpty())
            token = tokenEntity.data;
        return ResultEntityBuild.buildData(ResultEnum.REQUEST_SUCCESS, token);
    }

    @Override
    public ResultEntity<Object> getData(RequstDTO dto) {
        return dataCheckManage.getData(dto);
    }

    @Override
    public PageDTO<ApiSeverSubVO> getApiSubAll(ApiSubQueryDTO dto) {
        Integer apiSubAllCount = apiConfigMapper.getApiSubAllCount(dto);
        List<ApiSeverSubVO> apiSubAll = apiConfigMapper.getApiSubAll(dto,(dto.current-1) * dto.size,dto.size);
        List<Integer> apiIds = apiSubAll.stream().map(i -> i.getId()).collect(Collectors.toList());

        LambdaQueryWrapper<DatacheckServerFieldConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DatacheckServerFieldConfigPO::getApiId, apiIds);
        List<DatacheckServerFieldConfigPO> fieldList = fieldConfigService.list(queryWrapper);
        Map<Integer, List<DatacheckServerFieldConfigPO>> fieldMap = fieldList.stream().collect(Collectors.groupingBy(DatacheckServerFieldConfigPO::getApiId));

        List<ApiSeverSubVO> result = apiSubAll.stream().map(i -> {
            List<DatacheckServerFieldConfigPO> datacheckServerFieldConfigPOS = fieldMap.get(i.getId());
            if (!CollectionUtils.isEmpty(datacheckServerFieldConfigPOS)) {
                List<ApiFieldServerVO> apiFieldServerVOS = DatacheckServerFieldConfigMap.INSTANCES.poListToVoList(datacheckServerFieldConfigPOS);
                i.setFieldList(apiFieldServerVOS);
            }
            return i;
        }).collect(Collectors.toList());
        PageDTO page = new PageDTO<>();
        page.setItems(result);
        page.setTotal(apiSubAllCount.longValue());
        page.setTotalPage((long)dto.current);
        return page;
    }

    @Override
    public ResultEnum editApiField(ApiFieldEditDTO dto) {
        if (dto.getFieldDTOS() == null || dto.getFieldDTOS().isEmpty()){
            return ResultEnum.DS_APISERVICE_API_FIELD_EMPTY;
        }
        LambdaQueryWrapper<DatacheckServerFieldConfigPO> del = new LambdaQueryWrapper<>();
        del.eq(DatacheckServerFieldConfigPO::getApiId,dto.getApiId());
        fieldConfigService.remove(del);
        fieldConfigService.saveBatch(DatacheckServerFieldConfigMap.INSTANCES.dtoListToPoList(dto.getFieldDTOS()));
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum editApiState(ApiStateDTO dto) {
        DatacheckServerApiConfigPO datacheckServerApiConfigPO = new DatacheckServerApiConfigPO();
        datacheckServerApiConfigPO.setId(dto.getApiId());
        datacheckServerApiConfigPO.setApiState(dto.getApiState());
        apiConfigMapper.updateById(datacheckServerApiConfigPO);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum resetPwd(AppPwdResetDTO dto) {
        DatacheckServerAppConfigPO model = appConfigMapper.selectById(dto.appId);
        if (model == null)
            return ResultEnum.DS_APP_EXISTS;
        if (dto.appPassword.isEmpty())
            return ResultEnum.DS_APP_PWD_NOTNULL;

        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        model.setAppPassword(new String(base64Encrypt));
        return appConfigMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum createDoc(CreateAppApiDocDTO dto, HttpServletResponse response) {
        //        try {
        // 第一步：检验请求参数
        if (dto == null)
            return ResultEnum.PARAMTER_NOTNULL;
        DatacheckServerAppConfigPO appConfigPO = appConfigMapper.selectById(dto.appId);
        if (appConfigPO == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
//        List<AppApiSubDTO> collect = dto.appApiDto.stream().filter(item -> item.apiState == ApiStateTypeEnum.Enable.getValue()).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(collect))
//            return ResultEnum.DS_APPAPIDOC_DISABLE;
//        dto.appApiDto = collect;
        List<DatacheckServerApiConfigPO> subscribeListByAppId = apiConfigMapper.getSubscribeListBy(dto.appId);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(subscribeListByAppId))
            return ResultEnum.DS_APPAPIDOC_EXISTS;
        List<Integer> ruleIdList = subscribeListByAppId.stream().filter(item -> item.getApiState() == ApiStateTypeEnum.Enable.getValue()).map(DatacheckServerApiConfigPO::getCheckRuleId).collect(Collectors.toList());
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(ruleIdList))
            return ResultEnum.DS_APPAPIDOC_DISABLE;
        // 第二步：查询需要生成的API接口，在第一步查询时已验证API有效性
        LambdaQueryWrapper<DataCheckPO> queryApiWrapper = new LambdaQueryWrapper<>();
        queryApiWrapper.in(DataCheckPO::getId, ruleIdList);
        List<DataCheckPO> checkRules = dataCheckManage.list(queryApiWrapper);
        // 第三步：查询API接口的返回参数
        List<Long> apiIdList = subscribeListByAppId.stream().filter(item -> item.getApiState() == ApiStateTypeEnum.Enable.getValue()).map(DatacheckServerApiConfigPO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<DatacheckServerFieldConfigPO> queryFieldWrapper = new LambdaQueryWrapper<>();
        queryFieldWrapper.in(DatacheckServerFieldConfigPO::getApiId, apiIdList);
        List<DatacheckServerFieldConfigPO> fieldList = fieldConfigService.list(queryFieldWrapper);
        if (CollectionUtils.isEmpty(fieldList)) {
            return ResultEnum.DS_API_FIELD_EXISTS;
        }
        // 第五步：查询API接口的请求参数
        List<DatacheckServerFieldConfigPO> paramList = fieldList.stream().filter(i -> i.getReturnFlag() != 1).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paramList)) {
            return ResultEnum.DS_API_FIELD_EXISTS;
        }
        // 第六步：API信息转换为文档实体
        final ApiDocDTO docDTO = createDocDTO(appConfigPO,subscribeListByAppId, checkRules, paramList, fieldList);

        // 第七步：生成pdf，返回文件名称
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        PDFKit kit = new PDFKit();
        kit.setHeaderFooterBuilder(headerFooter);
        //设置输出路径
        long v = System.currentTimeMillis();

        String fileName = "APIServiceDoc" + v + ".pdf";
        OutputStream outputStream = kit.exportToResponse("apiserviceTemplate.ftl",
                templatePath, fileName, "接口文档", docDTO, response);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new PDFException("createDoc fail", ex);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<AppServiceCountVO> getApiAppServiceCount() {
        return baseMapper.getApiAppServiceCount();
    }

    @Override
    public ResultEnum delApi(Integer apiId) {
        apiConfigMapper.deleteById(apiId);
        LambdaQueryWrapper<DatacheckServerFieldConfigPO> delete = new LambdaQueryWrapper<>();
        delete.eq(DatacheckServerFieldConfigPO::getApiId,apiId);
        fieldConfigService.remove(delete);
        return ResultEnum.SUCCESS;
    }


    private ApiDocDTO createDocDTO(DatacheckServerAppConfigPO appConfig,
                                   List<DatacheckServerApiConfigPO> apiList,
                                   List<DataCheckPO> checkRules,
                                   List<DatacheckServerFieldConfigPO> paramsList,
                                   List<DatacheckServerFieldConfigPO> fieldList) {
        ApiDocDTO apiDocDTO = new ApiDocDTO();

        Map<Long, DataCheckPO> ruleMaps = checkRules.stream().collect(Collectors.toMap(DataCheckPO::getId, i ->i));
        // API文档基础信息
        String jsonResult = GOVERNANCE_APIBASICINFO.replace("{api_prd_address}", api_address)
                .replace("{apiVersion_StartDate}", DateTimeUtils.getNowToShortDate())
                .replace("{apiVersion_EndDate}", DateTimeUtils.getNowToShortDate())
                .replace("{apiVersion_Modified}", appConfig.getAppPrincipal())
                .replace("{release_Date}", DateTimeUtils.getNowToShortDate().replace("-", ""));

        // log.info("createDocDTO jsonInfo："+jsonResult);
        apiDocDTO = JSON.parseObject(jsonResult, ApiDocDTO.class);
        // API文档代码示例 c#
        apiDocDTO.apiCodeExamples_net = GOVERNANCE_APICODEEXAMPLES_NET.replace("{api_prd_address}", api_address);

        // API文档代码示例 java
        apiDocDTO.apiCodeExamples_java = GOVERNANCE_APICODEEXAMPLES_JAVA.replace("{api_prd_address}", api_address);

        apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestExamples = "{\n" +
                "&nbsp;&nbsp; \"appAccount\": \"xxx\",\n" +
                "&nbsp;&nbsp; \"appPassword\": \"xxx\"\n" +
                "}";
        apiDocDTO.apiBasicInfoDTOS.get(0).apiResponseExamples = String.format("{\n" +
                "&nbsp;&nbsp; \"code\": 200,\n" +
                "&nbsp;&nbsp; \"data\": \"xxx\", --%s\n" +
                "&nbsp;&nbsp; \"msg\": \"xxx\"\n" +
                "}", "2.4.9");
        // 特殊处理获取token接口的请求参数
        apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestDTOS_Fixed = apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestDTOS;
        apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestDTOS = new ArrayList<>();

//        apiDocDTO.apiBasicInfoDTOS.get(1).apiRequestExamples = "{\n" +
//                "&nbsp;&nbsp; \"apiCode\": \"xxx\",\n" +
//                "}";
//        apiDocDTO.apiBasicInfoDTOS.get(1).apiResponseExamples = String.format("{\n" +
//                "&nbsp;&nbsp; \"code\": 200,\n" +
//                "&nbsp;&nbsp; \"data\": \"xxx\", --%s\n" +
//                "&nbsp;&nbsp; \"msg\": \"xxx\"\n" +
//                "}", "2.4.9");

        BigDecimal catalogueIndex = new BigDecimal("2.5");
        List<ApiBasicInfoDTO> apiBasicInfoDTOS = new ArrayList<>();
        for (int i = 0; i < apiList.size(); i++) {

            DatacheckServerApiConfigPO apiConfigPO = apiList.get(i);
            DataCheckPO dataCheckPO = ruleMaps.get(apiConfigPO.getCheckRuleId().longValue());
            /* 设置目录 start */
            ApiCatalogueDTO apiCatalogueDTO = new ApiCatalogueDTO();
            BigDecimal incrementIndex = new BigDecimal("0.1");
            BigDecimal addIndex = catalogueIndex.add(incrementIndex);
            apiCatalogueDTO.grade = 3;
            apiCatalogueDTO.catalogueIndex = addIndex + ".";
            apiCatalogueDTO.catalogueName = dataCheckPO.ruleName;
            apiDocDTO.apiCatalogueDTOS.add(apiDocDTO.apiCatalogueDTOS.size() - 3, apiCatalogueDTO);
            catalogueIndex = addIndex;
            /* 设置目录 end */

            /* 设置API基础信息 start */
            ApiBasicInfoDTO apiBasicInfoDTO = new ApiBasicInfoDTO();
            apiBasicInfoDTO.apiName = dataCheckPO.ruleName;
            apiBasicInfoDTO.apiAddress = "/datagovernance/datacheckapi/getData";
            apiBasicInfoDTO.apiDesc = apiConfigPO.getApiDesc();
            apiBasicInfoDTO.apiRequestType = "POST";
            apiBasicInfoDTO.apiContentType = "application/json";
            apiBasicInfoDTO.apiHeader = "Authorization: Bearer {token}";
            /* 设置API基础信息 end */

            /* 设置API请求参数 start */
            List<ApiRequestDTO> apiRequestDTOS = new ArrayList<>();
            List<ApiRequestDTO> apiRequestDTOS_fixed = new ArrayList<>();
            final int[] trReqIndex = {1};
            final int[] trReqIndex_fixed = {1};

            // 请求参数新增api标识
            ApiRequestDTO requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("apiCode");
            requestDTO.setIsRequired("是");
            requestDTO.setParmType("String"); //String特指这个类型，string适用于引用对象
            requestDTO.setParmDesc(String.format("API标识：%s (真实数据)", apiConfigPO.getApiCode()));
            requestDTO.setTrStyle(trReqIndex_fixed[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff");
            apiRequestDTOS_fixed.add(requestDTO);
            trReqIndex_fixed[0]++;

            String flag = "否";
            if (!CollectionUtils.isEmpty(paramsList)) {
                List<DatacheckServerFieldConfigPO> collect = paramsList.stream().filter(v -> v.getApiId() == apiConfigPO.id).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect)){
                    flag = "是";
                }
            }
            requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("data");

            requestDTO.setIsRequired(flag);
            requestDTO.setParmType("List&lt;HashMap&gt;");
            requestDTO.setParmDesc("API参数列表，详情见data参数说明");
            requestDTO.setTrStyle(trReqIndex_fixed[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff");
            apiRequestDTOS_fixed.add(requestDTO);
            trReqIndex_fixed[0]++;
            apiBasicInfoDTO.apiRequestDTOS_Fixed = apiRequestDTOS_fixed;

            apiBasicInfoDTO.apiRequestDTOS = apiRequestDTOS;
            apiBasicInfoDTO.apiRequestExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"apiCode\": \"xxx\",\n" +
                    " &nbsp;&nbsp;\"data\": [{      --List&lt;HashMap&gt;\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"parm1\": \"value\" --%s\n" +
                    " &nbsp;&nbsp;}]\n" +
                    "}", addIndex + ".7");
            /* 设置API请求参数 end */

            /* 设置API返回参数 start */
            List<ApiResponseDTO> apiResponseDTOS = new ArrayList<>();
            final int[] trIndex_data = {1};
            ApiResponseDTO apiResponseDTO = new ApiResponseDTO();

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("data");
            apiResponseDTO.setParmType("List&lt;HashMap&gt;");
            apiResponseDTO.setParmDesc("返回结果对象列表");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;
            List<ApiResponseDTO> apiResponseDataArrays = new ArrayList<>();
            if (!CollectionUtils.isEmpty(fieldList)) {
                final int[] trIndex = {1};
                List<DatacheckServerFieldConfigPO> collect = fieldList.stream().filter(item -> item.getApiId() == apiConfigPO.id).sorted(Comparator.comparing(DatacheckServerFieldConfigPO::getCreateTime)).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(collect)) {
                    collect.forEach(e -> {
                        ApiResponseDTO apiResponseDataArray = new ApiResponseDTO();
                        apiResponseDataArray.parmName = e.getFieldName();
                        apiResponseDataArray.parmType = e.getFieldType();
                        apiResponseDataArray.parmDesc = e.getFieldDesc();
                        if (e.getReturnFlag() == 1){
                            apiResponseDataArray.parmReturnFlag = "是";
                        }else {
                            apiResponseDataArray.parmReturnFlag = "否";
                        }
                        apiResponseDataArray.trStyle = trIndex[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
                        apiResponseDataArrays.add(apiResponseDataArray);
                        trIndex[0]++;
                    });
                }
            }
            apiBasicInfoDTO.apiResponseDataArray = apiResponseDataArrays;
            apiBasicInfoDTO.apiResponseHeaderDesc = "返回参数说明";
            apiBasicInfoDTO.apiResponseDTOS = apiResponseDTOS;
            apiBasicInfoDTO.apiResponseExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"code\":200,\n" +
                    " &nbsp;&nbsp;\"data\":[{\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"parm1\": \"value\" --%s\n" +
                    " &nbsp;&nbsp;}],\n" +
                    " &nbsp;&nbsp;\"msg\":\"xxx\"\n" +
                    "}", addIndex + ".8");
            /* 设置API返回参数 end */

            /* 设置API目录 start */
            apiBasicInfoDTO.apiNameCatalogue = addIndex + ".";
            apiBasicInfoDTO.apiAddressCatalogue = addIndex + ".1";
            apiBasicInfoDTO.apiDescCatalogue = addIndex + ".2";
            apiBasicInfoDTO.apiRequestTypeCatalogue = addIndex + ".3";
            apiBasicInfoDTO.apiContentTypeCatalogue = addIndex + ".4";
            apiBasicInfoDTO.apiHeaderCatalogue = addIndex + ".5";
            apiBasicInfoDTO.apiRequestExamplesCatalogue = addIndex + ".6";
            apiBasicInfoDTO.apiRequestCatalogue = addIndex + ".7";
            apiBasicInfoDTO.apiResponseExamplesCatalogue = addIndex + ".8";
            apiBasicInfoDTO.apiResponseCatalogue = addIndex + ".9";
            /* 设置API目录 end */

            apiBasicInfoDTOS.add(apiBasicInfoDTO);
        }
        apiDocDTO.apiBasicInfoDTOS.addAll(apiBasicInfoDTOS);
        return apiDocDTO;
    }
}
