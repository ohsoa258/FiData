package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.office.pdf.component.PDFHeaderFooter;
import com.fisk.common.core.utils.office.pdf.component.PDFKit;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.dataaccess.dto.api.*;
import com.fisk.dataaccess.dto.api.doc.*;
import com.fisk.dataaccess.dto.api.httprequest.ApiHttpRequestDTO;
import com.fisk.dataaccess.dto.api.httprequest.JwtRequestDTO;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.ApiSqlResultDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.dataaccess.dto.table.TableSyncmodeDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.ApiConditionEnum;
import com.fisk.dataaccess.enums.ApiParameterTypeEnum;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.map.*;
import com.fisk.dataaccess.mapper.ApiConfigMapper;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IApiCondition;
import com.fisk.dataaccess.service.IApiConfig;
import com.fisk.dataaccess.utils.httprequest.ApiHttpRequestFactoryHelper;
import com.fisk.dataaccess.utils.httprequest.IBuildHttpRequest;
import com.fisk.dataaccess.utils.json.JsonUtils;
import com.fisk.dataaccess.utils.sql.PgsqlUtils;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datagovernance.client.DataQualityClient;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckWebDTO;
import com.fisk.datagovernance.enums.dataquality.CheckRuleEnum;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import com.fisk.task.dto.daconfig.ProcessorConfig;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.nifi.NifiStageMessageDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.dto.pipeline.NifiStageDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.fisk.common.core.constants.ApiConstants.*;
import static com.fisk.dataaccess.enums.HttpRequestEnum.GET;
import static com.fisk.dataaccess.enums.HttpRequestEnum.POST;

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
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    private TableSyncmodeImpl tableSyncmodeImpl;
    @Resource
    private TableBusinessImpl tableBusinessImpl;
    @Resource
    private ApiParameterServiceImpl apiParameterServiceImpl;
    @Resource
    private ApiResultConfigImpl apiResultConfigImpl;
    @Resource
    private UserHelper userHelper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private DataManageClient dataManageClient;
    @Value("${dataservice.pdf.path}")
    private String templatePath;
    @Value("${dataservice.pdf.uat_address}")
    private String pdf_uat_address;
    @Value("${dataservice.pdf.prd_address}")
    private String pdf_prd_address;
    @Resource
    private DataQualityClient dataQualityClient;
    @Value("${data-quality-check.ip}")
    private String dataQualityCheckIp;
    @Value("${data-quality-check.db-name}")
    private String dataQualityCheckName;
    @Resource
    private IApiCondition iApiCondition;
    // 实时api同步到stg的条数
    private Integer COUNT_SQL = 0;

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
        apiConfigDTO.list = poList.stream().map(e -> tableAccessImpl.getData(e.id)).collect(Collectors.toList());
        return apiConfigDTO;
    }

    @Override
    public ResultEnum addData(ApiConfigDTO dto) {
        // 当前字段名不可重复,得保证同一应用下
        boolean flag = checkApiName(dto);
        if (flag) {
            return ResultEnum.APINAME_ISEXIST;
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

    private boolean checkApiName(ApiConfigDTO dto) {

        boolean flag = false;

        QueryWrapper<ApiConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(ApiConfigPO::getAppId, ApiConfigPO::getApiName);
        List<ApiConfigPO> apiConfigPoList = baseMapper.selectList(queryWrapper);
        ApiConfigPO apiConfigPo = new ApiConfigPO();
        apiConfigPo.appId = dto.appId;
        apiConfigPo.apiName = dto.apiName;
        if (apiConfigPoList.contains(apiConfigPo)) {
            flag = true;
        }
        return flag;
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
        queryWrapper.lambda().eq(ApiConfigPO::getApiName, dto.apiName).eq(ApiConfigPO::getAppId, dto.appId);
        ApiConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id && !po.appId.equals(dto.appId)) {
            return ResultEnum.WORKFLOWNAME_EXISTS;
        }

        // 参数校验
        ApiConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 重置api发布装填
        dto.setPublish(0);

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

        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("app_id", dto.appId).one();
        if (dataSourcePo == null) {
            return ResultEnum.DATASOURCE_ISNULL;
        }

        // 发布之后,按照配置调用一次非实时api
        if (dto.executeConfigFlag && dataSourcePo.driveType.equalsIgnoreCase(DataSourceTypeEnum.API.getName())) {
            ApiImportDataDTO apiImportDataDTO = new ApiImportDataDTO();
            apiImportDataDTO.appId = dto.appId;
            apiImportDataDTO.apiId = dto.id;
            // 调用api推送数据方法
            importData(apiImportDataDTO);

            // 发布之后,按照配置推送一次实时api
        } else if (dto.executeConfigFlag && dataSourcePo.driveType.equalsIgnoreCase(DataSourceTypeEnum.RestfulAPI.getName())) {
            ReceiveDataDTO receiveDataDto = new ReceiveDataDTO();
            receiveDataDto.apiCode = dto.id;
            // 系统内部调用
            receiveDataDto.flag = true;
            // 实时推送示例数据
            receiveDataDto.executeConfigFlag = true;
            String pushData = dto.pushData;
            if (StringUtils.isNotBlank(pushData)) {
                String pushDataStr = pushData.replace("&nbsp;", "").replace("<br/>", "").replace("\\\\n\\n", "");
                log.info("pushDataStr = " + pushDataStr);
                receiveDataDto.pushData = pushDataStr;
            }
            pushData(receiveDataDto);
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
        for (TableAccessPO e : poList) {

            ResultEntity<NifiVO> result = tableAccessImpl.deleteData(e.id);

            log.info("方法返回值,{}", result.data);
            // TODO 删除pg库中的表和nifi流程
            NifiVO nifiVO = result.data;

            PgsqlDelTableDTO pgsqlDelTableDTO = new PgsqlDelTableDTO();
            pgsqlDelTableDTO.userId = nifiVO.userId;
            pgsqlDelTableDTO.appAtlasId = nifiVO.appAtlasId;
            pgsqlDelTableDTO.delApp = false;
            pgsqlDelTableDTO.businessTypeEnum = BusinessTypeEnum.DATAINPUT;
            if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(nifiVO.tableList)) {

                pgsqlDelTableDTO.tableList = nifiVO.tableList.stream().map(f -> {
                    TableListDTO dto = new TableListDTO();
                    dto.tableAtlasId = f.tableAtlasId;
                    dto.tableName = f.tableName;
                    dto.userId = nifiVO.userId;
                    return dto;
                }).collect(Collectors.toList());
            }
            log.info("删表对象信息: " + JSON.toJSONString(pgsqlDelTableDTO));
            // 删除pg库对应的表
            ResultEntity<Object> task = publishTaskClient.publishBuildDeletePgsqlTableTask(pgsqlDelTableDTO);

            DataModelVO dataModelVO = new DataModelVO();
            dataModelVO.delBusiness = false;
            DataModelTableVO dataModelTableVO = new DataModelTableVO();
            dataModelTableVO.ids = nifiVO.tableIdList;
            dataModelTableVO.type = OlapTableEnum.PHYSICS;
            dataModelVO.physicsIdList = dataModelTableVO;
            dataModelVO.businessId = nifiVO.appId;
            dataModelVO.dataClassifyEnum = DataClassifyEnum.DATAACCESS;
            dataModelVO.userId = nifiVO.userId;
            // 删除nifi流程
            publishTaskClient.deleteNifiFlow(dataModelVO);

            // 删除元数据
            // 删除元数据
            MetaDataDeleteAttributeDTO metaDataDeleteAttributeDto = new MetaDataDeleteAttributeDTO();
            metaDataDeleteAttributeDto.setQualifiedNames(nifiVO.qualifiedNames);
            dataManageClient.deleteMetaData(metaDataDeleteAttributeDto);
        }

        // 删除factory-dispatch对应的api配置
        String driveType = appDataSourceMapper.getDriveTypeByAppId(model.appId);
        // 只有非实时api才会在调度中使用
        if (DataSourceTypeEnum.API.getName().equalsIgnoreCase(driveType)) {
            List<DeleteTableDetailDTO> list = new ArrayList<>();
            DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
            deleteTableDetailDto.appId = String.valueOf(model.appId);
            deleteTableDetailDto.tableId = String.valueOf(id);
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DATALAKE_API_TASK;
            list.add(deleteTableDetailDto);
            dataFactoryClient.editByDeleteTable(list);
        }

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
    public ResultEnum generateAppPdfDoc(List<GenerateDocDTO> list, HttpServletResponse response) {

        List<ApiConfigDTO> dtoList = new ArrayList<>();
        // 去重
        list.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList())
                .forEach(generateDocDTO -> {
                    ApiConfigDTO data = getData(generateDocDTO.apiId);
                    if (data != null & generateDocDTO.tableIsEmpty) {
                        data.pushDataJson = generateDocDTO.pushDataJson;
                        dtoList.add(data);
                    }
                });

        // api信息转换为文档实体
        ApiDocDTO docDTO = createApiDocDTO(dtoList);
        // 生成pdf,返回文件名称
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        PDFKit kit = new PDFKit();
        kit.setHeaderFooterBuilder(headerFooter);
        // 系统时间戳
        long timeMillis = System.currentTimeMillis();
        String fileName = "APIServiceDoc" + timeMillis + ".pdf";

        // 生成PDF文件
        OutputStream outputStream = kit.exportToResponse("apiserviceTemplate.ftl",
                templatePath, fileName, "接口文档", docDTO, response);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new FkException(ResultEnum.GENERATE_PDF_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEntity<Object> pushData(ReceiveDataDTO dto) {
        ResultEnum resultEnum = null;
        StringBuilder msg = new StringBuilder("");
        Date startTime = new Date();
        try {
            if (dto.apiCode == null) {
                return ResultEntityBuild.build(ResultEnum.PUSH_TABLEID_NULL);
            }

            ApiConfigPO apiConfigPo = baseMapper.selectById(dto.apiCode);
            if (apiConfigPo == null) {
                return ResultEntityBuild.build(ResultEnum.API_NOT_EXIST);
            }

            // flag=false: 第三方调用,需要验证账号是否属于当前api
            if (!dto.flag) {
                AppDataSourcePO appDataSourcePo = appDataSourceImpl.query().eq("app_id", apiConfigPo.appId).one();
                if (!appDataSourcePo.realtimeAccount.equalsIgnoreCase(userHelper.getLoginUserInfo().username)) {
                    return ResultEntityBuild.build(ResultEnum.ACCOUNT_CANNOT_OPERATION_API);
                }
            }

            // json解析的根节点
            String jsonKey = StringUtils.isNotBlank(apiConfigPo.jsonKey) ? apiConfigPo.jsonKey : "data";
            log.info("json解析的根节点参数为: " + jsonKey);

            // 根据api_id查询所有物理表
            List<TableAccessPO> accessPoList = tableAccessImpl.query().eq("api_id", dto.apiCode).list();
            if (CollectionUtils.isEmpty(accessPoList)) {
                return ResultEntityBuild.build(ResultEnum.TABLE_NOT_EXIST);
            }
            // 获取当前api下的所有表数据
            List<ApiTableDTO> apiTableDtoList = getApiTableDtoList(accessPoList);
//            apiTableDtoList.forEach(System.out::println);

            AppRegistrationPO modelApp = appRegistrationImpl.query().eq("id", accessPoList.get(0).appId).one();
            if (modelApp == null) {
                return ResultEntityBuild.build(ResultEnum.APP_NOT_EXIST);
            }
            // 防止\未被解析
            String jsonStr = StringEscapeUtils.unescapeJava(dto.pushData);
            // 将数据同步到pgsql
            ResultEntity<Object> result = pushPgSql(null, jsonStr, apiTableDtoList, "stg_" + modelApp.appAbbreviation + "_", jsonKey, dto.apiCode);
            resultEnum = ResultEnum.getEnum(result.code);
            msg.append(resultEnum.getMsg()).append(": ").append(result.msg == null ? "" : result.msg);

            // stg同步到ods(联调task)
            if (resultEnum.getCode() == ResultEnum.SUCCESS.getCode()) {
                ResultEnum resultEnum1 = pushDataStgToOds(dto.apiCode, 1);
                log.info("stg表数据用完即删");
                pushDataStgToOds(dto.apiCode, 0);
                msg.append("数据同步到[ods]: ").append(resultEnum1.getMsg()).append("；");
            }

            // 保存本次的日志信息
            // 非实时api
            // 系统内部调用 && 实时推送示例数据
            if (dto.flag && !dto.executeConfigFlag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为正式数据]");
                }
                savePushDataLogToTask(null, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_API.getValue(), msg.toString());
                // 实时调用
                // executeConfigFlag: true -- 本次同步的数据为前端页面测试示例
            } else if (dto.flag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为前端页面测试示例]");
                }
                savePushDataLogToTask(null, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_RESTAPI.getValue(), msg.toString());
            } else if (!dto.executeConfigFlag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为正式数据]");
                }
                savePushDataLogToTask(null, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_RESTAPI.getValue(), msg.toString());
            }

        } catch (Exception e) {
            resultEnum = ResultEnum.PUSH_DATA_ERROR;
        }
        return ResultEntityBuild.build(resultEnum, msg);
    }

    /**
     * task添加日志执行的推送数据方法
     *
     * @param importDataDto 管道传递的参数
     * @param dto           同步api所需的属性
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @author Lock
     * @date 2022/6/17 15:28
     */
    private ResultEntity<Object> pushDataByImportData(ApiImportDataDTO importDataDto, ReceiveDataDTO dto) {
        ResultEnum resultEnum = null;
        StringBuilder msg = new StringBuilder("");
        Date startTime = new Date();
        try {
            if (dto.apiCode == null) {
                return ResultEntityBuild.build(ResultEnum.PUSH_TABLEID_NULL);
            }

            ApiConfigPO apiConfigPo = baseMapper.selectById(dto.apiCode);
            if (apiConfigPo == null) {
                return ResultEntityBuild.build(ResultEnum.API_NOT_EXIST);
            }

            // flag=false: 第三方调用,需要验证账号是否属于当前api
            if (!dto.flag) {
                AppDataSourcePO appDataSourcePo = appDataSourceImpl.query().eq("app_id", apiConfigPo.appId).one();
                if (!appDataSourcePo.realtimeAccount.equalsIgnoreCase(userHelper.getLoginUserInfo().username)) {
                    return ResultEntityBuild.build(ResultEnum.ACCOUNT_CANNOT_OPERATION_API);
                }
            }

            // json解析的根节点
            String jsonKey = StringUtils.isNotBlank(apiConfigPo.jsonKey) ? apiConfigPo.jsonKey : "data";
            log.info("json解析的根节点参数为: " + jsonKey);

            // 根据api_id查询所有物理表
            List<TableAccessPO> accessPoList = tableAccessImpl.query().eq("api_id", dto.apiCode).list();
            if (CollectionUtils.isEmpty(accessPoList)) {
                return ResultEntityBuild.build(ResultEnum.TABLE_NOT_EXIST);
            }
            // 获取当前api下的所有表数据
            List<ApiTableDTO> apiTableDtoList = getApiTableDtoList(accessPoList);
//            apiTableDtoList.forEach(System.out::println);

            AppRegistrationPO modelApp = appRegistrationImpl.query().eq("id", accessPoList.get(0).appId).one();
            if (modelApp == null) {
                return ResultEntityBuild.build(ResultEnum.APP_NOT_EXIST);
            }
            // 防止\未被解析
            String jsonStr = StringEscapeUtils.unescapeJava(dto.pushData);
            // 将数据同步到pgsql
            ResultEntity<Object> result = pushPgSql(importDataDto, jsonStr, apiTableDtoList, "stg_" + modelApp.appAbbreviation + "_", jsonKey, dto.apiCode);
            resultEnum = ResultEnum.getEnum(result.code);
            msg.append(resultEnum.getMsg()).append(": ").append(result.msg == null ? "" : result.msg);

            // stg同步到ods(联调task)
            if (resultEnum.getCode() == ResultEnum.SUCCESS.getCode()) {
                ResultEnum resultEnum1 = pushDataStgToOds(dto.apiCode, 1);
                log.info("stg表数据用完即删");
                pushDataStgToOds(dto.apiCode, 0);
                msg.append("数据同步到[ods]: ").append(resultEnum1.getMsg()).append("；");
            }

            // 保存本次的日志信息
            // 非实时api
            // 系统内部调用 && 实时推送示例数据
            if (dto.flag && !dto.executeConfigFlag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为正式数据]");
                }
                savePushDataLogToTask(importDataDto, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_API.getValue(), msg.toString());
                // 实时调用
                // executeConfigFlag: true -- 本次同步的数据为前端页面测试示例
            } else if (dto.flag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为前端页面测试示例]");
                }
                savePushDataLogToTask(importDataDto, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_RESTAPI.getValue(), msg.toString());
            } else if (!dto.executeConfigFlag) {
                if (StringUtils.isNotBlank(msg)) {
                    msg.deleteCharAt(msg.length() - 1).append("。--[本次同步的数据为正式数据]");
                }
                savePushDataLogToTask(importDataDto, startTime, dto, resultEnum, OlapTableEnum.PHYSICS_RESTAPI.getValue(), msg.toString());
            }

        } catch (Exception e) {
            resultEnum = ResultEnum.PUSH_DATA_ERROR;
        }
        return ResultEntityBuild.build(resultEnum, msg);
    }

    /**
     * task保存日志
     *
     * @return void
     * @description task保存日志
     * @author Lock
     * @date 2022/6/17 15:29
     * @version v1.0
     * @params startTime
     * @params dto
     * @params resultEnum
     * @params topicType
     * @params msg
     */
    private void savePushDataLogToTask(ApiImportDataDTO importDataDto, Date startTime, ReceiveDataDTO dto, ResultEnum resultEnum, int topicType, String msg) {
        ApiConfigPO apiConfigPo = this.query().eq("id", dto.apiCode).one();
        if (apiConfigPo == null) {
            throw new FkException(ResultEnum.APICONFIG_ISNULL);
        }

        NifiStageMessageDTO nifiStageMessageDto = new NifiStageMessageDTO();
        NifiStageDTO nifiStageDto = new NifiStageDTO();
        nifiStageDto.insertPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
        nifiStageDto.transitionPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
        nifiStageDto.queryPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
        try {
            Date endTime = new Date();
            nifiStageMessageDto.message = msg;
            nifiStageDto.comment = msg;
            nifiStageMessageDto.startTime = startTime;
            nifiStageMessageDto.endTime = endTime;
            nifiStageMessageDto.counts = COUNT_SQL / 2;
            nifiStageMessageDto.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + topicType + "." + apiConfigPo.appId + "." + dto.apiCode;
            if (resultEnum.getCode() == ResultEnum.SUCCESS.getCode()) {
                nifiStageDto.insertPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                nifiStageDto.transitionPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
                nifiStageDto.queryPhase = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();

            }
            // 非实时日志详情
            if (importDataDto != null) {
                nifiStageMessageDto.pipelTaskTraceId = importDataDto.pipelTaskTraceId;
                nifiStageMessageDto.pipelStageTraceId = importDataDto.pipelStageTraceId;
                nifiStageMessageDto.pipelJobTraceId = importDataDto.pipelJobTraceId;
                nifiStageMessageDto.pipelTraceId = importDataDto.pipelTraceId;
                PipelApiDispatchDTO pipelApiDispatchDto = JSON.parseObject(importDataDto.pipelApiDispatch, PipelApiDispatchDTO.class);
                if (pipelApiDispatchDto != null) {
                    nifiStageDto.componentId = Integer.parseInt(pipelApiDispatchDto.workflowId);
                }
            }
            nifiStageMessageDto.nifiStageDTO = nifiStageDto;

            log.info("保存到task的日志信息: " + JSON.toJSONString(nifiStageMessageDto));
            publishTaskClient.saveNifiStage(JSON.toJSONString(nifiStageMessageDto));
        } catch (Exception e) {
            Date endTime = new Date();
            nifiStageMessageDto.message = msg == null ? "运行失败" : msg;
            nifiStageDto.comment = msg == null ? "运行失败" : msg;
            nifiStageMessageDto.startTime = startTime;
            nifiStageMessageDto.endTime = endTime;
            nifiStageMessageDto.counts = COUNT_SQL;
            nifiStageMessageDto.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + topicType + "." + apiConfigPo.appId + "." + dto.apiCode;
            // 非实时日志详情
            if (importDataDto != null) {
                nifiStageMessageDto.pipelTaskTraceId = importDataDto.pipelTaskTraceId;
                nifiStageMessageDto.pipelStageTraceId = importDataDto.pipelStageTraceId;
                nifiStageMessageDto.pipelJobTraceId = importDataDto.pipelJobTraceId;
                nifiStageMessageDto.pipelTraceId = importDataDto.pipelTraceId;
                PipelApiDispatchDTO pipelApiDispatchDto = JSON.parseObject(importDataDto.pipelApiDispatch, PipelApiDispatchDTO.class);
                if (pipelApiDispatchDto != null) {
                    nifiStageDto.componentId = Integer.parseInt(pipelApiDispatchDto.workflowId);
                }
            }

            nifiStageMessageDto.nifiStageDTO = nifiStageDto;
            log.info("保存到task的日志信息: " + JSON.toJSONString(nifiStageMessageDto));
            publishTaskClient.saveNifiStage(JSON.toJSONString(nifiStageMessageDto));
        }
    }

    @Override
    public ResultEntity<String> getToken(ApiUserDTO dto) {

        // 根据账号名称查询对应的app_id下
        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("realtime_account", dto.getUseraccount()).one();
        if (!dataSourcePo.realtimeAccount.equals(dto.getUseraccount()) || !dataSourcePo.realtimePwd.equals(dto.getPassword())) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR, ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR.getMsg());
        }
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(dto.useraccount);
        userAuthDTO.setPassword(dto.password);
        userAuthDTO.setTemporaryId(RedisTokenKey.DATA_ACCESS_TOKEN + dataSourcePo.id);

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

    @Override
    public ResultEnum importData(ApiImportDataDTO dto) {
        // task根据调度配置调用
        //List<PipelApiDispatchDTO> pipelApiDispatchs = JSON.(dto.pipelApiDispatch, PipelApiDispatchDTO.class);
        PipelApiDispatchDTO pipelApiDispatch = JSON.parseObject(dto.pipelApiDispatch, PipelApiDispatchDTO.class);

        if (!Objects.isNull(pipelApiDispatch)) {
            dto.workflowId = pipelApiDispatch.workflowId;
            dto.appId = pipelApiDispatch.appId;
            dto.apiId = pipelApiDispatch.apiId;
            syncData(dto, null);
            consumer(dto);
        } else {
            // 接入模块调用
            syncData(dto, null);
            if (dto.workflowId != null) {
                consumer(dto);
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 调用管道下一级task
     *
     * @param dto dto
     * @author cfk
     * @date 2022/6/22 11:31
     */
    public void consumer(ApiImportDataDTO dto) {
        KafkaReceiveDTO kafkaReceiveDTO = new KafkaReceiveDTO();
        kafkaReceiveDTO.pipelTraceId = dto.pipelTraceId;
        kafkaReceiveDTO.pipelTaskTraceId = dto.pipelTaskTraceId;
        kafkaReceiveDTO.pipelStageTraceId = dto.pipelStageTraceId;
        kafkaReceiveDTO.pipelJobTraceId = dto.pipelJobTraceId;
        kafkaReceiveDTO.numbers = COUNT_SQL;
        kafkaReceiveDTO.tableId = Math.toIntExact(dto.apiId);
        kafkaReceiveDTO.tableType = OlapTableEnum.PHYSICS_API.getValue();
        kafkaReceiveDTO.topic = MqConstants.TopicPrefix.TOPIC_PREFIX + dto.workflowId + "." + kafkaReceiveDTO.tableType + "." + dto.appId + "." + dto.apiId;
        kafkaReceiveDTO.nifiCustomWorkflowDetailId = Long.valueOf(dto.workflowId);
        kafkaReceiveDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
        kafkaReceiveDTO.pipelApiDispatch = dto.pipelApiDispatch;
        publishTaskClient.consumer(JSON.toJSONString(kafkaReceiveDTO));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum copyApi(CopyApiDTO dto) {

        // TODO 复制api功能,仅保存配置,关于是否发布,后面再讨论
        // 1.查询出当前api实时还是非实时
        AppRegistrationPO appRegistrationPo = appRegistrationImpl.query().eq("id", dto.getAppId()).one();
        if (appRegistrationPo == null) {
            return ResultEnum.API_APP_ISNULL;
        }

        if (!CollectionUtils.isEmpty(dto.getApiIds())) {
            for (Long apiId : dto.getApiIds()) {
                // 2.根据apiId查询当前api信息tb_api_config: 调用/apiConfig/add接口,保存tb_api_config主表信息
                ApiConfigPO apiConfigPo = this.query().eq("id", apiId).one();
                if (apiConfigPo == null) {
                    return ResultEnum.COPY_API_ISNULL;
                }
                // 复制功能,重置id
                apiConfigPo.id = 0;
                apiConfigPo.appId = dto.getAppId();
                apiConfigPo.apiName = apiConfigPo.apiName + "_copy";
                // 重置发布状态
                apiConfigPo.publish = 0;

                boolean checkApiName = checkApiName(ApiConfigMap.INSTANCES.poToDto(apiConfigPo));
                if (checkApiName) {
                    throw new FkException(ResultEnum.COPY_APINAME_ISEXIST);
                }

                this.save(apiConfigPo);

                // 2-1.实时不需要保存请求参数表tb_api_parameter
                // 2-2.非实时需要保存请求参数表: 保存tb_api_parameter表信息
                if (appRegistrationPo.appType == 1) { // 1: 非实时api
                    List<ApiParameterPO> apiParameterPoList = apiParameterServiceImpl.query().eq("api_id", apiId).list();
                    if (!CollectionUtils.isEmpty(apiParameterPoList)) {
                        apiParameterPoList.forEach(e -> {
                            e.id = 0;
                            e.apiId = apiConfigPo.id;
                        });
                        apiParameterServiceImpl.addData(ApiParameterMap.INSTANCES.listPoToDto(apiParameterPoList));
                    }
                }

                // 3.保存json结构的所有表节点信息: 循环调用/v3/tableAccess/add接口
                List<TableAccessNonDTO> list = new ArrayList<>();
                List<TableAccessPO> tableAccessPoList = tableAccessImpl.query().eq("api_id", apiId).list();
                if (!CollectionUtils.isEmpty(tableAccessPoList)) {
                    for (TableAccessPO e : tableAccessPoList) {
                        TbTableAccessDTO tbTableAccessDto = TableAccessMap.INSTANCES.tbPoToDto(e);
                        // 重置id
                        tbTableAccessDto.id = 0;
                        // 重置发布状态
                        tbTableAccessDto.publish = 0;
                        tbTableAccessDto.apiId = apiConfigPo.id;
                        tbTableAccessDto.appId = dto.getAppId();
                        ResultEntity<Object> result = tableAccessImpl.addTableAccessData(tbTableAccessDto);
                        Object tableId = result.getData();
                        if (result.getData() == null) {
                            log.error("复制api下的表失败: " + result.msg);
                            throw new FkException(ResultEnum.COPY_API_TABLE_ERROR);
                        }
                        TableAccessNonDTO data = tableAccessImpl.getData((Long) tableId);
                        // 组装同步表信息
                        TableSyncmodePO tableSyncmodePo = tableSyncmodeImpl.query().eq("id", e.id).one();
                        if (tableSyncmodePo != null) {
                            TableSyncmodeDTO tableSyncmodeDto = TableSyncModeMap.INSTANCES.poToDto(tableSyncmodePo);
                            tableSyncmodeDto.id = data.id;
                            data.tableSyncmodeDTO = tableSyncmodeDto;
                        }
                        // 组装业务表信息
                        TableBusinessPO tableBusinessPo = tableBusinessImpl.query().eq("access_id", e.id).one();
                        if (tableBusinessPo != null) {
                            tableBusinessPo.accessId = data.id;
                            data.businessDTO = TableBusinessMap.INSTANCES.poToDto(tableBusinessPo);
                        }
                        // 组装字段详情表数据
                        List<TableFieldsPO> tableFieldsPoList = tableFieldImpl.query().eq("table_access_id", e.id).list();
                        List<TableFieldsDTO> tableFieldsDtoList = TableFieldsMap.INSTANCES.listPoToDto(tableFieldsPoList);
                        if (!CollectionUtils.isEmpty(tableFieldsDtoList)) {
                            // 组装字段详情表信息
                            tableFieldsDtoList.forEach(f -> {
                                f.id = 0;
                                f.tableAccessId = data.id;
                            });
                            data.list = tableFieldsDtoList;
                        }
                        // 封装所有数据
                        list.add(data);
                    }
                }
                // 4.调用apiConfig/addApiDetail接口
                ApiConfigDTO apiConfigDto = ApiConfigMap.INSTANCES.poToDto(apiConfigPo);
                if (!CollectionUtils.isEmpty(list)) {
                    apiConfigDto.list = list;
                }
                this.addApiDetail(apiConfigDto);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ApiSelectDTO> getAppAndApiList(int appType) {

        // 查询所有app_id和app_name
        List<AppRegistrationPO> list = appRegistrationImpl.list(Wrappers.<AppRegistrationPO>lambdaQuery()
                .eq(AppRegistrationPO::getAppType, appType)
                .select(AppRegistrationPO::getId, AppRegistrationPO::getAppName, AppRegistrationPO::getAppType)
                .orderByDesc(AppRegistrationPO::getCreateTime));

        List<AppRegistrationPO> appRegistrationPoList = new ArrayList<>();
        // 只需要RestfulAPI和api类型
        list.forEach(e -> {
            AppDataSourcePO appDataSourcePo = appDataSourceImpl.query().eq("app_id", e.id).one();
            if (DataSourceTypeEnum.API.getName().equalsIgnoreCase(appDataSourcePo.driveType) || DataSourceTypeEnum.RestfulAPI.getName().equalsIgnoreCase(appDataSourcePo.driveType)) {
                appRegistrationPoList.add(e);
            }
        });

        return ApiConfigMap.INSTANCES.listPoToApiSelectDto(appRegistrationPoList);
    }

    /**
     * 根据配置执行api功能
     *
     * @return void
     * @description 根据配置执行api功能
     * @author Lock
     * @date 2022/5/10 15:31
     * @version v1.0
     * @params dto
     */
    public ResultEnum syncData(ApiImportDataDTO dto, List<ApiParameterPO> apiParameters) {
        // 根据appId获取应用信息(身份验证方式,验证参数)
        // 根据apiId获取非实时api信息(uri 请求方式  请求参数  json解析  推送数据  同步方式)
        String data = "";
        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        int pageNum = 1;
        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("app_id", dto.appId).one();
        if (dataSourcePo == null) {
            return ResultEnum.DATASOURCE_INFORMATION_ISNULL;
        }
        ApiConfigPO apiConfigPo = this.query().eq("id", dto.apiId).one();
        if (apiConfigPo == null) {
            return ResultEnum.APICONFIG_ISNULL;
        }
        List<ApiParameterPO> parameterPoList = new ArrayList<>();
        //范本
        List<ApiParameterPO> collect = apiParameterServiceImpl.query().eq("api_id", dto.apiId).list().stream()
                .filter(e -> e.parameterValue.toLowerCase().contains(ApiConditionEnum.PAGENUM.getName().toLowerCase())).collect(Collectors.toList());
        // api的请求参数(允许为空)
        // 用apiParameters里面的值覆盖parameterPoList
        if (CollectionUtils.isEmpty(apiParameters)) {
            //实际参数
            apiParameters = ApiParameterMap.INSTANCES.listDtoToPo(iApiCondition.apiConditionAppend(dto.apiId));
            //找到那个value
            if (!CollectionUtils.isEmpty(collect)) {
                // 校验完成后每次推送数据前,将stg数据删除;解析上游的数据为空,本次也不需要同步数据,stg临时表也不用删
                pushDataStgToOds(dto.apiId, 0);
                for (ApiParameterPO apiParameterPO : collect) {
                    for (ApiParameterPO apiParameterPO1 : apiParameters) {
                        if (Objects.equals(apiParameterPO.parameterKey, apiParameterPO1.parameterKey)) {
                            apiParameterPO1.parameterValue = "1";
                            pageNum = Integer.parseInt(apiParameterPO1.parameterValue);
                        }
                    }
                }
            }
            parameterPoList = apiParameters;
        } else {
            //加1
            if (!CollectionUtils.isEmpty(collect)) {
                for (ApiParameterPO apiParameterPO : collect) {
                    for (ApiParameterPO apiParameterPO1 : apiParameters) {
                        if (Objects.equals(apiParameterPO.parameterKey, apiParameterPO1.parameterKey)) {
                            apiParameterPO1.parameterValue = String.valueOf(Integer.parseInt(apiParameterPO1.parameterValue) + 1);
                            pageNum = Integer.parseInt(apiParameterPO1.parameterValue);
                        }
                    }
                }
            }
            parameterPoList = apiParameters;
        }
        String formDataString = "form-data";
        String rawString = "raw";
        String bodyString = "Body";
        String headersString = "Headers";
        // Body: form-data参数
        List<ApiParameterPO> formDataParams = parameterPoList.stream().filter(e -> e.requestMethod.equalsIgnoreCase(formDataString) && e.requestType.equalsIgnoreCase(bodyString)).collect(Collectors.toList());
        // Body: raw参数
        List<ApiParameterPO> rawParams = parameterPoList.stream().filter(e -> e.requestMethod.equalsIgnoreCase(rawString) && e.requestType.equalsIgnoreCase(bodyString)).collect(Collectors.toList());
        // Headers的参数
        List<ApiParameterPO> headersParams = parameterPoList.stream().filter(e -> e.requestType.equalsIgnoreCase(headersString)).collect(Collectors.toList());
        // 封装请求头Headers的参数
        Map<String, String> params = new HashMap<>();
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(headersParams)) {
            params = headersParams.stream().collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b));
        }

        if (dataSourcePo.authenticationMethod == 3) { // JWT版
            // jwt身份验证方式对象
            ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
            apiHttpRequestDto.httpRequestEnum = POST;
            // 身份验证地址
            apiHttpRequestDto.uri = dataSourcePo.connectStr;
            // jwt账号&密码
            JwtRequestDTO jwtRequestDto = new JwtRequestDTO();
            jwtRequestDto.userKey = dataSourcePo.accountKey;
            jwtRequestDto.username = dataSourcePo.connectAccount;
            jwtRequestDto.pwdKey = dataSourcePo.pwdKey;
            jwtRequestDto.password = dataSourcePo.connectPwd;
            apiHttpRequestDto.jwtRequestDTO = jwtRequestDto;

            IBuildHttpRequest iBuildHttpRequest = ApiHttpRequestFactoryHelper.buildHttpRequest(apiHttpRequestDto);

            //获取token返回json串格式
            List<ApiResultConfigDTO> apiResultConfig = apiResultConfigImpl.getApiResultConfig(dataSourcePo.id);
            Optional<ApiResultConfigDTO> first = apiResultConfig.stream().filter(e -> e.checked == true).findFirst();
            apiHttpRequestDto.jsonDataKey = "token";
            if (first.isPresent()) {
                apiHttpRequestDto.jsonDataKey = first.get().name;
                //throw new FkException(ResultEnum.RETURN_RESULT_DEFINITION);
            }

            // 获取token
            String requestToken = iBuildHttpRequest.getRequestToken(apiHttpRequestDto);
            log.info("token参数:" + JSON.toJSONString(apiHttpRequestDto) + "token值" + requestToken);
            if (StringUtils.isBlank(requestToken)) {
                return ResultEnum.GET_JWT_TOKEN_ERROR;
            }

            apiHttpRequestDto.uri = apiConfigPo.apiAddress;
            apiHttpRequestDto.requestHeader = requestToken;
            if (apiConfigPo.apiRequestType == 1) {
                apiHttpRequestDto.httpRequestEnum = GET;
            } else if (apiConfigPo.apiRequestType == 2) {
                apiHttpRequestDto.httpRequestEnum = POST;
                // post请求携带的请求参数  Body: raw参数
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(rawParams)) {
                    apiHttpRequestDto.jsonObject = rawParams.stream().collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b, JSONObject::new));
                }

                // post请求携带的请求参数Body: form-data参数
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(formDataParams)) {
                    apiHttpRequestDto.formDataParams = formDataParams.stream()
                            .collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b));
                }
            }

            // 请求头参数
            apiHttpRequestDto.headersParams = params;

            // TODO 第一步: 查询阶段,调用第三方api返回的数据
            JSONObject jsonObject = iBuildHttpRequest.httpRequest(apiHttpRequestDto);
            log.info("iBuildHttpRequest对象值:{},{},{}", JSON.toJSONString(apiHttpRequestDto), JSON.toJSONString(iBuildHttpRequest), JSON.toJSONString(jsonObject));

            receiveDataDTO = new ReceiveDataDTO();
            receiveDataDTO.apiCode = dto.apiId;
            data = String.valueOf(jsonObject);
            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            log.info("data = " + data);
            receiveDataDTO.pushData = String.valueOf(data);
            //系统内部调用(非实时推送)
            receiveDataDTO.flag = true;
            receiveDataDTO.executeConfigFlag = false;


        } else if (dataSourcePo.authenticationMethod == 5) { // 没有身份验证方式

            ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
            apiHttpRequestDto.uri = apiConfigPo.apiAddress;
            if (apiConfigPo.apiRequestType == 1) {
                apiHttpRequestDto.httpRequestEnum = GET;
            } else if (apiConfigPo.apiRequestType == 2) {
                apiHttpRequestDto.httpRequestEnum = POST;
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(rawParams)) {
                    apiHttpRequestDto.jsonObject = rawParams.stream().collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b, JSONObject::new));
                }
                // post请求携带的请求参数Body: form-data参数
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(formDataParams)) {
                    apiHttpRequestDto.formDataParams = formDataParams.stream()
                            .collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b));
                }
            }

            // 请求头参数
            apiHttpRequestDto.headersParams = params;

            IBuildHttpRequest iBuildHttpRequest = ApiHttpRequestFactoryHelper.buildHttpRequest(apiHttpRequestDto);
            // 调用第三方api返回的数据
            JSONObject jsonObject = iBuildHttpRequest.httpRequest(apiHttpRequestDto);
            log.info("iBuildHttpRequest对象值:{},{},{}", JSON.toJSONString(apiHttpRequestDto), JSON.toJSONString(iBuildHttpRequest), JSON.toJSONString(jsonObject));

            receiveDataDTO = new ReceiveDataDTO();
            receiveDataDTO.apiCode = dto.apiId;
            data = String.valueOf(jsonObject);
            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            log.info("data = " + data);
            receiveDataDTO.pushData = String.valueOf(data);
            // 系统内部调用(非实时推送)
            receiveDataDTO.flag = true;
            receiveDataDTO.executeConfigFlag = false;

            // 推送数据
            //pushDataByImportData(dto, receiveDataDTO);

            // Bearer Token
        } else if (dataSourcePo.authenticationMethod == 4) {

            ApiHttpRequestDTO apiHttpRequestDto = new ApiHttpRequestDTO();
            apiHttpRequestDto.uri = apiConfigPo.apiAddress;
            apiHttpRequestDto.requestHeader = dataSourcePo.token;
            if (apiConfigPo.apiRequestType == 1) {
                apiHttpRequestDto.httpRequestEnum = GET;
            } else if (apiConfigPo.apiRequestType == 2) {
                apiHttpRequestDto.httpRequestEnum = POST;
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(rawParams)) {
                    apiHttpRequestDto.jsonObject = rawParams.stream().collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b, JSONObject::new));
                }
                // post请求携带的请求参数Body: form-data参数
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(formDataParams)) {
                    apiHttpRequestDto.formDataParams = formDataParams.stream()
                            .collect(Collectors.toMap(e -> e.parameterKey, e -> e.parameterValue, (a, b) -> b));
                }
            }

            // 请求头参数
            apiHttpRequestDto.headersParams = params;

            IBuildHttpRequest iBuildHttpRequest = ApiHttpRequestFactoryHelper.buildHttpRequest(apiHttpRequestDto);
            // 调用第三方api返回的数据
            JSONObject jsonObject = iBuildHttpRequest.httpRequest(apiHttpRequestDto);
            log.info("iBuildHttpRequest对象值:{},{},{}", JSON.toJSONString(apiHttpRequestDto), JSON.toJSONString(iBuildHttpRequest), JSON.toJSONString(jsonObject));
            receiveDataDTO = new ReceiveDataDTO();
            receiveDataDTO.apiCode = dto.apiId;
            data = String.valueOf(jsonObject);
            log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            log.info("data = " + data);
            receiveDataDTO.pushData = String.valueOf(data);
            // 系统内部调用(非实时推送)
            receiveDataDTO.flag = true;
            receiveDataDTO.executeConfigFlag = false;
            // 推送数据
            //pushDataByImportData(dto, receiveDataDTO);
        }
        log.info("data的值" + JSON.toJSONString(data));
        // json解析的根节点
        String jsonKey = StringUtils.isNotBlank(apiConfigPo.jsonKey) ? apiConfigPo.jsonKey : "data";
        JSONArray jsonArray = JSON.parseObject(data).getJSONArray(jsonKey);
        log.info("动态参数再次调用:第几{}页", pageNum);
        if (!CollectionUtils.isEmpty(jsonArray) && jsonArray.size() != 0 && pageNum < Integer.parseInt(ApiParameterTypeEnum.MAX_PAGE.getName())) {
            // 推送数据
            pushDataByImportData(dto, receiveDataDTO);
            syncData(dto, apiParameters);
        }
        return ResultEnum.SUCCESS;
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
    private List<ApiTableDTO> getApiTableDtoList(List<TableAccessPO> accessPoList) {
        // 根据table_id获取物理表详情
        List<TableAccessNonDTO> poList = accessPoList.stream().map(e -> tableAccessImpl.getData(e.id)).collect(Collectors.toList());

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
     * @param jsonStr         json数据
     * @param apiTableDtoList 当前api下的所有表(父子级结构)
     * @param tablePrefixName stg_应用简称
     * @param apiId           apiId
     * @param jsonKey         json解析的根节点(一般为data)
     * @return void
     * @description 将数据同步到pgsql
     * @author Lock
     * @date 2022/2/16 19:17
     */
    private ResultEntity<Object> pushPgSql(ApiImportDataDTO importDataDto, String jsonStr, List<ApiTableDTO> apiTableDtoList,
                                           String tablePrefixName, String jsonKey, Long apiId) {
        ResultEnum resultEnum;
        // 初始化数据
        StringBuilder checkResultMsg = new StringBuilder();
        // ods_应用简称
        String replaceTablePrefixName = tablePrefixName.replace("stg_", "ods_");
        List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
        JsonUtils jsonUtils = new JsonUtils();
        List<JsonTableData> targetTable = jsonUtils.getTargetTable(tableNameList);
//            targetTable.forEach(System.out::println);
        // 获取Json的schema信息
        List<JsonSchema> schemas = jsonUtils.getJsonSchema(apiTableDtoList, jsonKey);
//            schemas.forEach(System.out::println);
        // json根节点处理
        try {
            JSONObject json = JSON.parseObject(jsonStr);
            jsonUtils.rootNodeHandler(schemas, json, targetTable);
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.JSON_ROOTNODE_HANDLER_ERROR);
        }
        targetTable.forEach(System.out::println);
        try {
            // TODO 先去数据质量验证
            // 实例(url信息)  库  json解析完的参数(List<JsonTableData>)
            if (!CollectionUtils.isEmpty(targetTable)) {
                DataCheckWebDTO dto = new DataCheckWebDTO();
                dto.ip = dataQualityCheckIp;
                dto.dbName = dataQualityCheckName;

                HashMap<String, JSONArray> body = new HashMap<>();
                for (JsonTableData jsonTableData : targetTable) {
                    body.put(replaceTablePrefixName + jsonTableData.table, jsonTableData.data);
                }

                dto.body = body;
                // 如果检验的feign接口没有调通,当前的校验也不算通过,就不能去执行同步数据的sql
                ResultEntity<List<DataCheckResultVO>> result = dataQualityClient.interfaceCheckData(dto);
                log.info("数据质量校验结果通知: " + JSON.toJSONString(result));
                // 数据校验结果
                if (result.code == ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS.getCode()) {
                    List<DataCheckResultVO> data = result.data;
                    if (!CollectionUtils.isEmpty(data)) {
                        for (DataCheckResultVO e : data) {
                            // 强规则校验: 循环结果集,出现一个强规则,代表这一批数据其他规则通过已经不重要,返回失败
                            if (e.checkRule == CheckRuleEnum.STRONG_RULE.getValue()) {
                                return ResultEntityBuild.build(ResultEnum.FIELD_CKECK_NOPASS, e.checkResultMsg);
                            } else if (e.checkRule == CheckRuleEnum.WEAK_RULE.getValue()) {
                                checkResultMsg.append(e.checkResultMsg).append("；");
                            } else {
                                return ResultEntityBuild.build(ResultEnum.getEnum(result.code), result.msg);
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.DATA_QUALITY_FEIGN_ERROR);
        }
        System.out.println("开始执行sql");
        PgsqlUtils pgsqlUtils = new PgsqlUtils();
        // stg_abbreviationName_tableName
        ResultEntity<Object> excuteResult;
        try {
            if (importDataDto == null) {
                excuteResult = pgsqlUtils.executeBatchPgsql(tablePrefixName, targetTable, apiTableDtoList);
            } else {
                excuteResult = pgsqlUtils.executeBatchPgsql(importDataDto, tablePrefixName, targetTable, apiTableDtoList);
            }
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.PUSH_DATA_SQL_ERROR);
        }
        resultEnum = ResultEnum.getEnum(excuteResult.code);

        List<ApiSqlResultDTO> list = JSONArray.parseArray(excuteResult.msg.toString(), ApiSqlResultDTO.class);
        if (!CollectionUtils.isEmpty(list)) {
            for (ApiSqlResultDTO e : list) {
                checkResultMsg.append("数据推送到").append("[").append(e.getTableName()).append("]").append(": ")
                        .append(e.getMsg()).append(",").append("推送的条数为: ").append(e.getCount()).append("；");
                COUNT_SQL = e.getCount();
                // 推送条数累加值
                COUNT_SQL += COUNT_SQL;
            }
        }


        return ResultEntityBuild.build(resultEnum, checkResultMsg.toString());
    }

    /**
     * 将数据从stg同步到ods
     *
     * @param apiId apiId
     * @param flag  0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     * @return void
     * @author Lock
     * @date 2022/2/25 16:41
     */
    private ResultEnum pushDataStgToOds(Long apiId, int flag) {

        try {
            // 1.根据apiId获取api所有信息
            ApiConfigPO apiConfigPo = baseMapper.selectById(apiId);
            if (apiConfigPo == null) {
                return ResultEnum.API_NOT_EXIST;
            }
            // 2.根据appId获取app所有信息
            AppRegistrationPO app = appRegistrationImpl.query().eq("id", apiConfigPo.appId).one();
            if (app == null) {
                return ResultEnum.APP_NOT_EXIST;
            }

            // 3.根据apiId查询所有物理表详情
            ApiConfigDTO dto = getData(apiId);
            List<TableAccessNonDTO> tablelist = dto.list;
            if (CollectionUtils.isEmpty(tablelist)) {
                // 当前api下没有物理表
                return ResultEnum.TABLE_NOT_EXIST;
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
                // 装接入数据库api的字段
                List<TableFieldsPO> list = tableFieldImpl.query().eq("table_access_id", e.id).eq("del_flag", 1).list();
                dataSourceConfig.tableFieldsList = TableFieldsMap.INSTANCES.listPoToDto(list);
                // 增量对象
                if (syncmodePo.syncMode == 4) {
                    TableBusinessPO businessPo = tableBusinessImpl.query().eq("access_id", e.id).one();
                    configDTO.businessDTO = TableBusinessMap.INSTANCES.poToDto(businessPo);
                }

                // 业务主键集合(逗号隔开)
                List<TableFieldsDTO> fieldList = e.list;
                if (!CollectionUtils.isEmpty(fieldList)) {
                    String collect = fieldList.stream().filter(f -> f.isPrimarykey == 1).map(f -> f.fieldName + ",").collect(Collectors.joining());
                    // 去掉最后一位逗号","
                    if (StringUtils.isNotBlank(collect)) {
                        configDTO.businessKeyAppend = collect.substring(0, collect.length() - 1);
                    }
                }

                configDTO.processorConfig = processorConfig;
                configDTO.targetDsConfig = dataSourceConfig;

                // 获取同步数据的sql并执行
                return getSynchroDataSqlAndExcute(configDTO, flag);
            }
        } catch (Exception e) {
            return ResultEnum.PUSH_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 获取同步数据的sql并执行
     *
     * @param configDTO task需要的参数
     * @param flag      0: 推送数据前清空stg; 1: 推送完数据,开始同步stg->ods
     * @return void
     * @author Lock
     * @date 2022/2/25 16:06
     */
    private ResultEnum getSynchroDataSqlAndExcute(DataAccessConfigDTO configDTO, int flag) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            // 调用task,获取同步数据的sql
            log.info("同步sql入参AE87: " + JSON.toJSONString(configDTO));
            ResultEntity<List<String>> result = publishTaskClient.getSqlForPgOds(configDTO);
            log.info("task返回的执行sqlAE88: " + JSON.toJSONString(result));
            if (result.code == ResultEnum.SUCCESS.getCode()) {
                List<String> sqlList = JSON.parseObject(JSON.toJSONString(result.data), List.class);
                if (!CollectionUtils.isEmpty(sqlList)) {
                    PgsqlUtils pgsqlUtils = new PgsqlUtils();
                    resultEnum = pgsqlUtils.stgToOds(sqlList, flag);
                }
            }
        } catch (SQLException e) {
            return ResultEnum.STG_TO_ODS_ERROR;
        }
        return resultEnum;
    }

    /**
     * 根据api_id查询物理表集合
     *
     * @param id api_id
     * @return java.util.List<com.fisk.dataaccess.entity.TableAccessPO>
     * @description 根据api_id查询物理表集合
     * @author Lock
     * @date 2022/2/15 10:30
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

        String jsonResult = DATAACCESS_APIBASICINFO.replace("{api_uat_address}", pdf_uat_address)
                .replace("{api_prd_address}", pdf_prd_address);

        ApiDocDTO apiDocDTO = JSON.parseObject(jsonResult, ApiDocDTO.class);
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
        List<ApiBasicInfoDTO> apiBasicInfoDtoS = new ArrayList<>();
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
        List<ApiRequestDTO> apiRequestDtoS = new ArrayList<>();
        ApiRequestDTO apiId = new ApiRequestDTO();
        apiId.parmName = "apiCode";
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
        apiRequestDtoS.add(apiId);
        apiRequestDtoS.add(pushData);
        apiBasicInfoDTO.apiRequestDTOS = apiRequestDtoS;
        apiBasicInfoDTO.apiRequestExamples = String.format("{\n" +
                " &nbsp;&nbsp;\"apiCode\": \"xxx\",\n" +
                " &nbsp;&nbsp;\"pushData\": \"xxx\"\n" +
                "}", addIndex + ".7");

        // 参数(body)表格(2.5.9返回参数说明)
        List<ApiResponseDTO> apiResponseDtoS = new ArrayList<>();
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
        apiResponseDtoS.add(code);
        apiResponseDtoS.add(msg);
        apiResponseDtoS.add(data);
        apiBasicInfoDTO.apiResponseDTOS = apiResponseDtoS;

        //设置API返回参数,即返回示例(3)
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

        apiBasicInfoDtoS.add(apiBasicInfoDTO);

        apiDocDTO.apiBasicInfoDTOS.addAll(apiBasicInfoDtoS);
        return apiDocDTO;
    }

    /**
     * 创建模板填充数据所需的对象(以应用为单位)
     *
     * @return com.fisk.dataaccess.dto.api.doc.doc.ApiDocDTO
     * @description 创建模板填充数据所需的对象(以应用为单位)
     * @author Lock
     * @date 2022/3/11 14:49
     * @version v1.0
     * @params dtoList
     */
    private ApiDocDTO createApiDocDTO(List<ApiConfigDTO> dtoList) {

        String jsonResult = DATAACCESS_APIBASICINFO.replace("{api_uat_address}", pdf_uat_address)
                .replace("{api_prd_address}", pdf_prd_address);

        ApiDocDTO apiDocDTO = JSON.parseObject(jsonResult, ApiDocDTO.class);

        // API文档代码示例 c#
        apiDocDTO.apiCodeExamplesNet = DATAACCESS_APICODEEXAMPLES_NET.replace("{api_prd_address}", pdf_uat_address);
        // API文档代码示例 java
        apiDocDTO.apiCodeExamplesJava = DATAACCESS_APICODEEXAMPLES_JAVA.replace("{api_prd_address}", pdf_uat_address);

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
        List<ApiBasicInfoDTO> apiBasicInfoDtoS = new ArrayList<>();

        for (int i = 0; i < dtoList.size(); i++) {
            ApiConfigDTO dto = dtoList.get(i);
            List<TableAccessNonDTO> tableAccessDtoList = dto.list;
            if (CollectionUtils.isEmpty(tableAccessDtoList)) {
                return apiDocDTO;
            }

            // 设置目录
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
            apiDocDTO.apiCatalogueDTOS.add(apiDocDTO.apiCatalogueDTOS.size() - 3, apiCatalogueDTO);
            catalogueIndex = addIndex;

            // 设置API基础信息(2.5.-2.5.5)
            ApiBasicInfoDTO apiBasicInfoDTO = new ApiBasicInfoDTO();
            apiBasicInfoDTO.apiName = dto.apiName;
            apiBasicInfoDTO.apiAddress = "/dataAccess/apiConfig/pushdata";
            apiBasicInfoDTO.apiDesc = dto.apiDes;
            apiBasicInfoDTO.apiRequestType = "POST";
            apiBasicInfoDTO.apiContentType = "application/json";
            apiBasicInfoDTO.apiHeader = "Authorization: Bearer {token}";

            // 设置API请求参数(2.5.7 参数body)
            List<ApiRequestDTO> apiRequestDtoS = new ArrayList<>();
            ApiRequestDTO apiId = new ApiRequestDTO();
            apiId.parmName = "apiCode";
            apiId.isRequired = "是";
            apiId.parmType = "String";
            apiId.parmDesc = "api唯一标识: " + dto.id + " (真实数据)";
            apiId.trStyle = "background-color: #fff";
            ApiRequestDTO pushData = new ApiRequestDTO();
            pushData.parmName = "pushData";
            pushData.isRequired = "是";
            pushData.parmType = "String";
            pushData.parmDesc = "json序列化数据(参数格式及字段类型参考本小节【pushData json格式】及【json字段描述】)";
            pushData.trStyle = "background-color: #f8f8f8";
            apiRequestDtoS.add(apiId);
            apiRequestDtoS.add(pushData);
            apiBasicInfoDTO.apiRequestDTOS = apiRequestDtoS;
            apiBasicInfoDTO.apiRequestExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"apiCode\": \"xxx\",\n" +
                    " &nbsp;&nbsp;\"pushData\": \"xxx\"\n" +
                    "}", addIndex + ".7");

            // 参数(body)表格(2.5.9返回参数说明)
            List<ApiResponseDTO> apiResponseDtoS = new ArrayList<>();
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
            apiResponseDtoS.add(code);
            apiResponseDtoS.add(msg);
            apiResponseDtoS.add(data);
            apiBasicInfoDTO.apiResponseDTOS = apiResponseDtoS;

            //设置API返回参数,即返回示例(3)
            apiBasicInfoDTO.apiResponseExamples = String.format("{\n" +
                    " &nbsp;&nbsp;\"code\": 0,\n" +
                    " &nbsp;&nbsp;\"msg\": \"xxx\",\n" +
                    " &nbsp;&nbsp;\"data\": null\n" +
                    "}", addIndex + ".9");

            // pushData json格式
            if (StringUtils.isNotBlank(dto.pushDataJson)) {
                apiBasicInfoDTO.pushDataJson = dto.pushDataJson;
            } else {// 防止模板报错
                apiBasicInfoDTO.pushDataJson = "&nbsp;&nbsp;No parameters";
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
                        apiResponseDTO.parmPushRule = f.fieldPushRule;
                        apiResponseDTO.parmPushExample = f.fieldPushExample;
                        apiResponseDTO.trStyle = trIndex[0] % 2 == 0 ? "background-color: #f8f8f8" : "background-color: #fff";
                        pushDataDtos.add(apiResponseDTO);
                        trIndex[0]++;
                    });
                });
            } else {// 防止模板报错
                ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
                apiResponseDTO.tableName = "No parameters";
                apiResponseDTO.parmName = "No parameters";
                apiResponseDTO.parmType = "No parameters";
                apiResponseDTO.parmDesc = "No parameters";
                apiResponseDTO.trStyle = "background-color: #f8f8f8";
                pushDataDtos.add(apiResponseDTO);
            }
            apiBasicInfoDTO.pushDataDtos = pushDataDtos;


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

            apiBasicInfoDtoS.add(apiBasicInfoDTO);
        }

        apiDocDTO.apiBasicInfoDTOS.addAll(apiBasicInfoDtoS);
        return apiDocDTO;
    }

    /**
     * 根据api id获取配置数据
     *
     * @param apiId
     * @return
     */
    public ApiConfigDTO getAppIdByApiId(long apiId) {
        return ApiConfigMap.INSTANCES.poToDto(this.getById(apiId));

    }

//    public static void main(String[] args) {
//        JsonUtils jsonUtils = new JsonUtils();
//        // 测试时间
////        Instant inst1 = Instant.now();
//        String s = StringEscapeUtils.unescapeJava(jsonUtils.JSONSTR);
//        JSONObject json = JSON.parseObject(s);
//        System.out.println("json = " + json);
//
//        // 封装数据库存储的数据结构
//        List<ApiTableDTO> apiTableDtoList = jsonUtils.getApiTableDtoList01();
////        apiTableDtoList.forEach(System.out::println);
////        int a = 1 / 0;
//
//        List<String> tableNameList = apiTableDtoList.stream().map(tableDTO -> tableDTO.tableName).collect(Collectors.toList());
//        // 获取目标表
//        List<JsonTableData> targetTable = jsonUtils.getTargetTable(tableNameList);
//        targetTable.forEach(System.out::println);
//        // 获取Json的schema信息
//        List<JsonSchema> schemas = jsonUtils.getJsonSchema(apiTableDtoList);
//        schemas.forEach(System.out::println);
////        System.out.println("====================");
//        try {
//            // json根节点处理
//            jsonUtils.rootNodeHandler(schemas, json, targetTable);
//            targetTable.forEach(System.out::println);
//
//            System.out.println("开始执行sql");
//            PgsqlUtils pgsqlUtils = new PgsqlUtils();
//            // ods_abbreviationName_tableName
//            pgsqlUtils.executeBatchPgsql("stg_push_", targetTable);
//
////            Instant inst2 = Instant.now();
////            System.out.println("Difference in 纳秒 : " + Duration.between(inst1, inst2).getNano());
////            System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
//        } catch (Exception e) {
//            System.out.println("执行失败");
//        }
//    }
}