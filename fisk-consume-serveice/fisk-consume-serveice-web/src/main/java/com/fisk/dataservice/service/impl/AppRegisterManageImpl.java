package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.core.utils.office.pdf.component.PDFHeaderFooter;
import com.fisk.common.core.utils.office.pdf.component.PDFKit;
import com.fisk.common.core.utils.office.pdf.exception.PDFException;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataColumnAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datamanage.client.DataManageClient;
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
import com.fisk.dataservice.vo.api.FieldConfigVO;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import com.fisk.dataservice.vo.appcount.AppServiceCountVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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
    private  DataSourceConMapper dataSourceConMapper;

    @Resource
    private ApiBuiltinParmMapper apiBuiltinParmMapper;

    @Resource
    private ApiBuiltinParmManageImpl apiBuiltinParmImpl;

    @Resource
    private ApiFieldMapper apiFieldMapper;

    @Resource
    private ApiRegisterManageImpl apiRegisterManage;

    @Resource
    private DataManageClient dataManageClient;

    @Resource
    GetConfigDTO getConfig;

    @Value("${dataservice.pdf.path}")
    private String templatePath;
    @Value("${dataservice.pdf.api_address}")
    private String api_address;
    @Value("${dataservice.proxyservice.api_address}")
    private String proxyServiceApiAddress;
    @Value("${open-metadata}")
    private Boolean openMetadata;

    @Resource
    private RedisUtil redisUtil;


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
        dto.url = getConfig.getUrl();
        dto.userName = getConfig.getUsername();
        dto.password = getConfig.getPassword();
        dto.driver = getConfig.getDriver();
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

        int totalCount = 0;
        Page<AppRegisterVO> registerVOPage = baseMapper.filter(query.page, data);
        if (registerVOPage != null && CollectionUtils.isNotEmpty(registerVOPage.getRecords())) {
            // 查询应用下的API个数
            List<AppServiceCountVO> appServiceCount = appApiMapper.getApiAppServiceCount();
            for (AppRegisterVO appRegisterVO : registerVOPage.getRecords()) {
                if (CollectionUtils.isNotEmpty(appServiceCount)) {
                    AppServiceCountVO appServiceCountVO = appServiceCount.stream().filter(k -> k.getAppId() == appRegisterVO.getId()).findFirst().orElse(null);
                    if (appServiceCountVO != null) {
                        appRegisterVO.setItemCount(appServiceCountVO.getCount());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(appServiceCount)) {
                totalCount = appServiceCount.stream().collect(Collectors.summingInt(AppServiceCountVO::getCount));
                registerVOPage.getRecords().get(0).setTotalCount(totalCount);
            }
        }
        return registerVOPage;
    }

    @Override
    public Page<AppRegisterVO> getAll(Page<AppRegisterVO> page) {
        return baseMapper.getAll(page);
    }

    @Override
    public ResultEnum addData(AppRegisterDTO dto) {
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.getAppName()).
                        or().eq(AppConfigPO::getAppAccount, dto.getAppAccount()))
                .eq(AppConfigPO::getDelFlag, 1);
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.getAppName())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppName()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.getAppAccount())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppAccount()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppConfigPO model = AppRegisterMap.INSTANCES.dtoToPo(dto);
        if (StringUtils.isNotEmpty(dto.getAppPassword())) {
            byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.getAppPassword());
            model.setAppPassword(new String(base64Encrypt));
        }
        int insert = baseMapper.insert(model);
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(model.appPassword);
        String pwd = new String(base64Encrypt);
        redisUtil.set(RedisKeyEnum.DATA_SERVER_APP_ID +":"+ model.getAppAccount() + pwd,model.getId(), RedisKeyEnum.AUTH_USERINFO.getValue());
        if (insert > 0) {
            //同步元数据业务分类
            if (openMetadata) {
                ClassificationInfoDTO apiServiceByAppName = getApiServiceByAppName(dto.getAppName());
                if (apiServiceByAppName != null) {
                    apiServiceByAppName.setDelete(false);
                    dataManageClient.appSynchronousClassification(apiServiceByAppName);
                }
            }
            return ResultEnum.SUCCESS;

        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ResultEnum editData(AppRegisterEditDTO dto) {
        AppConfigPO model = baseMapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.getAppName()).
                        or().eq(AppConfigPO::getAppAccount, dto.getAppAccount()))
                .eq(AppConfigPO::getDelFlag, 1)
                .ne(AppConfigPO::getId, dto.getId());
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.getAppName())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppName()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.getAppAccount())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppAccount()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppRegisterMap.INSTANCES.editDtoToPo(dto, model);
        if (dto.getProxyAuthorizationSwitch() != 1) {
            model.setAppAccount("");
            model.setAppPassword("");
        }
        int i = baseMapper.updateById(model);
        if (i > 0) {
            //同步元数据业务分类
            if (openMetadata) {
                ClassificationInfoDTO apiServiceByAppName = getApiServiceByAppName(dto.getAppName());
                if (apiServiceByAppName != null) {
                    apiServiceByAppName.setDelete(false);
                    dataManageClient.appSynchronousClassification(apiServiceByAppName);
                }
            }
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }


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
                .eq(AppServiceConfigPO::getDelFlag, 1)
                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.API.getValue());
        List<AppServiceConfigPO> appApiPOS = appApiMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(appApiPOS)) {

            // 该应用下没有启用的api，可以直接删除
            if (baseMapper.deleteByIdWithFill(model) > 0) {
                if (openMetadata) {
                    //同步元数据业务分类
                    ClassificationInfoDTO classificationInfoDTO = new ClassificationInfoDTO();
                    classificationInfoDTO.setName(model.getAppName());
                    classificationInfoDTO.setDescription(model.getAppDesc());
                    classificationInfoDTO.setSourceType(ClassificationTypeEnum.API_GATEWAY_SERVICE);
                    classificationInfoDTO.setDelete(true);
                    dataManageClient.appSynchronousClassification(classificationInfoDTO);
                }
                return ResultEnum.SUCCESS;
            } else {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        } else {
            // 应用下有已启用的api,必须先禁用api
            return ResultEnum.DS_APP_API_EXISTS;
        }

    }

    @Override
    public Page<AppApiSubVO> getSubscribeAll(AppApiSubQueryDTO dto) {
        Page<AppApiSubVO> all = appApiMapper.getSubscribeAll(dto.page, dto);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            all.getRecords().forEach(e -> {
                e.setApiProxyCallUrl(proxyServiceApiAddress + "/" + e.getApiCode());
            });
        }
        return all;
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

            log.info("数据服务【appSubscribe】开始执行元数据同步");
            //同步元数据
            if (openMetadata) {
                List<Long> apiIds = saveDTO.dto.stream().filter(e -> e.apiState == 1).map(e -> e.getApiId().longValue()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(apiIds)) {
                    List<MetaDataEntityDTO> apiMetaData = getApiMetaDataByIds(apiIds);
                    dataManageClient.syncDataConsumptionMetaData(apiMetaData);
                }
            }
            log.info("数据服务【appSubscribe】执行元数据同步结束");
        } catch (Exception ex) {
            log.info(ex.toString());
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
        List<Long> paramIdList = null;
        List<ParmConfigPO> paramList = apiParmMapper.getListByApiIds(apiIdList);
        if (CollectionUtils.isNotEmpty(paramList)) {
            paramIdList = paramList.stream().map(ParmConfigPO::getId).collect(Collectors.toList());
        }

        // 第四步：查询应用API内置参数
        List<BuiltinParmPO> builtinParamList = null;
        if (paramIdList != null && paramIdList.size() > 0)
            builtinParamList = apiBuiltinParmMapper.getListByWhere(dto.appId, apiIdList, paramIdList);

        // 第五步：查询API接口的返回参数
        List<FieldConfigPO> fieldList = apiFieldMapper.getListByApiIds(apiIdList);
        if (CollectionUtils.isEmpty(fieldList))
            return ResultEnum.DS_API_FIELD_EXISTS;

        // 第六步：API信息转换为文档实体
        final ApiDocDTO docDTO = createDocDTO(appConfigPO, apiList, paramList, builtinParamList, fieldList);

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

        String fileName = "APIServiceDoc" + v + ".pdf";
//        try {
//            fileName = new String(appConfigPO.getAppName().getBytes("UTF-8"), "ISO-8859-1") + ".pdf";
//        } catch (UnsupportedEncodingException e) {
//            log.error("生成API文档时，字符编码异常：" + e);
//        }
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
    public List<AppApiParmVO> getParamAll(AppApiParmQueryDTO dto) {
        List<AppApiParmVO> appApiParamList = new ArrayList<>();
        QueryWrapper<ParmConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ParmConfigPO::getApiId, dto.apiId)
                .eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> selectList = apiParmMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            // 查询已设置内置参数
            QueryWrapper<BuiltinParmPO> builtinParamQuery = new QueryWrapper<>();
            builtinParamQuery.lambda()
                    .eq(BuiltinParmPO::getApiId, dto.apiId)
                    .eq(BuiltinParmPO::getAppId, dto.appId)
                    .eq(BuiltinParmPO::getDelFlag, 1);
            List<BuiltinParmPO> builtinParamList = apiBuiltinParmMapper.selectList(builtinParamQuery);
            if (CollectionUtils.isNotEmpty(builtinParamList)) {
                for (ParmConfigPO parmConfigPO : selectList) {
                    Optional<BuiltinParmPO> builtinParamOptional = builtinParamList.stream().filter(item -> item.getParmId() == parmConfigPO.id).findFirst();
                    if (builtinParamOptional.isPresent()) {
                        // 存在
                        BuiltinParmPO builtinParam = builtinParamOptional.get();
                        parmConfigPO.setParmValue(builtinParam.parmValue);
                        parmConfigPO.setParmDesc(builtinParam.parmDesc);
                        parmConfigPO.setParmIsbuiltin(builtinParam.parmIsbuiltin);
                    }
                }
            }
            appApiParamList = ApiParmMap.INSTANCES.listPoToAppApiParmVo(selectList);
        }
        return appApiParamList;
    }

    @Override
    public ResultEnum setParam(AppApiBuiltinParmEditDTO dto) {
        ApiConfigPO apiModel = apiRegisterMapper.selectById(dto.apiId);
        if (apiModel == null)
            return ResultEnum.DS_API_EXISTS;
        AppConfigPO appModel = baseMapper.selectById(dto.appId);
        if (appModel == null)
            return ResultEnum.DS_APP_EXISTS;

        // 删除此应用API下的所有内置参数，再新增
        int updateCount = apiBuiltinParmMapper.updateBySearch(dto.appId, dto.apiId);

        List<BuiltinParmPO> builtinParamList = ApiBuiltinParmMap.INSTANCES.listDtoToPo(dto.parmList);
        if (CollectionUtils.isNotEmpty(builtinParamList)) {
            return apiBuiltinParmImpl.saveBatch(builtinParamList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }


    /**
     * 生成API文档DTO
     *
     * @param apiList           API信息
     * @param paramsList        API参数信息
     * @param builtinParamsList API内置参数信息
     * @param fieldList         API字段信息
     * @return
     */
    private ApiDocDTO createDocDTO(AppConfigPO appConfig,
                                   List<ApiConfigPO> apiList,
                                   List<ParmConfigPO> paramsList,
                                   List<BuiltinParmPO> builtinParamsList,
                                   List<FieldConfigPO> fieldList) {
        ApiDocDTO apiDocDTO = new ApiDocDTO();
        // API文档基础信息
        String jsonResult = DATASERVICE_APIBASICINFO.replace("{api_prd_address}", api_address)
                .replace("{apiVersion_StartDate}", DateTimeUtils.getNowToShortDate())
                .replace("{apiVersion_EndDate}", DateTimeUtils.getNowToShortDate())
                .replace("{apiVersion_Modified}", appConfig.getAppPrincipal())
                .replace("{release_Date}", DateTimeUtils.getNowToShortDate().replace("-", ""));

        // log.info("createDocDTO jsonInfo："+jsonResult);
        apiDocDTO = JSON.parseObject(jsonResult, ApiDocDTO.class);
        // API文档代码示例 c#
        apiDocDTO.apiCodeExamples_net = DATASERVICE_APICODEEXAMPLES_NET.replace("{api_prd_address}", api_address);
        apiDocDTO.apiCodeExamples_net_encrypt = DATASERVICE_APICODEEXAMPLES_NET_ENCRYPT;


        // API文档代码示例 java
        apiDocDTO.apiCodeExamples_java = DATASERVICE_APICODEEXAMPLES_JAVA.replace("{api_prd_address}", api_address);
        apiDocDTO.apiCodeExamples_java_encrypt = DATASERVICE_APICODEEXAMPLES_JAVA_ENCRYPT;

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

        apiDocDTO.apiBasicInfoDTOS.get(1).apiRequestExamples = "{\n" +
                "&nbsp;&nbsp; \"apiCode\": \"xxx\",\n" +
                "}";
        apiDocDTO.apiBasicInfoDTOS.get(1).apiResponseExamples = String.format("{\n" +
                "&nbsp;&nbsp; \"code\": 200,\n" +
                "&nbsp;&nbsp; \"data\": \"xxx\", --%s\n" +
                "&nbsp;&nbsp; \"msg\": \"xxx\"\n" +
                "}", "2.4.9");
        // 特殊处理获取密钥接口的请求参数
        apiDocDTO.apiBasicInfoDTOS.get(1).apiRequestDTOS_Fixed = apiDocDTO.apiBasicInfoDTOS.get(1).apiRequestDTOS;
        apiDocDTO.apiBasicInfoDTOS.get(1).apiRequestDTOS = new ArrayList<>();

        BigDecimal catalogueIndex = new BigDecimal("2.5");
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

            String flag = "否";
            if (CollectionUtils.isNotEmpty(paramsList)) {
                List<ParmConfigPO> collect = paramsList.stream().filter(v -> v.getApiId() == apiConfigPO.id).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)){
                    flag = "是";
                }
            }
            // 请求参数新增parmList参数说明
            requestDTO = new ApiRequestDTO();
            requestDTO.setParmName("parmList");

            requestDTO.setIsRequired(flag);
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
            requestDTO.setIsRequired("是");
            requestDTO.setParmType("Integer"); //String特指这个类型，string适用于引用对象
            requestDTO.setParmDesc("每页数量：若未开启单次查询限制则该参数必填，建议每页500条。\n若开启单次查询限制则查询条数不会超过配置的最大条数，默认为最大条数。");
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
            final int[] trIndex_data = {1};
            ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("current");
            apiResponseDTO.setParmType("Integer");
            apiResponseDTO.setParmDesc("当前页码");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("size");
            apiResponseDTO.setParmType("Integer");
            apiResponseDTO.setParmDesc("当前页大小");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("total");
            apiResponseDTO.setParmType("Integer");
            apiResponseDTO.setParmDesc("总数");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("page");
            apiResponseDTO.setParmType("Integer");
            apiResponseDTO.setParmDesc("总页数");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("encryptedFields");
            apiResponseDTO.setParmType("String[]");
            apiResponseDTO.setParmDesc("加密字段列表");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.setParmName("dataArray");
            apiResponseDTO.setParmType("Object[]");
            apiResponseDTO.setParmDesc("返回结果对象列表");
            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
            apiResponseDTOS.add(apiResponseDTO);
            trIndex_data[0]++;

            // 请求参数新增密钥参数说明
//            apiResponseDTO = new ApiResponseDTO();
//            apiResponseDTO.setParmName("encryptKey");
//            apiResponseDTO.setParmType("String");
//            apiResponseDTO.setParmDesc("密钥key，用于字段加密和解密，若未加密则为null");
//            apiResponseDTO.trStyle = trIndex_data[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
//            apiResponseDTOS.add(apiResponseDTO);
//            trIndex_data[0]++;
            List<ApiResponseDTO> apiResponseDataArrays = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(fieldList)) {
                final int[] trIndex = {1};
                List<FieldConfigPO> collect = fieldList.stream().filter(item -> item.apiId == apiConfigPO.id).sorted(Comparator.comparing(FieldConfigPO::getFieldSort)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    collect.forEach(e -> {
                        ApiResponseDTO apiResponseDataArray = new ApiResponseDTO();
                        apiResponseDataArray.parmName = e.fieldName;
                        apiResponseDataArray.parmType = e.fieldType;
                        apiResponseDataArray.parmDesc = e.fieldDesc;
                        if (e.encrypt == 1){
                            apiResponseDataArray.parmEncrypt = "是";
                        }else {
                            apiResponseDataArray.parmEncrypt = "否";
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
                    " &nbsp;&nbsp;\"data\":{\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"current\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"size\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"total\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"page\":null,\n" +
                    " &nbsp;&nbsp;&nbsp;&nbsp;\"encryptedFields\":[],\n" +
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


    @Override
    public List<MetaDataEntityDTO> getApiMetaData() {
        //获取所有应用
        List<AppConfigPO> allApiConfigPOList = this.query().list();
        List<MetaDataEntityDTO> metaDataEntityDTOList = new ArrayList<>();
        List<DataSourceConPO> tableAppDatasourcePOList = dataSourceConMapper.selectList(null);
        for (AppConfigPO appConfigPO : allApiConfigPOList) {
            //获取应用下的API
            List<ApiConfigPO> apiTheAppList = appApiMapper.getApiTheAppList((int) appConfigPO.getId());
            //添加应用下的API
            for (ApiConfigPO apiConfigPO : apiTheAppList) {
                MetaDataEntityDTO metaDataEntityDTO = buildApiMetaDataEntity(appConfigPO, apiConfigPO,tableAppDatasourcePOList);
                metaDataEntityDTOList.add(metaDataEntityDTO);
            }
        }
        return metaDataEntityDTOList;
    }

    @Override
    public List<MetaDataEntityDTO> getApiMetaDataById(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        return getApiMetaDataByIds(ids);
    }

    @Override
    public List<MetaDataEntityDTO> getApiMetaDataByIds(List<Long> ids) {

        List<MetaDataEntityDTO> metaDataEntityDTOList = new ArrayList<>();
        //获取API
        List<ApiConfigPO> apiTheAppList = apiRegisterMapper.selectBatchIds(ids);
        List<DataSourceConPO> tableAppDatasourcePOList = dataSourceConMapper.selectList(null);
        //添加应用下的API
        for (ApiConfigPO apiConfigPO : apiTheAppList) {
            AppConfigPO appConfigPO = appApiMapper.getAppByApiList((int) apiConfigPO.getId()).stream().findFirst().orElse(null);
            if (appConfigPO == null) {
                return metaDataEntityDTOList;
            }
            MetaDataEntityDTO metaDataEntityDTO = buildApiMetaDataEntity(appConfigPO, apiConfigPO,tableAppDatasourcePOList);
            metaDataEntityDTOList.add(metaDataEntityDTO);
        }
        return metaDataEntityDTOList;
    }

    public MetaDataEntityDTO buildApiMetaDataEntity(AppConfigPO appConfigPO, ApiConfigPO apiConfigPO,List<DataSourceConPO> dataSourceConPOList) {
        MetaDataEntityDTO metaDataEntityDTO = new MetaDataEntityDTO();
        metaDataEntityDTO.setQualifiedName("api_" + appConfigPO.getId() + "_" + apiConfigPO.getId());
        metaDataEntityDTO.setName(apiConfigPO.getApiName());
        metaDataEntityDTO.setDisplayName(apiConfigPO.getApiName());
        metaDataEntityDTO.setDescription(apiConfigPO.getApiDesc());
        metaDataEntityDTO.setCreateSql(apiConfigPO.getCreateSql());
        metaDataEntityDTO.setApiType(apiConfigPO.getApiType());
        metaDataEntityDTO.setTableName(apiConfigPO.getTableName());
        if(apiConfigPO.getCreateApiType()!=3){
            //代理API没有数据集
            DataSourceConPO dataSourceConPO = dataSourceConPOList.stream().filter(e -> e.getId() == apiConfigPO.getDatasourceId()).findFirst().orElse(null);
            metaDataEntityDTO.setDatasourceDbId(dataSourceConPO.getDatasourceId());
            metaDataEntityDTO.setDatasourceType(dataSourceConPO.getDatasourceType());
        }
        metaDataEntityDTO.setEntityType(5);
        metaDataEntityDTO.setCreateApiType(apiConfigPO.getCreateApiType());
        metaDataEntityDTO.setOwner(apiConfigPO.createUser);
        metaDataEntityDTO.setAppName(appConfigPO.getAppName());
        //获取API下的字段
        List<FieldConfigVO> fieldConfigVOList = apiRegisterManage.getFieldAll((int) apiConfigPO.getId());
        //添加AP下的字段
        List<MetaDataColumnAttributeDTO> metaDataColumnAttributeDTOList = new ArrayList<>();
        for (FieldConfigVO fieldConfigVO : fieldConfigVOList) {
            MetaDataColumnAttributeDTO metaDataColumnAttributeDTO = new MetaDataColumnAttributeDTO();
            metaDataColumnAttributeDTO.setQualifiedName(metaDataEntityDTO.getQualifiedName() + "_" + fieldConfigVO.getId());
            metaDataColumnAttributeDTO.setName(fieldConfigVO.getFieldName());
            metaDataColumnAttributeDTO.setDisplayName(fieldConfigVO.getFieldName());
            metaDataColumnAttributeDTO.setDescription(fieldConfigVO.getFieldDesc());
            metaDataColumnAttributeDTO.setOwner(apiConfigPO.createUser);
            metaDataColumnAttributeDTOList.add(metaDataColumnAttributeDTO);
        }
        metaDataEntityDTO.setAttributeDTOList(metaDataColumnAttributeDTOList);
        return metaDataEntityDTO;
    }


    public ClassificationInfoDTO getApiServiceByAppName(String appName) {
        ClassificationInfoDTO classificationInfoDTO = new ClassificationInfoDTO();
        AppConfigPO apiAppConfigPOS = this.query().eq("app_name", appName).one();
        if (apiAppConfigPOS != null) {
            classificationInfoDTO.setName(apiAppConfigPOS.getAppName());
            classificationInfoDTO.setDescription(apiAppConfigPOS.getAppDesc());
            classificationInfoDTO.setSourceType(ClassificationTypeEnum.API_GATEWAY_SERVICE);
            return classificationInfoDTO;
        } else {
            return null;
        }
    }

    @Override
    public List<AppBusinessInfoDTO> getApiService() {
        //封装三个服务的所有应用
        List<AppBusinessInfoDTO> appInfos = new ArrayList<>();
        List<AppConfigPO> apiAppConfigPOS = this.query().list();
        //封装API服务的所有应用
        apiAppConfigPOS.stream()
                .forEach(a -> {
                    AppBusinessInfoDTO infoDTO = new AppBusinessInfoDTO(a.getId(), a.getAppName(), a.getAppPrincipal(), a.getAppDesc(), 3);
                    appInfos.add(infoDTO);
                });
        return appInfos;
    }
}
