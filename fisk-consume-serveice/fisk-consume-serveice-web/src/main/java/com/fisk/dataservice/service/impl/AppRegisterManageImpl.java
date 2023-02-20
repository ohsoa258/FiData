package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.core.utils.office.pdf.component.PDFHeaderFooter;
import com.fisk.common.core.utils.office.pdf.component.PDFKit;
import com.fisk.common.core.utils.office.pdf.exception.PDFException;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataservice.dto.GetConfigDTO;
import com.fisk.dataservice.dto.api.doc.*;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.ApiBuiltinParmMap;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.map.AppApiMap;
import com.fisk.dataservice.map.AppRegisterMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IAppRegisterManageService;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fisk.common.core.constants.ApiConstants.*;

/**
 * 应用接口实现类
 *
 * @author dick
 */
@Service
@Slf4j
public class AppRegisterManageImpl
        extends ServiceImpl<AppRegisterMapper, AppConfigPO>
        implements IAppRegisterManageService {

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private AppServiceConfigMapper appApiMapper;

    @Resource
    private ApiRegisterMapper apiRegisterMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiBuiltinParmMapper apiBuiltinParmMapper;

    @Resource
    private ApiBuiltinParmManageImpl apiBuiltinParmImpl;

    @Resource
    private ApiFieldMapper apiFieldMapper;

    @Resource
    GetConfigDTO getConfig;

    @Value("${dataservice.pdf.path}")
    private String templatePath;
    @Value("${dataservice.pdf.api_address}")
    private String api_address;

    @Override
    public Integer getAppCount() {
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppConfigPO::getDelFlag, 1);
        Integer appCount = baseMapper.selectCount(queryWrapper);
        if (appCount == null)
            appCount = 0;
        return appCount;
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_app_config";
        dto.filterSql = FilterSqlConstants.DS_APP_REGISTRATION_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<AppRegisterVO> pageFilter(AppRegisterQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        AppRegisterPageDTO data = new AppRegisterPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public Page<AppRegisterVO> getAll(Page<AppRegisterVO> page) {
        return baseMapper.getAll(page);
    }

    @Override
    public ResultEnum addData(AppRegisterDTO dto) {
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        // and(appName = '' or appAccount = '')
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.appName).
                        or().eq(AppConfigPO::getAppAccount, dto.appAccount))
                .eq(AppConfigPO::getDelFlag, 1);
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.appName)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.appAccount)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppConfigPO model = AppRegisterMap.INSTANCES.dtoToPo(dto);
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        model.setAppPassword(new String(base64Encrypt));
        return baseMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum editData(AppRegisterEditDTO dto) {
        AppConfigPO model = baseMapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        // and(appName = '' or appAccount = '')
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.appName).
                        or().eq(AppConfigPO::getAppAccount, dto.appAccount))
                .eq(AppConfigPO::getDelFlag, 1)
                .ne(AppConfigPO::getId, dto.id);
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.appName)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.appAccount)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppRegisterMap.INSTANCES.editDtoToPo(dto, model);
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(int id) {
        AppConfigPO model = baseMapper.selectById(id);
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        // 查询应用下是否存在api
        QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppServiceConfigPO::getAppId, id).eq(AppServiceConfigPO::getApiState, 1)
                .eq(AppServiceConfigPO::getDelFlag, 1);
        List<AppServiceConfigPO> appApiPOS = appApiMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(appApiPOS)) {
            // 该应用下没有启用的api，可以直接删除
            return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } else {
            // 应用下有已启用的api,必须先禁用api
            return ResultEnum.DS_APP_API_EXISTS;
        }
    }

    @Override
    public Page<AppApiSubVO> getSubscribeAll(AppApiSubQueryDTO dto) {
        return appApiMapper.getSubscribeAll(dto.page, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum appSubscribe(AppApiSubSaveDTO saveDTO) {
        try {
            if (saveDTO.saveType == 1) {
                // 需要删除订阅的API,验证这些API是否已经启用，启用则返回提示
                List<AppApiSubDTO> collect = saveDTO.dto.stream().filter(item -> item.apiState == ApiStateTypeEnum.Disable.getValue()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    List<Integer> collect1 = collect.stream().map(AppApiSubDTO::getApiId).collect(Collectors.toList());
                    QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().
                            in(AppServiceConfigPO::getServiceId, collect1)
                            .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.API)
                            .eq(AppServiceConfigPO::getAppId, collect.get(0).appId)
                            .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                            .eq(AppServiceConfigPO::getDelFlag, 1);
                    List<AppServiceConfigPO> appApiPOS = appApiMapper.selectList(queryWrapper);
                    if (CollectionUtils.isNotEmpty(appApiPOS))
                        return ResultEnum.DS_APP_SUBAPI_ENABLE;
                }
            }
            for (AppApiSubDTO dto : saveDTO.dto) {
                // 根据应用id和APIID查询是否存在订阅记录
                QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(AppServiceConfigPO::getAppId, dto.appId)
                        .eq(AppServiceConfigPO::getServiceId, dto.apiId)
                        .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.API.getValue())
                        .eq(AppServiceConfigPO::getDelFlag, 1);
                AppServiceConfigPO data = appApiMapper.selectOne(queryWrapper);
                if (data != null) {
                    if (saveDTO.saveType == 1) {
                        // 存在&取消订阅，删除该订阅记录
                        if (dto.apiState == ApiStateTypeEnum.Disable.getValue()) {
                            appApiMapper.deleteByIdWithFill(data);
                        }
                    } else if (saveDTO.saveType == 2) {
                        // 存在则修改状态
                        data.setApiState(data.apiState = dto.apiState);
                        data.setType(AppServiceTypeEnum.API.getValue());
                        data.setServiceId(dto.apiId);
                        appApiMapper.updateById(data);
//                        appApiMapper.updateSubscribeById(data.getApiState(), data.getType(), data.getServiceId(), data.getId());
                    }
                } else {
                    // 未勾选状态，不做任何操作
                    if (saveDTO.saveType == 1
                            && dto.apiState == ApiStateTypeEnum.Disable.getValue())
                        continue;
                    // 不存在则新增
                    AppServiceConfigPO model = AppApiMap.INSTANCES.dtoToPo(dto);
                    model.type = AppServiceTypeEnum.API.getValue();
                    model.serviceId = dto.apiId;
                    appApiMapper.insert(model);
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum resetPwd(AppPwdResetDTO dto) {
        AppConfigPO model = baseMapper.selectById(dto.appId);
        if (model == null)
            return ResultEnum.DS_APP_EXISTS;
        if (dto.appPassword.isEmpty())
            return ResultEnum.DS_APP_PWD_NOTNULL;

        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        model.setAppPassword(new String(base64Encrypt));
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum createDoc(CreateAppApiDocDTO dto, HttpServletResponse response) {
//        try {
        // 第一步：检验请求参数
        if (dto == null)
            return ResultEnum.PARAMTER_NOTNULL;
        AppConfigPO appConfigPO = baseMapper.selectById(dto.appId);
        if (appConfigPO == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
//        List<AppApiSubDTO> collect = dto.appApiDto.stream().filter(item -> item.apiState == ApiStateTypeEnum.Enable.getValue()).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(collect))
//            return ResultEnum.DS_APPAPIDOC_DISABLE;
//        dto.appApiDto = collect;
        List<AppServiceConfigPO> subscribeListByAppId = appApiMapper.getSubscribeListBy(dto.appId);
        if (CollectionUtils.isEmpty(subscribeListByAppId))
            return ResultEnum.DS_APPAPIDOC_EXISTS;
        List<Integer> apiIdList = subscribeListByAppId.stream().filter(item -> item.apiState == ApiStateTypeEnum.Enable.getValue()).map(AppServiceConfigPO::getServiceId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiIdList))
            return ResultEnum.DS_APPAPIDOC_DISABLE;
        // 第二步：查询需要生成的API接口，在第一步查询时已验证API有效性
        List<ApiConfigPO> apiList = apiRegisterMapper.getListByAppApiIds(apiIdList, dto.appId);
//        if (CollectionUtils.isEmpty(apiList)
//                || apiList.size() != apiIdList.size())
//            return ResultEnum.DS_API_EXISTS;

        // 第三步：查询API接口的请求参数
        List<Long> parmIdList = null;
        List<ParmConfigPO> parmList = apiParmMapper.getListByApiIds(apiIdList);
        if (CollectionUtils.isNotEmpty(parmList)) {
            parmIdList = parmList.stream().map(ParmConfigPO::getId).collect(Collectors.toList());
        }

        // 第四步：查询应用API内置参数
        List<BuiltinParmPO> builtinParmList = null;
        if (parmIdList != null && parmIdList.size() > 0)
            builtinParmList = apiBuiltinParmMapper.getListByWhere(dto.appId, apiIdList, parmIdList);

        // 第五步：查询API接口的返回参数
        List<FieldConfigPO> fieldList = apiFieldMapper.getListByApiIds(apiIdList);
        if (CollectionUtils.isEmpty(fieldList))
            return ResultEnum.DS_API_FIELD_EXISTS;

        // 第六步：API信息转换为文档实体
        final ApiDocDTO docDTO = createDocDTO(apiList, parmList, builtinParmList, fieldList);

        // 第七步：生成pdf，返回文件名称
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        PDFKit kit = new PDFKit();
        kit.setHeaderFooterBuilder(headerFooter);
        //设置输出路径
        long v = System.currentTimeMillis();
//        不设置目录则默认生成到/target/classes/pdf目录
//        kit.setSaveFilePath("C:\\Users\\Player\\Downloads\\pdffile\\APIServiceDoc" + v + ".pdf");
//        kit.setSaveFilePath("/root/java/dataservice/pdf/APIServiceDoc" + v + ".pdf");
//        String saveFilePath = kit.exportToFile("apiserviceTemplate.ftl", "菲斯科白泽接口文档", docDTO);
//        File file = new File(saveFilePath);
//        if (file.exists()) {
//            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, file.getName());
//        } else {
//            return ResultEntityBuild.buildData(ResultEnum.DS_APPAPIDOC_ERROR, saveFilePath);
//        }

        String fileName = null;
        try {
            fileName = new String(appConfigPO.getAppName().getBytes("UTF-8"), "ISO-8859-1") + ".pdf";
        } catch (UnsupportedEncodingException e) {
            log.error("生成API文档时，字符编码异常：" + e);
        }
        OutputStream outputStream = kit.exportToResponse("apiserviceTemplate.ftl",
                templatePath, fileName, "接口文档", docDTO, response);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new PDFException("createDoc fail", ex);
        }
//        } catch (Exception ex) {
//            return ResultEntityBuild.buildData(ResultEnum.SAVE_DATA_ERROR, ex.getMessage());
//        }
        return ResultEnum.SUCCESS;
    }

//    @Override
//    public ResponseEntity downloadDoc(String fileName) {
//        Path path = Paths.get("C:/Users/Player/Downloads/pdffile", fileName);
//        File file = new File(path.toString());
//        if (!file.exists())
//            return null;
//        InputStream in = null;
//        try {
//            in = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        final HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/pdf");
//        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
//        try {
//            return new ResponseEntity(IOUtils.toByteArray(in), headers, HttpStatus.OK);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public List<AppApiParmVO> getParmAll(AppApiParmQueryDTO dto) {
        List<AppApiParmVO> appApiParmList = new ArrayList<>();
        QueryWrapper<ParmConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ParmConfigPO::getApiId, dto.apiId)
                .eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> selectList = apiParmMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            // 查询已设置内置参数的parm
            QueryWrapper<BuiltinParmPO> builtinParmQuery = new QueryWrapper<>();
            builtinParmQuery.lambda()
                    .eq(BuiltinParmPO::getApiId, dto.apiId)
                    .eq(BuiltinParmPO::getAppId, dto.appId)
                    .eq(BuiltinParmPO::getDelFlag, 1);
            List<BuiltinParmPO> builtinParmList = apiBuiltinParmMapper.selectList(builtinParmQuery);
            if (CollectionUtils.isNotEmpty(builtinParmList)) {
                for (ParmConfigPO parmConfigPO : selectList) {
                    Optional<BuiltinParmPO> builtinParmOptional = builtinParmList.stream().filter(item -> item.getParmId() == parmConfigPO.id).findFirst();
                    if (builtinParmOptional.isPresent()) {
                        // 存在
                        BuiltinParmPO builtinParm = builtinParmOptional.get();
                        parmConfigPO.setParmValue(builtinParm.parmValue);
                        parmConfigPO.setParmDesc(builtinParm.parmDesc);
                        parmConfigPO.setParmIsbuiltin(builtinParm.parmIsbuiltin);
                    }
                }
            }
            appApiParmList = ApiParmMap.INSTANCES.listPoToAppApiParmVo(selectList);
        }
        return appApiParmList;
    }

    @Override
    public ResultEnum setParm(AppApiBuiltinParmEditDTO dto) {
        ApiConfigPO apiModel = apiRegisterMapper.selectById(dto.apiId);
        if (apiModel == null)
            return ResultEnum.DS_API_EXISTS;
        AppConfigPO appModel = baseMapper.selectById(dto.appId);
        if (appModel == null)
            return ResultEnum.DS_APP_EXISTS;

        // 删除此应用API下的所有内置参数，再新增
        int updateCount = apiBuiltinParmMapper.updateBySearch(dto.appId, dto.apiId);

        List<BuiltinParmPO> builtinParmList = ApiBuiltinParmMap.INSTANCES.listDtoToPo(dto.parmList);
        if (CollectionUtils.isNotEmpty(builtinParmList)) {
            return apiBuiltinParmImpl.saveBatch(builtinParmList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 生成API文档DTO
     *
     * @param apiList           API信息
     * @param parmasList        API参数信息
     * @param builtinParamsList API内置参数信息
     * @param fieldList         API字段信息
     * @return
     */
    private ApiDocDTO createDocDTO(List<ApiConfigPO> apiList,
                                   List<ParmConfigPO> paramsList,
                                   List<BuiltinParmPO> builtinParamsList,
                                   List<FieldConfigPO> fieldList) {
        ApiDocDTO apiDocDTO = new ApiDocDTO();
        // API文档基础信息
        String jsonResult = DATASERVICE_APIBASICINFO.replace("{api_prd_address}", api_address);
        // log.info("createDocDTO jsonInfo："+jsonResult);
        apiDocDTO = JSON.parseObject(jsonResult, ApiDocDTO.class);
        // API文档代码示例 c#
        apiDocDTO.apiCodeExamples_net = DATASERVICE_APICODEEXAMPLES_NET.replace("{api_prd_address}", api_address);
        // API文档代码示例 java
        apiDocDTO.apiCodeExamples_java = DATASERVICE_APICODEEXAMPLES_JAVA.replace("{api_prd_address}", api_address);

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

        BigDecimal catalogueIndex = new BigDecimal("2.4");
        List<ApiBasicInfoDTO> apiBasicInfoDTOS = new ArrayList<>();
        for (int i = 0; i < apiList.size(); i++) {

            ApiConfigPO apiConfigPO = apiList.get(i);

            /* 设置目录 start */
            ApiCatalogueDTO apiCatalogueDTO = new ApiCatalogueDTO();
            BigDecimal incrementIndex = new BigDecimal("0.1");
            BigDecimal addIndex = catalogueIndex.add(incrementIndex);
            apiCatalogueDTO.grade = 3;
            apiCatalogueDTO.catalogueIndex = addIndex + ".";
            apiCatalogueDTO.catalogueName = apiConfigPO.apiName;
            apiDocDTO.apiCatalogueDTOS.add(apiDocDTO.apiCatalogueDTOS.size() - 3, apiCatalogueDTO);
            catalogueIndex = addIndex;
            /* 设置目录 end */

            /* 设置API基础信息 start */
            ApiBasicInfoDTO apiBasicInfoDTO = new ApiBasicInfoDTO();
            apiBasicInfoDTO.apiName = apiConfigPO.apiName;
            apiBasicInfoDTO.apiAddress = "/dataservice/apiService/getData";
            apiBasicInfoDTO.apiDesc = apiConfigPO.apiDesc;
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

            // 请求参数新增parmList参数说明
            requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("parmList");
            requestDTO.setIsRequired("否");
            requestDTO.setParmType("HashMap");
            requestDTO.setParmDesc("API参数列表，详情见parmList参数说明，默认为null");
            requestDTO.setTrStyle(trReqIndex_fixed[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff");
            apiRequestDTOS_fixed.add(requestDTO);
            trReqIndex_fixed[0]++;

            // 请求参数新增分页参数说明
            requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("current");
            requestDTO.setIsRequired("否");
            requestDTO.setParmType("Integer"); //String特指这个类型，string适用于引用对象
            requestDTO.setParmDesc("页码，第1页开始，默认为null");
            requestDTO.setTrStyle(trReqIndex_fixed[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff");
            apiRequestDTOS_fixed.add(requestDTO);
            trReqIndex_fixed[0]++;

            // 请求参数新增分页参数说明
            requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("size");
            requestDTO.setIsRequired("否");
            requestDTO.setParmType("Integer"); //String特指这个类型，string适用于引用对象
            requestDTO.setParmDesc("页数，建议每页500条，默认为null");
            requestDTO.setTrStyle(trReqIndex_fixed[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff");
            apiRequestDTOS_fixed.add(requestDTO);
            trReqIndex_fixed[0]++;

            apiBasicInfoDTO.apiRequestDTOS_Fixed = apiRequestDTOS_fixed;

            if (CollectionUtils.isNotEmpty(paramsList)) {
                List<ParmConfigPO> collect = paramsList.stream().filter(item -> item.apiId == apiConfigPO.id).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    collect.forEach(e -> {
                        Optional<BuiltinParmPO> builtinParamsOptional = builtinParamsList.stream().filter(item -> item.getParmId() == e.id).findFirst();
                        // 不是内置参数，在文档中体现
                        if (!builtinParamsOptional.isPresent()) {
                            ApiRequestDTO apiRequestDTO = new ApiRequestDTO();
                            apiRequestDTO.parmName = e.parmName;
                            apiRequestDTO.isRequired = "是";
                            apiRequestDTO.parmType = "String"; //String特指这个类型，string适用于引用对象
                            apiRequestDTO.parmDesc = e.parmDesc;
                            apiRequestDTO.trStyle = trReqIndex[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
                            apiRequestDTOS.add(apiRequestDTO);
                            trReqIndex[0]++;
                        }
                    });
                }
            }
            apiBasicInfoDTO.apiRequestDTOS = apiRequestDTOS;
            apiBasicInfoDTO.apiRequestExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"apiCode\": \"xxx\",\n" +
                    " &nbsp;&nbsp;\"parmList\": {      --HashMap\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"parm1\": \"value\" --%s\n" +
                    " &nbsp;&nbsp;},\n" +
                    " &nbsp;&nbsp;\"current\": null,\n" +
                    " &nbsp;&nbsp;\"size\": null\n" +
                    "}", addIndex + ".7");
            /* 设置API请求参数 end */

            /* 设置API返回参数 start */
            List<ApiResponseDTO> apiResponseDTOS = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(fieldList)) {
                final int[] trIndex = {1};
                List<FieldConfigPO> collect = fieldList.stream().filter(item -> item.apiId == apiConfigPO.id).sorted(Comparator.comparing(FieldConfigPO::getFieldSort)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    collect.forEach(e -> {
                        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
                        apiResponseDTO.parmName = e.fieldName;
                        apiResponseDTO.parmType = e.fieldType;
                        apiResponseDTO.parmDesc = e.fieldDesc;
                        apiResponseDTO.trStyle = trIndex[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
                        apiResponseDTOS.add(apiResponseDTO);
                        trIndex[0]++;
                    });
                }
            }
            apiBasicInfoDTO.apiResponseHeaderDesc = "返回参数（dataArray）说明";
            apiBasicInfoDTO.apiResponseDTOS = apiResponseDTOS;
            apiBasicInfoDTO.apiResponseExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"code\":200,\n" +
                    " &nbsp;&nbsp;\"data\":{\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"current\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"size\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"total\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"page\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"dataArray\":[] --%s\n" +
                    " &nbsp;&nbsp;},\n" +
                    " &nbsp;&nbsp;\"msg\":\"xxx\"\n" +
                    "}", addIndex + ".9");
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
