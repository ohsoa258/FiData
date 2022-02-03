package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.EnCryptUtils;
import com.fisk.dataservice.dto.api.doc.ApiDocDTO;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.map.*;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IAppRegisterManageService;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用接口实现类
 *
 * @author dick
 */
@Service
public class AppRegisterManageImpl extends ServiceImpl<AppRegisterMapper, AppConfigPO> implements IAppRegisterManageService {

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private AppApiMapper appApiMapper;

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

    @Override
    public List<FilterFieldDTO> getColumn() {
        return getMetadata.getMetadataList(
                "dmp_dataservice_db",
                "tb_app_config",
                "",
                FilterSqlConstants.DS_APP_REGISTRATION_SQL);
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
        QueryWrapper<AppApiPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppApiPO::getAppId, id).eq(AppApiPO::getApiState, 1)
                .eq(AppApiPO::getDelFlag, 1);
        List<AppApiPO> appApiPOS = appApiMapper.selectList(queryWrapper);
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
            for (AppApiSubDTO dto : saveDTO.dto) {
                // 根据应用id和APIID查询是否存在订阅记录
                QueryWrapper<AppApiPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(AppApiPO::getAppId, dto.appId).eq(AppApiPO::getApiId, dto.apiId)
                        .eq(AppApiPO::getDelFlag, 1);
                AppApiPO data = appApiMapper.selectOne(queryWrapper);
                if (data != null) {
                    // 存在则修改状态，如果是api列表订阅保存，数据如果存在不做任何操作
                    if (saveDTO.saveType == 1)
                        continue;
                    data.setApiState(data.apiState = dto.apiState);
                    appApiMapper.updateById(data);
                } else {
                    // 不存在则新增
                    AppApiPO model = AppApiMap.INSTANCES.dtoToPo(dto);
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
    public ResultEntity<String> createDoc(CreateAppApiDocDTO dto) {
        String fileName = null;
        // 第一步：检验请求参数
        if (dto == null || CollectionUtils.isEmpty(dto.appApiDto))
            return ResultEntityBuild.buildData(ResultEnum.DS_APPAPIDOC_EXISTS, fileName);
        List<AppApiSubDTO> collect = dto.appApiDto.stream().filter(item -> item.apiState == ApiStateTypeEnum.Enable.getValue()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect))
            return ResultEntityBuild.buildData(ResultEnum.DS_APPAPIDOC_DISABLE, fileName);
        // PS：APP和API是否有效在订阅列表页已验证有效性,此处不做重复验证。

        List<Integer> apiIdList = dto.appApiDto.stream().map(AppApiSubDTO::getApiId).collect(Collectors.toList());

        // 第二步：查询需要生成的API接口
        List<ApiConfigPO> apiList = apiRegisterMapper.getListByIds(apiIdList);
        if (CollectionUtils.isEmpty(apiList))
            return ResultEntityBuild.buildData(ResultEnum.DS_API_EXISTS, fileName);

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
            return ResultEntityBuild.buildData(ResultEnum.DS_API_FIELD_EXISTS, fileName);

        // 第六步：API信息转换为文档实体
        final ApiDocDTO docDTO = createDocDTO(apiList, parmList, builtinParmList, fieldList);

        // 第七步：新增文档实体 TDDD:获取token

        // 第八步：替换ftl模板中变量

        // 第九步：生成pdf，返回文件名称

        return null;
    }

    @Override
    public ResponseEntity downloadDoc(String fileName) {
        Path path = Paths.get("C:/Users/Player/Downloads/pdffile", fileName);
        File file = new File(path.toString());
        if (!file.exists())
            return null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/pdf");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        try {
            return new ResponseEntity(IOUtils.toByteArray(in), headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

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
     * @param apiList         API信息
     * @param parmList        API参数信息
     * @param builtinParmList API内置参数信息
     * @param fieldList       API字段信息
     * @return
     */
    private ApiDocDTO createDocDTO(List<ApiConfigPO> apiList,
                                   List<ParmConfigPO> parmList,
                                   List<BuiltinParmPO> builtinParmList,
                                   List<FieldConfigPO> fieldList) {
        ApiDocDTO apiDocDTO = new ApiDocDTO();

        String jsonResult = "{\n" +
                "    \"title\":\"MDM主数据API接口文档\",\n" +
                "    \"docVersion\":\"文档版本 V1.0\",\n" +
                "    \"isuCompany\":\"菲斯科（上海）软件有限公司编制\",\n" +
                "    \"isuDate\":\"发布日期：20220101\",\n" +
                "    \"footerName\":\"菲斯科白泽接口文档\",\n" +
                "    \"docPurpose\":\"本文由本文由菲斯科（上海）软件有限公司编写，用于下游系统对接白泽接口。\",\n" +
                "    \"readers\":\"预期读者包括需要从白泽获取数据的下游系统。\",\n" +
                "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码为UTF-8。\",\n" +
                "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
                "    \"uatAddress\":\"https://uatHost/{apiaddress}。\",\n" +
                "    \"prdAddress\":\"https://prdHost/{apiaddress}。\",\n" +
                "    \"apiContactsDTOS\":[\n" +
                "        {\n" +
                "            \"category\":\"接口负责人\",\n" +
                "            \"company\":\"菲斯科\",\n" +
                "            \"fullName\":\"李家温\",\n" +
                "            \"mailbox\":\"dick@fisksoft.com\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"category\":\"接口负责人\",\n" +
                "            \"company\":\"菲斯科\",\n" +
                "            \"fullName\":\"李家温\",\n" +
                "            \"mailbox\":\"dick@fisksoft.com\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"apiVersionDTOS\":[\n" +
                "        {\n" +
                "            \"version\":\"0.1\",\n" +
                "            \"startDate\":\"2022/01/01\",\n" +
                "            \"endDate\":\"2022/01/01\",\n" +
                "            \"modifier\":\"lijiawen\",\n" +
                "            \"explain\":\"文档创建、编写\",\n" +
                "            \"state\":\"初稿\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"apiResponseCodeDTOS\":[\n" +
                "        {\n" +
                "            \"code\":\"200\",\n" +
                "            \"type\":\"int\",\n" +
                "            \"desc\":\"调用结果描述\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"code\":\"401\",\n" +
                "            \"type\":\"int\",\n" +
                "            \"desc\":\"无权限访问此API\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"code\":\"404\",\n" +
                "            \"type\":\"int\",\n" +
                "            \"desc\":\"API不存在或被取消订阅\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"code\":\"500\",\n" +
                "            \"type\":\"int\",\n" +
                "            \"desc\":\"API服务器异常\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        apiDocDTO = JSON.parseObject(JSON.parse(jsonResult).toString(), ApiDocDTO.class);

        List<String> docCatalogue = new ArrayList<>();
        BigDecimal catalogueIndex = new BigDecimal(Double.toString(2.2));
        for (int i = 0; i < apiList.size(); i++) {
            BigDecimal incrementIndex = new BigDecimal(Double.toString(i + 1));
            docCatalogue.add(catalogueIndex.add(incrementIndex).doubleValue() + apiList.get(i).apiName);
        }
        apiDocDTO.docCatalogue = docCatalogue;

        return apiDocDTO;
    }
}
