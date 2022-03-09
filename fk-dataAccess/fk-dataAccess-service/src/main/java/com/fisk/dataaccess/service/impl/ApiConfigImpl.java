package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.constants.ApiConstants;
import com.fisk.common.constants.RedisTokenKey;
import com.fisk.common.exception.FkException;
import com.fisk.common.pdf.component.PDFHeaderFooter;
import com.fisk.common.pdf.component.PDFKit;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.ApiUserDTO;
import com.fisk.dataaccess.dto.api.GenerateDocDTO;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.dto.api.doc.doc.*;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.map.ApiConfigMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.mapper.ApiConfigMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IApiConfig;
import com.fisk.dataaccess.utils.json.JsonUtils;
import com.fisk.dataaccess.utils.sql.PgsqlUtils;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.ProcessorConfig;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Slf4j
@Service
public class ApiConfigImpl extends ServiceImpl<ApiConfigMapper, ApiConfigPO> implements IApiConfig {

    @Resource
    private TableAccessImpl tableAccessImpl;
    @Resource
    private TableAccessMapper tableAccessMapper;
    @Resource
    private TableFieldsImpl tableFieldImpl;
    @Resource
    private AppRegistrationImpl appRegistrationImpl;
    @Resource
    private AuthClient authClient;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private TableSyncmodeImpl tableSyncmodeImpl;
    @Resource
    private TableBusinessImpl tableBusinessImpl;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Value("${dataservice.pdf.path}")
    private String templatePath;

    @Override
    public ApiConfigDTO getData(long id) {

        ApiConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        ApiConfigDTO apiConfigDTO = ApiConfigMap.INSTANCES.poToDto(po);
        // 根据api_id查询物理表集合
        List<TableAccessPO> poList = getListTableAccessByApiId(id);
        // 根据table_id查询出表详情,并赋值给apiConfigDTO
        apiConfigDTO.list = poList.stream().map(tableAccessPO -> tableAccessImpl.getData(tableAccessPO.id)).collect(Collectors.toList());
        return apiConfigDTO;
    }

    @Override
    public ResultEnum addData(ApiConfigDTO dto) {
        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.apiName).collect(Collectors.toList());
        if (list.contains(dto.apiName)) {
            return ResultEnum.NAME_EXISTS;
        }

        // dto -> po
        ApiConfigPO model = ApiConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        //保存
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addApiDetail(ApiConfigDTO dto) {

        ApiConfigPO model = ApiConfigMap.INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEnum.API_NOT_EXIST;
        }
        // 修改api
        editData(dto);

        if (!CollectionUtils.isEmpty(dto.list)) {
            dto.list.forEach(e -> tableFieldImpl.addData(e));
        }

        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(ApiConfigDTO dto) {
        // 判断名称是否重复
        QueryWrapper<ApiConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigPO::getApiName, dto.apiName);
        ApiConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // 参数校验
        ApiConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(ApiConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum editApiDetail(ApiConfigDTO dto) {

        ApiConfigPO model = ApiConfigMap.INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEnum.API_NOT_EXIST;
        }
        // 修改api
        editData(dto);

        if (!CollectionUtils.isEmpty(dto.list)) {
            dto.list.forEach(e -> tableFieldImpl.updateData(e));
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        ApiConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 根据api_id查询物理表集合
        List<TableAccessPO> poList = getListTableAccessByApiId(id);
        // 删除api下所有物理表
        poList.forEach(e -> tableAccessImpl.deleteData(e.id));

        // 删除api
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ApiConfigDTO> getApiListData(long appId) {

        List<ApiConfigPO> list = this.query().eq("app_id", appId).list();

        return ApiConfigMap.INSTANCES.listPoToDto(list);
    }

    @Override
    public ResultEnum generateDoc(GenerateDocDTO dto, HttpServletResponse response) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        // api信息
        ApiConfigDTO data = getData(dto.apiId);
        if (data == null) {
            return ResultEnum.API_ISEMPTY;
        }
        // api信息转换为文档实体
        ApiDocDTO docDTO = createDocDTO(data, dto.pushDataJson);
        // 生成pdf,返回文件名称
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        PDFKit kit = new PDFKit();
        kit.setHeaderFooterBuilder(headerFooter);
        // 系统时间戳
        long timeMillis = System.currentTimeMillis();
        String fileName = "APIServiceDoc" + timeMillis + ".pdf";
        // 生成PDF文件
        OutputStream outputStream = kit.exportToResponse("apiserviceTemplate.ftl",
                templatePath, fileName, "FiData接口文档", docDTO, response);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new FkException(ResultEnum.GENERATE_PDF_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum pushData(ReceiveDataDTO dto) {
        try {
            if (dto.apiId == null) {
                return ResultEnum.PUSH_TABLEID_NULL;
            }

            // 每次推送数据前,将stg数据删除
            pushDataStgToOds(dto.apiId,0);

            // 根据api_id查询所有物理表
            List<TableAccessPO> accessPOList = tableAccessImpl.query().eq("api_id", dto.apiId).list();
            if (CollectionUtils.isEmpty(accessPOList)) {
                return ResultEnum.TABLE_NOT_EXIST;
            }
            // 获取所有表数据
            List<ApiTableDTO> apiTableDtoList = getApiTableDtoList(accessPOList);
            apiTableDtoList.forEach(System.out::println);

            AppRegistrationPO modelApp = appRegistrationImpl.query().eq("id", accessPOList.get(0).appId).one();
            if (modelApp == null) {
                return ResultEnum.APP_NOT_EXIST;
            }
            // 防止\未被解析
            String jsonStr = StringEscapeUtils.unescapeJava(dto.pushData);
            // 将数据同步到pgsql
            pushPgSQL(jsonStr, apiTableDtoList, "stg_" + modelApp.appAbbreviation + "_");

            // TODO stg同步到ods(联调task)
            pushDataStgToOds(dto.apiId, 1);
        } catch (Exception e) {
            return ResultEnum.PUSH_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEntity<String> getToken(ApiUserDTO dto) {

        // 根据账号名称查询对应的app_id下
        AppDataSourcePO dataSourcePO = appDataSourceImpl.query().eq("realtime_account", dto.getUseraccount()).one();
        if (!dataSourcePO.realtimeAccount.equals(dto.getUseraccount()) || !dataSourcePO.realtimePwd.equals(dto.getPassword())) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR, ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR.getMsg());
        }
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(dto.useraccount);
        userAuthDTO.setPassword(dto.password);
        userAuthDTO.setTemporaryId(RedisTokenKey.DATA_ACCESS_TOKEN + dataSourcePO.id);

        ResultEntity<String> result = authClient.getToken(userAuthDTO);
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            return result;
        } else {
            log.error("远程调用失败,方法名: 【auth-service:getToken】");
            return ResultEntityBuild.build(ResultEnum.GET_TOKEN_ERROR);
        }
    }

    @Override
    public void updateApiPublishStatus(ModelPublishStatusDTO dto) {
        // 修改表状态
        tableAccessImpl.updateTablePublishStatus(dto);

        ApiConfigPO model = baseMapper.selectById(dto.apiId);
        if (model != null) {
            // 获取api下所有表
            List<TableAccessPO> list = tableAccessImpl.query().eq("api_id", dto.apiId).list();
            // 获取所有表状态
            List<Integer> publishList = list.stream().map(a -> a.publish).collect(Collectors.toList());
            // 默认成功
            int apiPublish = 1;
            // 有一张表发布失败,api即失败
            for (Integer publish : publishList) {
                if (publish == 2) {
                    apiPublish = publish;
                }
            }
            // 修改api状态
            model.publish = apiPublish;
            baseMapper.updateById(model);
        }
    }

    /**
     * 获取所有表数据
     *
     * @return java.util.List<com.fisk.dataaccess.dto.json.ApiTableDTO>
     * @description 获取所有表数据
     * @author Lock
     * @date 2022/2/22 17:02
     * @version v1.0
     * @params accessPOList 物理表集合
     */
    private List<ApiTableDTO> getApiTableDtoList(List<TableAccessPO> accessPOList) {
        // 根据table_id获取物理表详情
        List<TableAccessNonDTO> poList = accessPOList.stream().map(e -> tableAccessImpl.getData(e.id)).collect(Collectors.toList());

        List<ApiTableDTO> apiTableDTOList = new ArrayList<>();
        poList.forEach(e -> {
            ApiTableDTO apiTableDTO = new ApiTableDTO();
            apiTableDTO.tableName = e.tableName;
            apiTableDTO.pid = e.pid;
            apiTableDTO.list = e.list;
            // 查询所有子级表名
            QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TableAccessPO::getPid, e.id)
                    .select(TableAccessPO::getTableName);
            List<TableAccessPO> list = tableAccessMapper.selectList(queryWrapper);
            apiTableDTO.childTableName = list.stream().filter(Objects::nonNull).map(f -> f.tableName).collect(Collectors.toList());
            apiTableDTOList.add(apiTableDTO);
        });

        return apiTableDTOList;
    }

    /**
     * 将数据同步到pgsql
     *
     * @return void
     * @description 将数据同步到pgsql
     * @author Lock
     * @date 2022/2/16 19:17
     * @version v1.0
     * @params jsonStr
     * @params apiTableDtoList
     * @params tablePrefixName pg中的物理表名
     */
    private void pushPgSQL(String jsonStr, List<ApiTableDTO> apiTableDtoList, String tablePrefixName) {
        try {
            JSONObject json = JSON.parseObject(jsonStr);
            List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
            JsonUtils jsonUtils = new JsonUtils();
            List<JsonTableData> targetTable = jsonUtils.getTargetTable(tableNameList);
            targetTable.forEach(System.out::println);
            // 获取Json的schema信息
            List<JsonSchema> schemas = jsonUtils.getJsonSchema(apiTableDtoList);
            schemas.forEach(System.out::println);
            // json根节点处理
            jsonUtils.rootNodeHandler(schemas, json, targetTable);
            targetTable.forEach(System.out::println);

            System.out.println("开始执行sql");
            PgsqlUtils pgsqlUtils = new PgsqlUtils();
            // ods_abbreviationName_tableName
            pgsqlUtils.executeBatchPgsql(tablePrefixName, targetTable);

        } catch (Exception e) {
            throw new FkException(ResultEnum.PUSH_DATA_ERROR);
        }
    }

    /**
     * 将数据从stg同步到ods
     *
     * @return void
     * @description
     * @author Lock
     * @date 2022/2/25 16:41
     * @version v1.0
     * @params apiId apiId
     * @params flag 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     */
    private void pushDataStgToOds(Long apiId, int flag) {

        // 1.根据apiId获取api所有信息
        ApiConfigPO apiConfigPO = baseMapper.selectById(apiId);
        if (apiConfigPO == null) {
            throw new FkException(ResultEnum.API_NOT_EXIST);
        }
        // 2.根据appId获取app所有信息
        AppRegistrationPO app = appRegistrationImpl.query().eq("id", apiConfigPO.appId).one();
        if (app == null) {
            throw new FkException(ResultEnum.APP_NOT_EXIST);
        }

        // 3.根据apiId查询所有物理表详情
        ApiConfigDTO dto = getData(apiId);
        List<TableAccessNonDTO> tablelist = dto.list;
        if (CollectionUtils.isEmpty(tablelist)) {
            // 当前api下没有物理表
            throw new FkException(ResultEnum.TABLE_NOT_EXIST);
        }

        // 4.组装参数,调用tasdk,获取推送数据所需的sql
        for (TableAccessNonDTO e : tablelist) {
            TableSyncmodePO syncmodePo = tableSyncmodeImpl.query().eq("id", e.id).one();
            DataAccessConfigDTO configDTO = new DataAccessConfigDTO();
            // 表名
            ProcessorConfig processorConfig = new ProcessorConfig();
            processorConfig.targetTableName = app.appAbbreviation + "_" + e.tableName;
            // 同步方式
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.syncMode = syncmodePo.syncMode;
            // 增量对象
            if (syncmodePo.syncMode == 4) {
                TableBusinessPO businessPo = tableBusinessImpl.query().eq("access_id", e.id).one();
                configDTO.businessDTO = TableBusinessMap.INSTANCES.poToDto(businessPo);
            }

            // 业务主键集合(逗号隔开)
            List<TableFieldsDTO> fieldList = e.list;
            if (!CollectionUtils.isEmpty(fieldList)) {
                configDTO.businessKeyAppend = fieldList.stream().filter(f -> f.isPrimarykey == 1).map(f -> f.fieldName + ",").collect(Collectors.joining());
            }

            configDTO.processorConfig = processorConfig;
            configDTO.targetDsConfig = dataSourceConfig;

            // 获取同步数据的sql并执行
            getSynchroDataSqlAndExcute(configDTO, flag);
        }
    }

    /**
     * 获取同步数据的sql并执行
     *
     * @return void
     * @description 获取同步数据的sql并执行
     * @author Lock
     * @date 2022/2/25 16:06
     * @version v1.0
     * @params configDTO task需要的参数
     * @params flag 0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     */
    private void getSynchroDataSqlAndExcute(DataAccessConfigDTO configDTO, int flag) {
        try {
            // 调用task,获取同步数据的sql
            ResultEntity<List<String>> result = publishTaskClient.getSqlForPgOds(configDTO);
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                List<String> sqlList = JSON.parseObject(JSON.toJSONString(result.data), List.class);
                if (!CollectionUtils.isEmpty(sqlList)) {
                    PgsqlUtils pgsqlUtils = new PgsqlUtils();
                    pgsqlUtils.stgToOds(sqlList, flag);
                }
            }
        } catch (SQLException e) {
            throw new FkException(ResultEnum.STG_TO_ODS_ERROR);
        }
    }

    /**
     * 根据api_id查询物理表集合
     *
     * @return java.util.List<com.fisk.dataaccess.entity.TableAccessPO>
     * @description 根据api_id查询物理表集合
     * @author Lock
     * @date 2022/2/15 10:30
     * @version v1.0
     * @params id api_id
     */
    private List<TableAccessPO> getListTableAccessByApiId(long id) {
        QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableAccessPO::getApiId, id)
                // 只查询table_id
                .select(TableAccessPO::getId);
        return tableAccessMapper.selectList(queryWrapper);
    }

    /**
     * 创建模板填充数据所需的对象
     *
     * @return com.fisk.dataaccess.dto.api.doc.doc.ApiDocDTO
     * @description 创建模板填充数据所需的对象
     * @author Lock
     * @date 2022/2/25 11:03
     * @version v1.0
     * @params dto api对象
     * @params pushDataJson 前端传入的表json格式数据
     */
    private ApiDocDTO createDocDTO(ApiConfigDTO dto, String pushDataJson) {

        ApiDocDTO apiDocDTO = JSON.parseObject(ApiConstants.DATAACCESS_APIBASICINFO, ApiDocDTO.class);
        apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestExamples = "{\n" +
                "&nbsp;&nbsp; \"useraccount\": \"xxx\",\n" +
                "&nbsp;&nbsp; \"password\": \"xxx\"\n" +
                "}";
        apiDocDTO.apiBasicInfoDTOS.get(0).apiResponseExamples = String.format("{\n" +
                "&nbsp;&nbsp; \"code\": 0,\n" +
                "&nbsp;&nbsp; \"msg\": \"xxx\", --%s\n" +
                "&nbsp;&nbsp; \"data\": \"temporary token value\"\n" +
                "}", "2.4.9");
        BigDecimal catalogueIndex = new BigDecimal("2.4");

        // API基本信息对象
        List<ApiBasicInfoDTO> apiBasicInfoDTOS = new ArrayList<>();
        List<TableAccessNonDTO> tableAccessDtoList = dto.list;
        if (CollectionUtils.isEmpty(tableAccessDtoList)) {
            return apiDocDTO;
        }

        // 设置目录
        ApiCatalogueDTO apiCatalogueDTO = new ApiCatalogueDTO();
        BigDecimal incrementIndex = new BigDecimal("0.1");
        BigDecimal addIndex = catalogueIndex.add(incrementIndex);
        // 目录等级
        apiCatalogueDTO.grade = 3;
        // 目录序号
        apiCatalogueDTO.catalogueIndex = addIndex + ".";
        // 目录名称
        apiCatalogueDTO.catalogueName = dto.apiName;
        apiDocDTO.apiCatalogueDTOS.add(apiDocDTO.apiCatalogueDTOS.size() - 1, apiCatalogueDTO);
        catalogueIndex = addIndex;

        // 设置API基础信息(2.5.-2.5.5)
        ApiBasicInfoDTO apiBasicInfoDTO = new ApiBasicInfoDTO();
        apiBasicInfoDTO.apiName = dto.apiName;
        apiBasicInfoDTO.apiAddress = "/dataAccess/apiConfig/pushdata";
        apiBasicInfoDTO.apiDesc = dto.apiDes;
        apiBasicInfoDTO.apiRequestType = "POST";
        apiBasicInfoDTO.apiContentType = "application/json";
        apiBasicInfoDTO.apiHeader = "Authorization: Bearer {token}";

        apiBasicInfoDTO.apiUnique = String.valueOf(dto.id);

        // 设置API请求参数(2.5.7 参数body)
        List<ApiRequestDTO> apiRequestDTOS = new ArrayList<>();
        ApiRequestDTO apiId = new ApiRequestDTO();
        apiId.parmName = "apiId";
        apiId.isRequired = "是";
        apiId.parmType = "String";
        apiId.parmDesc = "api唯一标识: " + dto.id + " (真实数据)";
        apiId.trStyle = "background-color: #fff";
        ApiRequestDTO pushData = new ApiRequestDTO();
        pushData.parmName = "pushData";
        pushData.isRequired = "是";
        pushData.parmType = "String";
        pushData.parmDesc = "json序列化数据(参数格式及字段类型参考2.6.0&2.6.1)";
        pushData.trStyle = "background-color: #f8f8f8";
        apiRequestDTOS.add(apiId);
        apiRequestDTOS.add(pushData);
        apiBasicInfoDTO.apiRequestDTOS = apiRequestDTOS;
        apiBasicInfoDTO.apiRequestExamples = String.format("{\n" +
                " &nbsp;&nbsp;\"apiId\": \"xxx\",\n" +
                " &nbsp;&nbsp;\"pushData\": \"xxx\"\n" +
                "}", addIndex + ".7");

        // 参数(body)表格
        List<ApiResponseDTO> apiResponseDTOS = new ArrayList<>();
        ApiResponseDTO code = new ApiResponseDTO();
        code.parmName = "code";
        code.parmType = "int";
        code.parmDesc = "调用结果状态";
        ApiResponseDTO msg = new ApiResponseDTO();
        msg.parmName = "msg";
        msg.parmType = "String";
        msg.parmDesc = "调用结果描述";
        ApiResponseDTO data = new ApiResponseDTO();
        data.parmName = "data";
        data.parmType = "String";
        data.parmDesc = "返回的数据";
        apiResponseDTOS.add(code);
        apiResponseDTOS.add(msg);
        apiResponseDTOS.add(data);
        apiBasicInfoDTO.apiResponseDTOS = apiResponseDTOS;

        //设置API返回参数,即返回示例
        apiBasicInfoDTO.apiResponseExamples = String.format("{\n" +
                " &nbsp;&nbsp;\"code\": 0,\n" +
                " &nbsp;&nbsp;\"msg\": \"xxx\",\n" +
                " &nbsp;&nbsp;\"data\": null\n" +
                "}", addIndex + ".9");

        // pushData json格式
        if (StringUtils.isNotBlank(pushDataJson)) {
            apiDocDTO.pushDataJson = pushDataJson;
        } else {// 防止模板报错
            apiDocDTO.pushDataJson = "&nbsp;&nbsp;当前api没有表";
        }


        // pushData json字段描述
        List<ApiResponseDTO> pushDataDtos = new ArrayList<>();
        List<TableAccessNonDTO> list = dto.list;
        if (!CollectionUtils.isEmpty(list)) {
            final int[] trIndex = {1};
            list.forEach(e -> {
                e.list.forEach(f -> {
                    ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
                    apiResponseDTO.tableName = e.tableName;
                    apiResponseDTO.parmName = f.fieldName;
                    apiResponseDTO.parmType = f.fieldType;
                    apiResponseDTO.parmDesc = f.fieldDes;
                    apiResponseDTO.trStyle = trIndex[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
                    pushDataDtos.add(apiResponseDTO);
                    trIndex[0]++;
                });
            });
        } else {// 防止模板报错
            ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
            apiResponseDTO.tableName = "暂无数据";
            apiResponseDTO.parmName = "暂无数据";
            apiResponseDTO.parmType = "暂无数据";
            apiResponseDTO.parmDesc = "暂无数据";
            apiResponseDTO.trStyle = "background-color: #f8f8f8";
            pushDataDtos.add(apiResponseDTO);
        }
        apiDocDTO.pushDataDtos = pushDataDtos;


        //设置API目录
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

        apiDocDTO.apiBasicInfoDTOS.addAll(apiBasicInfoDTOS);
        return apiDocDTO;
    }

    public static void main(String[] args) {
        JsonUtils jsonUtils = new JsonUtils();
        // 测试时间
//        Instant inst1 = Instant.now();
        String s = StringEscapeUtils.unescapeJava(jsonUtils.JSONSTR);
        JSONObject json = JSON.parseObject(s);
        System.out.println("json = " + json);

        // 封装数据库存储的数据结构
        List<ApiTableDTO> apiTableDtoList = jsonUtils.getApiTableDtoList01();
//        apiTableDtoList.forEach(System.out::println);
//        int a = 1 / 0;

        List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
        // 获取目标表
        List<JsonTableData> targetTable = jsonUtils.getTargetTable(tableNameList);
        targetTable.forEach(System.out::println);
        // 获取Json的schema信息
        List<JsonSchema> schemas = jsonUtils.getJsonSchema(apiTableDtoList);
        schemas.forEach(System.out::println);
//        System.out.println("====================");
        try {
            // json根节点处理
            jsonUtils.rootNodeHandler(schemas, json, targetTable);
            targetTable.forEach(System.out::println);

            System.out.println("开始执行sql");
            PgsqlUtils pgsqlUtils = new PgsqlUtils();
            // ods_abbreviationName_tableName
            pgsqlUtils.executeBatchPgsql("stg_push_", targetTable);

//            Instant inst2 = Instant.now();
//            System.out.println("Difference in 纳秒 : " + Duration.between(inst1, inst2).getNano());
//            System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
        } catch (Exception e) {
            System.out.println("执行失败");
        }
    }
}