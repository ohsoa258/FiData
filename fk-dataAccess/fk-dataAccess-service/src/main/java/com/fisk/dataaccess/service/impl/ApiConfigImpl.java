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
import com.fisk.common.pdf.exception.PDFException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.dto.api.ApiConfigDTO;
import com.fisk.dataaccess.dto.api.GenerateApiDTO;
import com.fisk.dataaccess.dto.api.GenerateDocDTO;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.dto.json.ApiTableDTO;
import com.fisk.dataaccess.dto.json.JsonSchema;
import com.fisk.dataaccess.dto.json.JsonTableData;
import com.fisk.dataaccess.entity.ApiConfigPO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.map.ApiConfigMap;
import com.fisk.dataaccess.mapper.ApiConfigMapper;
import com.fisk.dataaccess.mapper.TableAccessMapper;
import com.fisk.dataaccess.service.IApiConfig;
import com.fisk.dataaccess.utils.json.JsonUtils;
import com.fisk.dataaccess.utils.sql.PgsqlUtils;
import com.fisk.dataservice.dto.api.doc.ApiBasicInfoDTO;
import com.fisk.dataservice.dto.api.doc.ApiDocDTO;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
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

    @Override
    public ResultEnum generateDoc(GenerateDocDTO dto, HttpServletResponse response) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // api信息转换为文档实体
        ApiDocDTO docDTO = createDocDTO();
        // 生成pdf,返回文件名称
        PDFHeaderFooter headerFooter = new PDFHeaderFooter();
        PDFKit kit = new PDFKit();
        kit.setHeaderFooterBuilder(headerFooter);
        // 系统时间戳
        long timeMillis = System.currentTimeMillis();
        String fileName = "APIServiceDoc" + timeMillis + ".pdf";
        OutputStream outputStream = kit.exportToResponse("apiserviceTemplate.ftl",
                templatePath, fileName, "菲斯科FiData接口文档", docDTO, response);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new PDFException("createDoc fail", ex);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum pushData(ReceiveDataDTO dto) {
        try {
            if (dto.apiId == null) {
                return ResultEnum.PUSH_TABLEID_NULL;
            }
            // 根据api_id查询所有物理表
            List<TableAccessPO> accessPOList = tableAccessImpl.query().eq("api_id", dto.apiId).list();
            // 获取所有表数据
            List<ApiTableDTO> apiTableDtoList = getApiTableDtoList(accessPOList);
            apiTableDtoList.forEach(System.out::println);

            AppRegistrationPO modelApp = appRegistrationImpl.query().eq("id", accessPOList.get(0).appId).one();
            // 防止\未被解析
            String jsonStr = StringEscapeUtils.unescapeJava(dto.pushData);
            // 将数据同步到pgsql
            pushPgSQL(jsonStr, apiTableDtoList, "stg_" + modelApp.appAbbreviation + "_");
        } catch (Exception e) {
            return ResultEnum.PUSH_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEntity<String> getToken(UserAuthDTO dto) {

        // 根据账号名称查询对应的app_id下
        AppDataSourcePO dataSourcePO = appDataSourceImpl.query().eq("realtime_account", dto.getUserAccount()).one();
        if (!dataSourcePO.realtimeAccount.equals(dto.getUserAccount()) || !dataSourcePO.realtimePwd.equals(dto.getPassword())) {
            return ResultEntityBuild.build(ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR, ResultEnum.REALTIME_ACCOUNT_OR_PWD_ERROR.getMsg());
        }
        dto.setTemporaryId(RedisTokenKey.DATA_ACCESS_TOKEN + dataSourcePO.id);

        ResultEntity<String> result = authClient.getToken(dto);
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            return result;
        } else {
            log.error("远程调用失败,方法名: 【auth-service:getToken】");
            return ResultEntityBuild.build(ResultEnum.GET_TOKEN_ERROR);
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
            pgsqlUtils.executeBatchPgsql("", targetTable);

//            Instant inst2 = Instant.now();
//            System.out.println("Difference in 纳秒 : " + Duration.between(inst1, inst2).getNano());
//            System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
        } catch (Exception e) {
            System.out.println("执行失败");
        }
    }

    /*
     * @description 获取父子级关系
     * @author Lock
     * @date 2022/1/18 10:05
     * @version v1.0
     * @params dto
     * @params dtoLost
     * @return com.fisk.dataaccess.dto.api.GenerateApiDTO
     */
    private GenerateApiDTO bulidChildTree(GenerateApiDTO dto, List<TableAccessNonDTO> dtoLost) {
        List<GenerateApiDTO> list = new ArrayList<>();

        for (TableAccessNonDTO e : dtoLost) {
//            if (dto.tableIdentity.equals(e.pid)) {
//
//                GenerateApiDTO generateApiDTO = new GenerateApiDTO();
//                generateApiDTO.tableIdentity = e.id;
//                generateApiDTO.fieldList = getFieldList(e);
//                list.add(generateApiDTO);
//            }
        }

        dto.data = list;
        return dto;
    }

    private List<String> getFieldList(TableAccessNonDTO dto) {
        List<String> fieldList = null;
        List<TableFieldsDTO> list = dto.list;
        if (!CollectionUtils.isEmpty(list)) {
            fieldList = list.stream().map(e -> e.fieldName).collect(Collectors.toList());
        }
        return fieldList;
    }

    private ApiDocDTO createDocDTO() {

        ApiDocDTO apiDocDTO = JSON.parseObject(ApiConstants.DATAACCESS_APIBASICINFO, ApiDocDTO.class);
        apiDocDTO.apiBasicInfoDTOS.get(0).apiRequestExamples = "{\n" +
                "&nbsp;&nbsp; \"useraccount\": \"xxx\",\n" +
                "&nbsp;&nbsp; \"password\": \"xxx\"\n" +
                "}";
        apiDocDTO.apiBasicInfoDTOS.get(0).apiResponseExamples = String.format("{\n" +
                "&nbsp;&nbsp; \"code\": 200,\n" +
                "&nbsp;&nbsp; \"token\": \"xxx\", --%s\n" +
                "&nbsp;&nbsp; \"msg\": \"xxx\"\n" +
                "}", "2.4.9");
        BigDecimal catalogueIndex = new BigDecimal("2.4");
        List<ApiBasicInfoDTO> apiBasicInfoDTOS = new ArrayList<>();

        apiDocDTO.apiBasicInfoDTOS.addAll(apiBasicInfoDTOS);
        return apiDocDTO;
    }
}