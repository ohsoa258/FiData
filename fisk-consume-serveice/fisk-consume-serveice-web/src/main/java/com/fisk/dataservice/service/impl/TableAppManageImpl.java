package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
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
import com.fisk.dataservice.dto.tableapi.TableApiAuthRequestDTO;
import com.fisk.dataservice.dto.tableapi.TableApiResultDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.AppTypeEnum;
import com.fisk.dataservice.enums.InterfaceTypeEnum;
import com.fisk.dataservice.map.TableApiAuthRequestMap;
import com.fisk.dataservice.map.TableApiResultMap;
import com.fisk.dataservice.map.TableAppDatasourceMap;
import com.fisk.dataservice.map.TableAppMap;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.TableAppDatasourceMapper;
import com.fisk.dataservice.mapper.TableAppMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.*;
import com.fisk.dataservice.vo.appcount.AppServiceCountVO;
import com.fisk.dataservice.vo.tableservice.TableAppDatasourceVO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildDeleteTableApiServiceDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableAppManageImpl
        extends ServiceImpl<TableAppMapper, TableAppPO>
        implements ITableAppManageService {

    @Resource
    private UserHelper userHelper;

    @Resource
    private GetConfigDTO getConfig;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private TableAppDatasourceMapper tableAppDatasourceMapper;

    @Resource
    private TableAppDatasourceManageImpl tableAppDatasourceManage;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private TableServiceMapper tableServiceMapper;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private TableServiceImpl tableService;
    @Resource
    ITableApiService tableApiService;
    @Resource
    ITableApiResultService tableApiResultService;
    @Resource
    ITableApiAuthRequestService tableApiAuthRequestService;
    @Resource
    ITableApiParameterService tableApiParameterService;
    @Resource
    TableSyncModeImpl tableSyncMode;
    @Resource
    TableFieldImpl tableField;

    @Resource
    DataManageClient dataManageClient;

    @Value("${open-metadata}")
    private Boolean openMetadata;


    @Override
    public List<FilterFieldDTO> getFilterColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_table_app";
        dto.filterSql = FilterSqlConstants.TABLE_APP_REGISTRATION_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<TableAppVO> pageFilter(TableAppQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        TableAppPageDTO data = new TableAppPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        Page<TableAppVO> filter = baseMapper.filter(query.page, data);
        if (filter != null && CollectionUtils.isNotEmpty(filter.getRecords())) {
            List<Integer> tableAppId = filter.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
            QueryWrapper<TableAppDatasourcePO> tableAppDatasourcePOQueryWrapper = new QueryWrapper<>();
            tableAppDatasourcePOQueryWrapper.lambda().
                    eq(TableAppDatasourcePO::getDelFlag, 1)
                    .in(TableAppDatasourcePO::getTableAppId, tableAppId);
            List<TableAppDatasourcePO> tableAppDatasourcePOS = tableAppDatasourceMapper.selectList(tableAppDatasourcePOQueryWrapper);
            List<AppServiceCountVO> tableAppServiceCount = appServiceConfigMapper.getTableAppServiceCount();
            int totalCount = 0;

            for (TableAppVO t : filter.getRecords()) {
                if (CollectionUtils.isNotEmpty(tableAppDatasourcePOS)) {
                    List<TableAppDatasourcePO> tableAppDatasourcePOList = tableAppDatasourcePOS.stream().filter(k -> k.getTableAppId() == t.getId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                        List<TableAppDatasourceVO> tableAppDatasourceVOS = TableAppDatasourceMap.INSTANCES.listPoToVo(tableAppDatasourcePOList);
                        t.setTableAppDatasourceVOS(tableAppDatasourceVOS);
                    }
                }
                LambdaQueryWrapper<TableApiServicePO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TableApiServicePO::getAppId, t.id);
                t.setItemCount(tableApiService.count(queryWrapper));
                LambdaQueryWrapper<TableApiAuthRequestPO> apiAuthQueryWrapper = new LambdaQueryWrapper<>();
                apiAuthQueryWrapper.eq(TableApiAuthRequestPO::getAppId, t.id);
                List<TableApiAuthRequestPO> tableApiAuthRequestPOS = tableApiAuthRequestService.list(apiAuthQueryWrapper);
                LambdaQueryWrapper<TableApiResultPO> apiResultQueryWrapper = new LambdaQueryWrapper<>();
                apiResultQueryWrapper.eq(TableApiResultPO::getAppId, t.id);
                List<TableApiResultPO> apiResultPOS = tableApiResultService.list(apiResultQueryWrapper);
                if (CollectionUtils.isNotEmpty(tableApiAuthRequestPOS)) {
                    t.setApiAuthRequestDTO(TableApiAuthRequestMap.INSTANCES.listPoToDto(tableApiAuthRequestPOS));
                }
                if (CollectionUtils.isNotEmpty(apiResultPOS)) {
                    t.setApiResultDTO(TableApiResultMap.INSTANCES.listPoToDto(apiResultPOS));
                }
                if (CollectionUtils.isNotEmpty(tableAppServiceCount)) {
                    AppServiceCountVO appServiceCountVO = tableAppServiceCount.stream().filter(k -> k.getAppId() == t.getId()).findFirst().orElse(null);
                    if (appServiceCountVO != null) {
                        t.setItemCount(appServiceCountVO.getCount());
                    }
                }
            }
            totalCount += tableApiService.count();
            filter.getRecords().get(0).setTotalCount(totalCount);
        }
        return filter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(TableAppDTO dto) {
        try {
            if (dto == null
                    || StringUtils.isEmpty(dto.getAppName())
                    || CollectionUtils.isEmpty(dto.getTableAppDatasourceDTOS())
                    || StringUtils.isEmpty(dto.appType)) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QueryWrapper<TableAppPO> tableAppPOQueryWrapper = new QueryWrapper<>();
            tableAppPOQueryWrapper.lambda()
                    .eq(TableAppPO::getDelFlag, 1)
                    .eq(TableAppPO::getAppName, dto.getAppName());
            TableAppPO tableAppPO = baseMapper.selectOne(tableAppPOQueryWrapper);
            if (tableAppPO != null) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            List<TableAppDatasourcePO> tableAppDatasourcePOList = null;
            if (CollectionUtils.isNotEmpty(dto.getTableAppDatasourceDTOS())) {
                tableAppDatasourcePOList = TableAppDatasourceMap.INSTANCES.listDtoToPo(dto.getTableAppDatasourceDTOS());
            }
            tableAppPO = TableAppMap.INSTANCES.dtoToPo(dto);
            tableAppPO.setCreateTime(LocalDateTime.now());
            tableAppPO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
            boolean save = save(tableAppPO);
            if (!save) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (dto.appType == AppTypeEnum.API_TYPE) {
                saveApiConfig(dto, tableAppPO.getId());
            }
            int tableAppId = Math.toIntExact(tableAppPO.getId());
            if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                tableAppDatasourcePOList.forEach(t -> {
                    t.setTableAppId(tableAppId);
                });
                tableAppDatasourceManage.saveBatch(tableAppDatasourcePOList);
            }
        } catch (Exception ex) {
            log.error("【addRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    private void saveApiConfig(TableAppDTO dto, long appId) {
        if (dto.getInterfaceType() == InterfaceTypeEnum.REST_API) {
            switch (dto.getAuthenticationType()) {
                case NONE_VALIDATION:
                    break;
                case BASIC_VALIDATION:
                case JWT_VALIDATION:
                case BEARER_TOKEN_VALIDATION:
                case OAUTH_TWO_VALIDATION:
                case API_KEY_VALIDATION:
                    saveValidation(dto, appId);
                    break;
                default:
                    break;
            }
        } else if (dto.getInterfaceType() == InterfaceTypeEnum.WEB_SERVICE) {
            saveValidation(dto, appId);
        }

    }

    private void saveValidation(TableAppDTO dto, long appId) {
        List<TableApiAuthRequestDTO> apiAuthRequestDTO = dto.getApiAuthRequestDTO();
        apiAuthRequestDTO = apiAuthRequestDTO.stream().map(i -> {
            i.setAppId((int) appId);
            return i;
        }).collect(Collectors.toList());
        List<TableApiAuthRequestPO> tableApiAuthRequestPOS = TableApiAuthRequestMap.INSTANCES.listDtoToPo(apiAuthRequestDTO);
        if (CollectionUtils.isNotEmpty(apiAuthRequestDTO)){
            tableApiAuthRequestService.saveBatch(tableApiAuthRequestPOS);
        }
        List<TableApiResultDTO> apiResultDTO = dto.getApiResultDTO();
        if (CollectionUtils.isNotEmpty(apiResultDTO)){
            tableApiResultService.saveApiResult(apiResultDTO, appId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(TableAppDTO dto) {
        try {
            if (dto == null || dto.getId() == 0
                    || org.apache.commons.lang.StringUtils.isEmpty(dto.getAppName())
                    || CollectionUtils.isEmpty(dto.getTableAppDatasourceDTOS())
                    || StringUtils.isEmpty(dto.appType)) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(dto.getId());
            if (tableAppPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }

            List<TableAppDatasourcePO> tableAppDatasourcePOList = null;
            if (CollectionUtils.isNotEmpty(dto.getTableAppDatasourceDTOS())) {
                tableAppDatasourcePOList = TableAppDatasourceMap.INSTANCES.listDtoToPo(dto.getTableAppDatasourceDTOS());
            }
            int tableAppId = Math.toIntExact(dto.getId());
            tableAppPO = TableAppMap.INSTANCES.dtoToPo(dto);
            int i = baseMapper.updateById(tableAppPO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (tableAppPO.appType == AppTypeEnum.API_TYPE.getValue()) {
                if (dto.appType == AppTypeEnum.TABLE_TYPE) {
                    deleteApiConfig(dto, tableAppPO.getId());
                    QueryWrapper<AppServiceConfigPO> appServiceConfigPoQueryWrapper = new QueryWrapper<>();
                    appServiceConfigPoQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                            .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                            .eq(AppServiceConfigPO::getAppId, tableAppPO.getId())
                            .eq(AppServiceConfigPO::getType, 2);
                    List<AppServiceConfigPO> appServiceConfigPos = appServiceConfigMapper.selectList(appServiceConfigPoQueryWrapper);
                    List<Long> tableServiceIdList = null;
                    if (CollectionUtils.isNotEmpty(appServiceConfigPos)) {
                        tableServiceIdList = appServiceConfigPos.stream().map(t -> Long.parseLong(String.valueOf(t.getServiceId()))).collect(Collectors.toList());
                        // 根据表ID查询表配置详情
                        QueryWrapper<TableServicePO> tableServicePoQueryWrapper = new QueryWrapper<>();
                        tableServicePoQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                                .in(TableServicePO::getId, tableServiceIdList);
                        List<TableServicePO> tableServicePos = tableServiceMapper.selectList(tableServicePoQueryWrapper);
                        if (CollectionUtils.isNotEmpty(tableServiceIdList)) {
                            tableServiceIdList = tableServicePos.stream().map(t -> t.getId()).collect(Collectors.toList());
                            BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
                            buildDeleteTableService.appId = String.valueOf(tableAppPO.getId());
                            buildDeleteTableService.ids = tableServiceIdList;
                            buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
                            buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
                            buildDeleteTableService.delBusiness = true;
                            publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
                        }
                    }
                } else {
                    editApiConfig(dto, tableAppPO.getId());
                }
            } else {
                if (dto.appType == AppTypeEnum.API_TYPE) {
                    saveValidation(dto, tableAppPO.getId());
                    QueryWrapper<AppServiceConfigPO> appServiceConfigPoQueryWrapper = new QueryWrapper<>();
                    appServiceConfigPoQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                            .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                            .eq(AppServiceConfigPO::getAppId, tableAppPO.getId())
                            .eq(AppServiceConfigPO::getType, 2);
                    List<AppServiceConfigPO> appServiceConfigPos = appServiceConfigMapper.selectList(appServiceConfigPoQueryWrapper);
                    List<Long> tableServiceIdList = null;
                    if (CollectionUtils.isNotEmpty(appServiceConfigPos)) {
                        tableServiceIdList = appServiceConfigPos.stream().map(t -> Long.parseLong(String.valueOf(t.getServiceId()))).collect(Collectors.toList());
                        // 根据表ID查询表配置详情
                        QueryWrapper<TableServicePO> tableServicePoQueryWrapper = new QueryWrapper<>();
                        tableServicePoQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                                .in(TableServicePO::getId, tableServiceIdList);
                        List<TableServicePO> tableServicePos = tableServiceMapper.selectList(tableServicePoQueryWrapper);
                        if (CollectionUtils.isNotEmpty(tableServiceIdList)) {
                            tableServiceIdList = tableServicePos.stream().map(t -> t.getId()).collect(Collectors.toList());
                            BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
                            buildDeleteTableService.appId = String.valueOf(tableAppPO.getId());
                            buildDeleteTableService.ids = tableServiceIdList;
                            buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
                            buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
                            buildDeleteTableService.delBusiness = true;
                            publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                tableAppDatasourcePOList.forEach(t -> {
                    t.setTableAppId(tableAppId);
                });
                tableAppDatasourceMapper.updateByTableAppId(tableAppId);
                tableAppDatasourceManage.saveBatch(tableAppDatasourcePOList);
            }
        } catch (Exception ex) {
            log.error("【editRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    private void editApiConfig(TableAppDTO dto, long appId) {
        if (dto.getInterfaceType() == InterfaceTypeEnum.REST_API) {
            switch (dto.getAuthenticationType()) {
                case NONE_VALIDATION:
                    break;
                case BASIC_VALIDATION:
                case JWT_VALIDATION:
                case BEARER_TOKEN_VALIDATION:
                case OAUTH_TWO_VALIDATION:
                case API_KEY_VALIDATION:
                    deleteValidation(appId);
                    saveValidation(dto, appId);
                    break;
                default:
                    break;
            }
        } else if (dto.getInterfaceType() == InterfaceTypeEnum.WEB_SERVICE) {
            deleteValidation(appId);
            saveValidation(dto, appId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        try {
            if (id == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(id);
            if (tableAppPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            int i = baseMapper.deleteByIdWithFill(tableAppPO);
            if (i <= 0) {
                return ResultEnum.DELETE_ERROR;
            }
            // 查询应用下的表ID
            QueryWrapper<AppServiceConfigPO> appServiceConfigPoQueryWrapper = new QueryWrapper<>();
            appServiceConfigPoQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                    .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                    .eq(AppServiceConfigPO::getAppId, id)
                    .eq(AppServiceConfigPO::getType, 2);
            if (tableAppPO.getAppType() == AppTypeEnum.TABLE_TYPE.getValue()) {
                List<AppServiceConfigPO> appServiceConfigPos = appServiceConfigMapper.selectList(appServiceConfigPoQueryWrapper);
                List<Long> tableServiceIdList = null;

                if (CollectionUtils.isNotEmpty(appServiceConfigPos)) {
                    tableServiceIdList = appServiceConfigPos.stream().map(t -> Long.parseLong(String.valueOf(t.getServiceId()))).collect(Collectors.toList());
                    List<Long> configIds = appServiceConfigPos.stream().map(BasePO::getId).collect(Collectors.toList());
                    appServiceConfigMapper.deleteBatchIds(configIds);
                    // 根据表ID查询表配置详情
                    QueryWrapper<TableServicePO> tableServicePoQueryWrapper = new QueryWrapper<>();
                    tableServicePoQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                            .in(TableServicePO::getId, tableServiceIdList);
                    List<TableServicePO> tableServicePos = tableServiceMapper.selectList(tableServicePoQueryWrapper);
                    tableService.remove(tableServicePoQueryWrapper);
                    //删除app数据源配置信息
                    LambdaQueryWrapper<TableAppDatasourcePO> datasourceQueryWrapper = new LambdaQueryWrapper<>();
                    datasourceQueryWrapper.eq(TableAppDatasourcePO::getTableAppId, id);
                    tableAppDatasourceManage.remove(datasourceQueryWrapper);
                    //删除表字段
                    LambdaQueryWrapper<TableFieldPO> filedQueryWrapper = new LambdaQueryWrapper<>();
                    filedQueryWrapper.in(TableFieldPO::getTableServiceId, tableServiceIdList);
                    tableField.remove(filedQueryWrapper);
                    //删除表配置数据
                    LambdaQueryWrapper<TableSyncModePO> syncQueryWrapper = new LambdaQueryWrapper<>();
                    syncQueryWrapper.eq(TableSyncModePO::getType, 4);
                    syncQueryWrapper.in(TableSyncModePO::getTypeTableId, tableServiceIdList);
                    tableSyncMode.remove(syncQueryWrapper);
                    if (CollectionUtils.isNotEmpty(tableServiceIdList)) {
                        tableServiceIdList = tableServicePos.stream().map(BasePO::getId).collect(Collectors.toList());
                        BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
                        buildDeleteTableService.appId = String.valueOf(id);
                        buildDeleteTableService.ids = tableServiceIdList;
                        buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
                        buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
                        buildDeleteTableService.delBusiness = true;
                        publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
                    }
                }
            } else if (tableAppPO.getAppType() == AppTypeEnum.API_TYPE.getValue()) {
                //删除所有app关联数据
                LambdaQueryWrapper<TableApiServicePO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TableApiServicePO::getAppId, id);
                List<TableApiServicePO> tableApiServicePOS = tableApiService.list(queryWrapper);
                List<Long> apiIds = tableApiServicePOS.stream().map(TableApiServicePO::getId).collect(Collectors.toList());
                //删除api数据
                tableApiService.remove(queryWrapper);
                //删除app数据源配置信息
                LambdaQueryWrapper<TableAppDatasourcePO> datasourceQueryWrapper = new LambdaQueryWrapper<>();
                datasourceQueryWrapper.eq(TableAppDatasourcePO::getTableAppId, id);
                tableAppDatasourceManage.remove(datasourceQueryWrapper);
                //删除api认证请求配置返回数据
                LambdaQueryWrapper<TableApiResultPO> resultQueryWrapper = new LambdaQueryWrapper<>();
                resultQueryWrapper.eq(TableApiResultPO::getAppId, id);
                tableApiResultService.remove(resultQueryWrapper);
                //删除api认证请求配置数据
                LambdaQueryWrapper<TableApiAuthRequestPO> authQueryWrapper = new LambdaQueryWrapper<>();
                authQueryWrapper.eq(TableApiAuthRequestPO::getAppId, id);
                tableApiAuthRequestService.remove(authQueryWrapper);
                if (CollectionUtils.isNotEmpty(apiIds)){
                    //删除api配置字段数据
                    LambdaQueryWrapper<TableApiParameterPO> apiParameterQueryWrapper = new LambdaQueryWrapper<>();
                    apiParameterQueryWrapper.in(TableApiParameterPO::getApiId, apiIds);
                    tableApiParameterService.remove(apiParameterQueryWrapper);
                    //删除api配置数据
                    LambdaQueryWrapper<TableSyncModePO> syncQueryWrapper = new LambdaQueryWrapper<>();
                    syncQueryWrapper.eq(TableSyncModePO::getType, 4);
                    syncQueryWrapper.in(TableSyncModePO::getTypeTableId, apiIds);
                    tableSyncMode.remove(syncQueryWrapper);
                }
                List<Long> tableApiIdList = tableApiServicePOS.stream().map(BasePO::getId).collect(Collectors.toList());
                BuildDeleteTableApiServiceDTO buildDeleteTableApiServiceDTO = new BuildDeleteTableApiServiceDTO();
                buildDeleteTableApiServiceDTO.ids = tableApiIdList;
                buildDeleteTableApiServiceDTO.appId = String.valueOf(id);
                buildDeleteTableApiServiceDTO.olapTableEnum = OlapTableEnum.DATA_SERVICE_API;
                buildDeleteTableApiServiceDTO.userId = userHelper.getLoginUserInfo().id;
                buildDeleteTableApiServiceDTO.delBusiness = true;
                publishTaskClient.publishBuildDeleteDataServiceApi(buildDeleteTableApiServiceDTO);
            }
            //同步元数据业务分类
            if (openMetadata){
                ClassificationInfoDTO classificationInfoDTO=new ClassificationInfoDTO();
                classificationInfoDTO.setName(tableAppPO.getAppName());
                classificationInfoDTO.setDescription(tableAppPO.getAppDesc());
                classificationInfoDTO.setSourceType(ClassificationTypeEnum.DATA_DISTRIBUTION);
                classificationInfoDTO.setDelete(true);
                dataManageClient.appSynchronousClassification(classificationInfoDTO);
            }
        } catch (Exception ex) {
            log.error("【deleteRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }

        return ResultEnum.SUCCESS;
    }

    public void deleteApiConfig(TableAppDTO dto, long appId) {
        if (dto.getInterfaceType() == InterfaceTypeEnum.REST_API) {
            switch (dto.getAuthenticationType()) {
                case NONE_VALIDATION:
                    break;
                case BASIC_VALIDATION:
                case JWT_VALIDATION:
                case BEARER_TOKEN_VALIDATION:
                case OAUTH_TWO_VALIDATION:
                case API_KEY_VALIDATION:
                    deleteValidation(appId);
                    break;
                default:
                    break;
            }
        } else if (dto.getInterfaceType() == InterfaceTypeEnum.WEB_SERVICE) {
            deleteValidation(appId);
        }

    }

    private void deleteValidation(long appId) {
        LambdaQueryWrapper<TableApiAuthRequestPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiAuthRequestPO::getAppId, appId);
        tableApiAuthRequestService.remove(queryWrapper);
        LambdaQueryWrapper<TableApiResultPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TableApiResultPO::getAppId, appId);
        tableApiResultService.remove(lambdaQueryWrapper);
    }

    @Override
    public ResultEnum checkDataSourceIsNoUse(TableAppDatasourceDTO dto) {
        try {
            if (dto == null) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(dto.getTableAppId());
            if (tableAppPO == null) {
                return ResultEnum.DS_DATA_STATUS_HAS_CHANGED;
            }
            // 查询应用下的表ID
            QueryWrapper<AppServiceConfigPO> appServiceConfigPOQueryWrapper = new QueryWrapper<>();
            appServiceConfigPOQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                    .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                    .eq(AppServiceConfigPO::getAppId, dto.getTableAppId())
                    .eq(AppServiceConfigPO::getType, 2);
            List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(appServiceConfigPOQueryWrapper);
            if (CollectionUtils.isEmpty(appServiceConfigPOS)) {
                return ResultEnum.SUCCESS;
            }
            List<Integer> tableServiceIdList = appServiceConfigPOS.stream().map(t -> t.getServiceId()).collect(Collectors.toList());
            // 根据表ID查询表配置详情
            QueryWrapper<TableServicePO> tableServicePOQueryWrapper = new QueryWrapper<>();
            if (dto.getDatasourceType() == 1) {
                tableServicePOQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                        .eq(TableServicePO::getSourceDbId, dto.getDatasourceId())
                        .in(TableServicePO::getId, tableServiceIdList);
            } else {
                tableServicePOQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                        .eq(TableServicePO::getTargetDbId, dto.getDatasourceId())
                        .in(TableServicePO::getId, tableServiceIdList);
            }
            List<TableServicePO> tableServicePOS = tableServiceMapper.selectList(tableServicePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(tableServicePOS)) {
                return ResultEnum.DS_DATA_SOURCE_APPLIED;
            }
        } catch (Exception ex) {
            log.error("【checkDataSourceIsUse】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }


    /**
     * 获取表同步服务元数据
     *
     * @return
     */
    @Override
    public List<MetaDataEntityDTO> getTableSyncMetaData() {
        //获取所有应用
        List<TableAppPO> allTableAppPO = this.query().list();
        List<MetaDataEntityDTO> metaDataEntityDTOList = new ArrayList<>();
        for (TableAppPO tableAppPO : allTableAppPO) {
            //获取所有已发布的表
            List<TableServicePO> tableServiceInTheAppList = tableServiceMapper.getTableServiceInTheApp((int) tableAppPO.getId());
            //添加应用下的API
            for (TableServicePO tableServicePO : tableServiceInTheAppList) {
                MetaDataEntityDTO metaDataEntityDTO = buildTableServiceMetaData(tableServicePO, tableAppPO);
                metaDataEntityDTOList.add(metaDataEntityDTO);
            }
        }
        return metaDataEntityDTOList;
    }


    /**
     * 组装表同步服务元数据
     * @param tableServicePO
     * @param tableAppPO
     * @return
     */
    public MetaDataEntityDTO buildTableServiceMetaData(TableServicePO tableServicePO,TableAppPO tableAppPO){
        MetaDataEntityDTO metaDataEntityDTO = new MetaDataEntityDTO();
        metaDataEntityDTO.setName(tableServicePO.getTableName());
        metaDataEntityDTO.setDisplayName(tableServicePO.getDisplayName());
        metaDataEntityDTO.setQualifiedName(String.valueOf(tableServicePO.getId()));
        metaDataEntityDTO.setDescription(tableServicePO.getTableDes());
        metaDataEntityDTO.setCreateSql(tableServicePO.getSqlScript());
        metaDataEntityDTO.setDatasourceDbId(tableServicePO.getSourceDbId());
        metaDataEntityDTO.setTargetDbId(tableServicePO.getTargetDbId());
        metaDataEntityDTO.setTableName(tableServicePO.getTargetTable());
        metaDataEntityDTO.setEntityType(14);
        metaDataEntityDTO.setOwner(tableAppPO.getAppPrincipal());
        metaDataEntityDTO.setAppName(tableAppPO.getAppName());
        //获取表下的字段
        LambdaQueryWrapper<TableFieldPO> tableFieldPOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tableFieldPOLambdaQueryWrapper.eq(TableFieldPO::getTableServiceId, tableServicePO.getId());
        List<TableFieldPO> tableFieldPOList = tableField.list(tableFieldPOLambdaQueryWrapper);
        //添加表下的字段
        List<MetaDataColumnAttributeDTO> metaDataColumnAttributeDTOList = new ArrayList<>();
        for (TableFieldPO tableFieldPO : tableFieldPOList) {
            MetaDataColumnAttributeDTO metaDataColumnAttributeDTO = new MetaDataColumnAttributeDTO();
            metaDataColumnAttributeDTO.setName(tableFieldPO.getFieldName());
            metaDataColumnAttributeDTO.setDisplayName(tableFieldPO.getDisplayName());
            metaDataColumnAttributeDTO.setDescription(tableFieldPO.getFieldDes());
            metaDataColumnAttributeDTO.setDataType(tableFieldPO.getFieldType());
            metaDataColumnAttributeDTO.setLength(tableFieldPO.getFieldType());
            metaDataColumnAttributeDTO.setQualifiedName(String.valueOf(tableFieldPO.getId()));
            metaDataColumnAttributeDTO.setOwner(tableAppPO.getAppPrincipal());
            metaDataColumnAttributeDTOList.add(metaDataColumnAttributeDTO);
        }
        metaDataEntityDTO.setAttributeDTOList(metaDataColumnAttributeDTOList);
        return metaDataEntityDTO;
    }

    /**
     * 根据表服务ID获取元数据信息
     * @param id
     * @return
     */
    @Override
    public List<MetaDataEntityDTO> getTableSyncMetaDataById(Long id) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        return getTableSyncMetaDataByIds(ids);
    }


    /**
     * 根据表服务器ID集合获取元数据信息
     * @param ids
     * @return
     */
    @Override
    public List<MetaDataEntityDTO> getTableSyncMetaDataByIds(List<Long> ids) {

        List<MetaDataEntityDTO> metaDataEntityDTOList = new ArrayList<>();
        //获取所有已发布的表
        List<TableServicePO> tableServiceInTheAppList =tableService.query().in("id",ids).list();
        //添加应用下的API
        for (TableServicePO tableServicePO : tableServiceInTheAppList) {
            TableAppPO tableAppPO = tableServiceMapper.getAppByTableService((int) tableServicePO.getId()).stream().findFirst().orElse(null);
            MetaDataEntityDTO metaDataEntityDTO = buildTableServiceMetaData(tableServicePO, tableAppPO);
            metaDataEntityDTOList.add(metaDataEntityDTO);
        }
        return metaDataEntityDTOList;
    }


    @Override
    public List<AppBusinessInfoDTO> getTableService() {
        //封装三个服务的所有应用
        List<AppBusinessInfoDTO> appInfos=new ArrayList<>();
        List<TableAppPO> tableAppPOS = this.query().list();
        //封装Table服务的所有应用
        tableAppPOS.stream()
                .forEach(a -> {
                    AppBusinessInfoDTO infoDTO = new AppBusinessInfoDTO(a.getId(),a.getAppName(),a.getAppPrincipal(),a.getAppDesc(),4);
                    appInfos.add(infoDTO);
                });

        return appInfos;
    }
}
