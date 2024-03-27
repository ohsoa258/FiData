package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.dataservice.BuildDataServiceHelper;
import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.dto.appserviceconfig.AppTableServiceConfigDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.*;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.ApiEncryptConfigService;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.vo.api.*;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.dataservice.vo.fileservice.FileServiceVO;
import com.fisk.dataservice.vo.tableapi.ConsumeServerVO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;
import com.fisk.dataservice.vo.tableservice.TableServiceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * api接口实现类
 *
 * @author dick
 */
@Service
@Slf4j
public class ApiRegisterManageImpl extends ServiceImpl<ApiRegisterMapper, ApiConfigPO> implements IApiRegisterManageService {

    @Resource
    private ApiFieldMapper apiFieldMapper;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private ApiFilterConditionMapper apiFilterConditionMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiFieldManageImpl apiFieldManageImpl;

    @Resource
    private ApiFilterConditionManageImpl apiFilterConditionManageImpl;

    @Resource
    private ApiParmManageImpl apiParmManageImpl;

    @Resource
    private TableSyncModeImpl tableSyncModeImpl;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private TableServiceMapper tableServiceMapper;

    @Resource
    private FileServiceMapper fileServiceMapper;

    @Resource
    private AppRegisterManageImpl appRegisterManage;

    @Resource
    UserHelper userHelper;

    @Resource
    private UserClient userClient;

    @Resource
    private DataManageClient dataManageClient;

    @Value("${dataservice.proxyservice.api_address}")
    private String proxyServiceApiAddress;

    @Value("${open-metadata}")
    private Boolean openMetadata;

    @Resource
    private ApiMenuConfigServiceImpl apiMenuConfigService;

    @Resource
    private ApiEncryptConfigService apiEncryptConfigService;

    @Override
    public Page<ApiConfigVO> getAll(ApiRegisterQueryDTO query) {
        List<ApiMenuConfigPO> list = apiMenuConfigService.list();
        List<Integer> allMenuId = findAllChildrenIdsWithParent(list, query.menuId);
        allMenuId.add(query.menuId);
        List<String> ids = allMenuId.stream().map(String::valueOf).collect(Collectors.toList());
        query.setMenuIds(ids);
        Page<ApiConfigVO> all = baseMapper.getAll(query.page, query);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Long> userIds = all.getRecords().stream()
                    .filter(x -> StringUtils.isNotEmpty(x.createUser))
                    .map(x -> Long.valueOf(x.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                all.getRecords().forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                    e.setApiProxyCallUrl(proxyServiceApiAddress + "/" + e.getApiCode());
                });
            }
        }
        return all;
    }

    // 递归查询所有子 ID（包含父 ID）
    public List<Integer> findAllChildrenIdsWithParent(List<ApiMenuConfigPO> list, Integer parentId) {
        List<Integer> childrenIds = new ArrayList<>();
        List<ApiMenuConfigPO> children = list.stream()
                .filter(item -> item.getPid().equals(parentId))
                .collect(Collectors.toList());

        for (ApiMenuConfigPO child : children) {
            childrenIds.add((int)child.getId());
            childrenIds.addAll(findAllChildrenIdsWithParent(list, (int)child.getId()));
        }
        return childrenIds;
    }

    @Override
    public PageDTO<ApiSubVO> getApiSubAll(ApiSubQueryDTO dto) {
        PageDTO<ApiSubVO> pageDTO = new PageDTO<>();

        List<ApiSubVO> apiSubVOS = new ArrayList<>();
        Integer createApiType = dto.getAppType() == 2 ? 3 : dto.getAppType();
        List<ApiConfigPO> apiConfigPOS = baseMapper.getList(dto.getKeyword(), createApiType);
        if (CollectionUtils.isNotEmpty(apiConfigPOS)) {
            apiSubVOS = ApiRegisterMap.INSTANCES.poToApiSubVO(apiConfigPOS);
            List<AppServiceConfigPO> subscribeListByAppId = appServiceConfigMapper.getSubscribeListByAppId(dto.appId);
            if (CollectionUtils.isNotEmpty(subscribeListByAppId)) {
                apiSubVOS.forEach(e -> {
                    subscribeListByAppId
                            .stream()
                            .filter(item -> item.getServiceId() == e.getId()
                                    && item.getType() == AppServiceTypeEnum.API.getValue())
                            .findFirst()
                            .ifPresent(user -> e.apiSubState = 1);
                });
            }
            pageDTO.setTotal((long) apiSubVOS.size());
            dto.current = dto.current - 1;
            apiSubVOS = apiSubVOS.stream().sorted(Comparator.comparing(ApiSubVO::getApiSubState).reversed()).skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
            List<Long> userIds = apiSubVOS.stream()
                    .filter(x -> StringUtils.isNotEmpty(x.createUser))
                    .map(x -> Long.valueOf(x.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                apiSubVOS.forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                });
            }
        }
        pageDTO.setItems(apiSubVOS);
        return pageDTO;
    }

    @Override
    public PageDTO<TableServiceVO> getTableServiceSubAll(ApiSubQueryDTO dto) {
        QueryWrapper<TableServicePO> tableServicePOQueryWrapper = new QueryWrapper<>();
        tableServicePOQueryWrapper.orderByDesc("create_time");
        if (!StringUtils.isEmpty(dto.keyword)) {
            tableServicePOQueryWrapper
                    .like("table_name", dto.keyword)
                    .or()
                    .like("display_name", dto.keyword);
        }
        List<TableServicePO> poList = tableServiceMapper.selectList(tableServicePOQueryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return new PageDTO<>();
        }

        List<TableServiceVO> list = new ArrayList<>();

        PageDTO<TableServiceVO> pageDTO = new PageDTO<>();

        //获取已订阅的数据
        QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("service_id")
                .lambda()
                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.TABLE.getValue());
        List<AppServiceConfigPO> subscribePo = appServiceConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(subscribePo)) {
            List<Long> subscribeList = subscribePo.stream().map(e -> Long.valueOf(e.serviceId)).collect(Collectors.toList());
            List<TableServicePO> collect = poList
                    .stream()
                    .filter(e -> subscribeList.contains(e.id))
                    .sorted(Comparator.comparing(TableServicePO::getCreateTime))
                    .collect(Collectors.toList());

            list.addAll(TableServiceMap.INSTANCES.poListToVoList(collect));
            list.stream().map(e -> e.tableServiceSubState = 1).collect(Collectors.toList());

            //反转
            Collections.reverse(list);

            //未订阅的数据
            List<TableServicePO> collect1 = poList
                    .stream()
                    .filter(e -> !subscribeList.contains(e.id))
                    .sorted(Comparator.comparing(TableServicePO::getCreateTime))
                    .collect(Collectors.toList());
            List<TableServiceVO> list1 = TableServiceMap.INSTANCES.poListToVoList(collect1);
            list1.stream().map(e -> e.tableServiceSubState = 0).collect(Collectors.toList());

            //反转
            Collections.reverse(list1);

            list.addAll(list1);

        } else {
            list = TableServiceMap.INSTANCES.poListToVoList(poList);
            list.stream().map(e -> e.tableServiceSubState = 0).collect(Collectors.toList());
        }

        pageDTO.setTotal(Long.valueOf(list.size()));
        pageDTO.setItems(list.stream().skip((dto.current - 1) * dto.size).limit(dto.size).collect(Collectors.toList()));

        return pageDTO;
    }

    @Override
    public PageDTO<FileServiceVO> getFileServiceSubAll(ApiSubQueryDTO dto) {

        PageDTO<FileServiceVO> pageDTO = new PageDTO<>();

        QueryWrapper<FileServicePO> fileServicePOQueryWrapper = new QueryWrapper<>();
        fileServicePOQueryWrapper.orderByDesc("create_time");
        if (!StringUtils.isEmpty(dto.keyword)) {
            fileServicePOQueryWrapper
                    .like("table_name", dto.keyword)
                    .or()
                    .like("display_name", dto.keyword);
        }
        List<FileServicePO> poList = fileServiceMapper.selectList(fileServicePOQueryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            pageDTO.setItems(new ArrayList<>());
            return pageDTO;
        }

        List<FileServiceVO> list = new ArrayList<>();

        //获取已订阅的数据
        QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("service_id")
                .lambda()
                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.FILE.getValue());
        List<AppServiceConfigPO> subscribePo = appServiceConfigMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(subscribePo)) {
            List<Long> subscribeList = subscribePo.stream().map(e -> Long.valueOf(e.serviceId)).collect(Collectors.toList());
            List<FileServicePO> collect = poList
                    .stream()
                    .filter(e -> subscribeList.contains(e.id))
                    .sorted(Comparator.comparing(FileServicePO::getCreateTime))
                    .collect(Collectors.toList());

            list.addAll(FileServiceMap.INSTANCES.poListToVoList(collect));
            list.stream().map(e -> e.fileServiceSubState = 1).collect(Collectors.toList());

            //反转
            Collections.reverse(list);

            //未订阅的数据
            List<FileServicePO> collect1 = poList
                    .stream()
                    .filter(e -> !subscribeList.contains(e.id))
                    .sorted(Comparator.comparing(FileServicePO::getCreateTime))
                    .collect(Collectors.toList());
            List<FileServiceVO> list1 = FileServiceMap.INSTANCES.poListToVoList(collect1);
            list1.stream().map(e -> e.fileServiceSubState = 0).collect(Collectors.toList());

            //反转
            Collections.reverse(list1);

            list.addAll(list1);

        } else {
            list = FileServiceMap.INSTANCES.poListToVoList(poList);
            list.stream().map(e -> e.fileServiceSubState = 0).collect(Collectors.toList());
        }

        pageDTO.setTotal(Long.valueOf(list.size()));
        pageDTO.setItems(list.stream().skip((dto.current - 1) * dto.size).limit(dto.size).collect(Collectors.toList()));

        return pageDTO;
    }

    @Override
    public ResultEnum appTableServiceConfig(List<AppTableServiceConfigDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.SUCCESS;
        }

        delAppServiceConfig(dtoList.get(0).appId, AppServiceTypeEnum.TABLE.getValue());

        List<AppServiceConfigPO> poList = new ArrayList<>();

        for (AppTableServiceConfigDTO item : dtoList) {
            AppServiceConfigPO po = new AppServiceConfigPO();
            po.type = AppServiceTypeEnum.TABLE.getValue();
            po.serviceId = item.serviceId;
            po.appId = item.appId;

            poList.add(po);
        }

        batchAddAppServiceConfig(poList);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum importantOrUnimportant(Integer id) {
        ApiConfigPO apiConfigPO = this.getById(id);
        if (apiConfigPO.getImportantInterface() == 0) {
            apiConfigPO.setImportantInterface(1);
        } else if (apiConfigPO.getImportantInterface() == 1) {
            apiConfigPO.setImportantInterface(0);
        }
        if (baseMapper.updateById(apiConfigPO) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ConsumeServerVO getConsumeServer() {
        ConsumeServerVO consumeServerVO = new ConsumeServerVO();
        consumeServerVO.setTotalNumber(this.baseMapper.getTotalNumber());
        consumeServerVO.setFocusApiTotalNumber(this.baseMapper.focusApiTotalNumber());
        consumeServerVO.setApiNumber(this.baseMapper.getApiNumber());
        consumeServerVO.setFrequency(this.baseMapper.getFrequency());
        consumeServerVO.setFaildNumber(this.baseMapper.faildNumber());
        consumeServerVO.setSuccessNumber(this.baseMapper.successNumber());
        return consumeServerVO;
    }

    @Override
    public List<TopFrequencyVO> getTopFrequency() {
        return this.baseMapper.getTopFrequency();
    }

    @Override
    public Long getApiIdByApiName(String apiName) {
        return null;
    }

    public ResultEnum delAppServiceConfig(Integer appId, Integer type) {
        QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AppServiceConfigPO::getAppId, appId)
                .eq(AppServiceConfigPO::getType, type);

        List<AppServiceConfigPO> poList = appServiceConfigMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }

        int flat = appServiceConfigMapper.delete(queryWrapper);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;

    }

    public ResultEnum batchAddAppServiceConfig(List<AppServiceConfigPO> poList) {
        for (AppServiceConfigPO po : poList) {
            int flat = appServiceConfigMapper.insert(po);
            if (flat == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(ApiRegisterDTO dto) {

        boolean isInsert = false;
        int apiId;
        // 第一步：保存api信息
        ApiConfigPO apiConfigPO = ApiRegisterMap.INSTANCES.dtoToPo(dto.apiDTO);
        if (apiConfigPO.getEnableCache() == 1){
            Integer cacheTime = apiConfigPO.getCacheTime();
            if (cacheTime == null || cacheTime < 5 || cacheTime > 300){
                return ResultEnum.DATA_SERVER_CACHE_TIME_ERROR;
            }
        }
        ApiMenuConfigPO apiMenuConfigPO = new ApiMenuConfigPO();
        Integer menuId = dto.apiDTO.getMenuId();
        if (menuId == null){
            return ResultEnum.DS_APISERVICE_MENUID_NOT_EXIST;
        }
        //查询排序添加位置
        LambdaQueryWrapper<ApiMenuConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiMenuConfigPO::getPid,menuId);
        queryWrapper.orderByDesc(ApiMenuConfigPO::getSort);
        queryWrapper.last("LIMIT 1");
        ApiMenuConfigPO tragetMenu = apiMenuConfigService.getOne(queryWrapper);
        if (tragetMenu != null){
            apiMenuConfigPO.setSort(tragetMenu.getSort()+1);
        }else {
            apiMenuConfigPO.setSort(1);
        }
        apiMenuConfigPO.setPid(menuId);
        apiMenuConfigPO.setType(2);
        apiMenuConfigPO.setServerType(dto.apiDTO.createApiType);
        apiMenuConfigPO.setName(apiConfigPO.getApiName());
        apiMenuConfigService.save(apiMenuConfigPO);

        if (apiConfigPO == null)
            return ResultEnum.SAVE_DATA_ERROR;
        if (apiConfigPO.getCreateApiType() != 3) {
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(apiConfigPO.getDatasourceId());
            if (dataSourceConPO == null)
                return ResultEnum.DS_DATASOURCE_NOTEXISTS;
        }
        String apiCode = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        apiConfigPO.setApiCode(apiCode);
        apiConfigPO.setCreateTime(LocalDateTime.now());
        Long userId = userHelper.getLoginUserInfo().getId();
        apiConfigPO.setCreateUser(userId.toString());
        apiConfigPO.setApiMenuId((int)apiMenuConfigPO.getId());
        isInsert = baseMapper.insertOne(apiConfigPO) > 0;
        if (!isInsert)
            return ResultEnum.SAVE_DATA_ERROR;
        apiId = (int) apiConfigPO.getId();
        // API代理只需要保存API的基本信息
        if (apiConfigPO.getCreateApiType() == 3) {
            return ResultEnum.SUCCESS;
        }

        // 第二步：保存字段信息
        List<FieldConfigPO> fieldConfigPOS = ApiFieldMap.INSTANCES.listDtoToPo_Add(dto.fieldDTO);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            fieldConfigPOS.forEach(e -> {
                e.apiId = apiId;
            });
            isInsert = apiFieldManageImpl.saveBatch(fieldConfigPOS);
            if (!isInsert)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        // 第三步：保存过滤条件信息
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            List<FilterConditionConfigPO> filterConditionConfigPOS = ApiFilterConditionMap.INSTANCES.listDtoToPo(dto.whereDTO);
            if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
                filterConditionConfigPOS.forEach(e -> {
                    e.apiId = apiId;
                });
                isInsert = apiFilterConditionManageImpl.saveBatch(filterConditionConfigPOS);
                if (!isInsert)
                    return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // 第四步：保存输入参数信息
        List<ParmConfigPO> paramConfigPOS = ApiParmMap.INSTANCES.listDtoToPo(dto.parmDTO);
        if (CollectionUtils.isNotEmpty(paramConfigPOS)) {
            paramConfigPOS.forEach(e -> {
                e.apiId = apiId;
            });
            isInsert = apiParmManageImpl.saveBatch(paramConfigPOS);
            if (!isInsert)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        //第五步：保存调度(创建现有api类型才有调度)
        if (dto.apiDTO != null && dto.apiDTO.createApiType == 2) {
            dto.syncModeDTO.typeTableId = apiId;
            return tableSyncModeImpl.addApiTableSyncMode(dto.syncModeDTO);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(ApiRegisterEditDTO dto) {
        ApiConfigPO model = baseMapper.selectById(dto.apiDTO.getId());
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }
        if (model.getEnableCache() == 1){
            Integer cacheTime = model.getCacheTime();
            if (cacheTime == null || cacheTime < 5 || cacheTime > 300){
                return ResultEnum.DATA_SERVER_CACHE_TIME_ERROR;
            }
        }
        if (model.getCreateApiType() != 3) {
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(dto.apiDTO.getDatasourceId());
            if (dataSourceConPO == null)
                return ResultEnum.DS_DATASOURCE_NOTEXISTS;
        }

        int apiId;
        boolean isUpdate = false;
        // 第一步：编辑保存api信息
        ApiConfigPO apiConfigPO = ApiRegisterMap.INSTANCES.dtoToPo_Edit(dto.apiDTO);
        if (apiConfigPO == null)
            return ResultEnum.SAVE_DATA_ERROR;
        isUpdate = baseMapper.updateById(apiConfigPO) > 0;
        if (!isUpdate)
            return ResultEnum.SAVE_DATA_ERROR;
        apiId = (int) apiConfigPO.getId();

        // API代理只需要保存API的基本信息
        if (apiConfigPO.getCreateApiType() == 3) {
            return ResultEnum.SUCCESS;
        }

        // 第二步：编辑保存字段信息[数据库可能修改表字段描述，此处直接全量更新]
        apiFieldMapper.updateByApiId(apiId);
        List<FieldConfigPO> fieldConfigPOS = ApiFieldMap.INSTANCES.listDtoToPo_Add(dto.fieldDTO);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            isUpdate = apiFieldManageImpl.saveBatch(fieldConfigPOS);
            if (!isUpdate)
                return ResultEnum.SAVE_DATA_ERROR;
        }

        // 第三步：保存编辑过滤信息
        apiFilterConditionMapper.updateByApiId(apiId);
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            List<FilterConditionConfigPO> filterConditionConfigPOS = ApiFilterConditionMap.INSTANCES.listDtoToPo(dto.whereDTO);
            if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
                isUpdate = apiFilterConditionManageImpl.saveBatch(filterConditionConfigPOS);
                if (!isUpdate)
                    return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        // 第四步：保存编辑参数信息
        /* 因为参数信息又被用作于内置参数，因此在此处不能直接删除
         * 1、删除不存在的参数
         * 2、修改参数
         * 3、新增参数
         * */
        if (CollectionUtils.isNotEmpty(dto.parmDTO)) {
            dto.parmDTO.forEach(t -> {
                t.setApiId(apiId);
            });
        }
        QueryWrapper<ParmConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ParmConfigPO::getApiId, apiId).eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> parmConfigPOS = apiParmMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            parmConfigPOS.forEach(e -> {
                Optional<ParmConfigEditDTO> first = dto.parmDTO.stream().filter(item -> item.getId() == e.id).findFirst();
                if (!first.isPresent()) {
                    apiParmMapper.deleteByIdWithFill(e);
                }
            });
        }
        List<ParmConfigPO> parmConfigPOS1 = ApiParmMap.INSTANCES.listDtoToPo_Edit(dto.parmDTO);
        if (CollectionUtils.isNotEmpty(parmConfigPOS1)) {
            isUpdate = apiParmManageImpl.saveOrUpdateBatch(parmConfigPOS1);
            if (!isUpdate)
                return ResultEnum.SAVE_DATA_ERROR;
        }
        ApiMenuConfigPO apiMenuConfigPO = apiMenuConfigService.getById(apiConfigPO.getApiMenuId());
        apiMenuConfigPO.setName(apiConfigPO.getApiName());
        apiMenuConfigService.updateById(apiMenuConfigPO);
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editApiData(ApiConfigEditDTO dto) {
        ApiConfigPO model = baseMapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }

        if (model.getCreateApiType() != 3) {
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(dto.getDatasourceId());
            if (dataSourceConPO == null)
                return ResultEnum.DS_DATASOURCE_NOTEXISTS;
        }

        boolean isUpdate = false;
        // 第一步：编辑保存api信息部分信息
        model.setApiDesc(dto.getApiDesc());
        model.setExpirationType(dto.getExpirationType());
        model.setExpirationTime(dto.getExpirationTime());
        model.setEnableCache(dto.getEnableCache());
        model.setCacheTime(dto.getCacheTime());
        model.setMaxSizeType(dto.getMaxSizeType());
        model.setMaxSize(dto.getMaxSize());
        isUpdate = baseMapper.updateById(model) > 0;
        if (!isUpdate)
            return ResultEnum.SAVE_DATA_ERROR;
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int apiId) {
        //查询同步元数据
        List<MetaDataEntityDTO> apiMetaDataList = appRegisterManage.getApiMetaDataById((long) apiId);

        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }
        int i = baseMapper.deleteByIdWithFill(model);
        if (i > 0) {
            //同步元数据
            if (openMetadata) {
                dataManageClient.deleteConsumptionMetaData(apiMetaDataList);
            }
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }

    }

    @Override
    public ResultEntity<ApiRegisterDetailVO> detail(int apiId) {
        ApiRegisterDetailVO apiRegisterDetailVO = new ApiRegisterDetailVO();

        // 第一步：查询API信息,selectById仅查询有效的
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_API_EXISTS, apiRegisterDetailVO);
        apiRegisterDetailVO.apiVO = ApiRegisterMap.INSTANCES.poToVo(model);
        if (model.getDatasourceId() != 0) {
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(model.getDatasourceId());
            apiRegisterDetailVO.apiVO.setDatasourceType(dataSourceConPO.getDatasourceType());
        }

        // 第二步：查询字段信息
        QueryWrapper<FieldConfigPO> fieldQueryWrapper = new QueryWrapper<>();
        fieldQueryWrapper.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> fieldConfigPOS = apiFieldMapper.selectList(fieldQueryWrapper);
        if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
            apiRegisterDetailVO.fieldVO = ApiFieldMap.INSTANCES.listPoToVo(fieldConfigPOS);
        }

        // 第三步：查询过滤信息
        QueryWrapper<FilterConditionConfigPO> filterConditionQueryWrapper = new QueryWrapper<>();
        filterConditionQueryWrapper.lambda()
                .eq(FilterConditionConfigPO::getApiId, apiId)
                .eq(FilterConditionConfigPO::getDelFlag, 1);
        List<FilterConditionConfigPO> filterConditionConfigPOS = apiFilterConditionMapper.selectList(filterConditionQueryWrapper);
        if (CollectionUtils.isNotEmpty(filterConditionConfigPOS)) {
            apiRegisterDetailVO.whereVO = ApiFilterConditionMap.INSTANCES.listPoToVo(filterConditionConfigPOS);
        }

        // 第四步：查询参数信息
        QueryWrapper<ParmConfigPO> parmQueryWrapper = new QueryWrapper<>();
        parmQueryWrapper.lambda()
                .eq(ParmConfigPO::getApiId, apiId)
                .eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> parmConfigPOS = apiParmMapper.selectList(parmQueryWrapper);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            apiRegisterDetailVO.parmVO = ApiParmMap.INSTANCES.listPoToVo(parmConfigPOS);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, apiRegisterDetailVO);
    }

    @Override
    public List<FieldConfigVO> getFieldAll(int apiId) {
        List<FieldConfigVO> fieldList = new ArrayList<>();
        QueryWrapper<FieldConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> selectList = apiFieldMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            fieldList = ApiFieldMap.INSTANCES.listPoToVo(selectList);
        }
        return fieldList;
    }

    @Override
    public FieldEncryptConfigDTO getFieldEncryptAll(int apiId) {
        FieldEncryptConfigDTO encryptConfigDTO = new FieldEncryptConfigDTO();

        List<FieldEncryptDTO> fieldList = new ArrayList<>();
        QueryWrapper<FieldConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> selectList = apiFieldMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            encryptConfigDTO.setApiId(apiId);
            fieldList = ApiFieldMap.INSTANCES.listPoToDTO(selectList);
        }
        encryptConfigDTO.setFieldEncryptDTOS(fieldList);

        LambdaQueryWrapper<ApiEncryptConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiEncryptConfigPO::getApiId,apiId);
        ApiEncryptConfigPO apiEncryptConfigPO = apiEncryptConfigService.getOne(queryWrapper);
        if (apiEncryptConfigPO != null){
            encryptConfigDTO.setEncryptKey(apiEncryptConfigPO.getEncryptKey());
        }
        return encryptConfigDTO;
    }

    @Override
    public ResultEnum setField(List<FieldConfigEditDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<FieldConfigPO> fieldList = ApiFieldMap.INSTANCES.listDtoToPo(dto);
        return apiFieldManageImpl.updateBatchById(fieldList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum setFieldEncrypt(FieldEncryptConfigDTO dto) {
        List<FieldEncryptDTO> fieldEncryptDTOS = dto.fieldEncryptDTOS;
        if (dto.apiId == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        if (StringUtils.isEmpty(dto.encryptKey)) {
            dto.encryptKey = generateKey();
        }
        if (CollectionUtils.isEmpty(fieldEncryptDTOS)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<FieldConfigPO> fieldList = ApiFieldMap.INSTANCES.listEncryptDtoToPo(fieldEncryptDTOS);
        boolean saveField = apiFieldManageImpl.updateBatchById(fieldList);

        ApiEncryptConfigPO apiEncryptConfigPO = ApiEncryptConfigMap.INSTANCES.dtoToPo(dto);
        LambdaQueryWrapper<ApiEncryptConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApiEncryptConfigPO::getApiId,dto.apiId);
        ApiEncryptConfigPO configPO = apiEncryptConfigService.getOne(queryWrapper);
        boolean update = false;
        if (configPO != null){
            UpdateWrapper<ApiEncryptConfigPO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("api_id",dto.apiId);
            update = apiEncryptConfigService.update(apiEncryptConfigPO, updateWrapper);
        }else {
            update = apiEncryptConfigService.save(apiEncryptConfigPO);
        }
        if (saveField && update){
            return ResultEnum.SUCCESS;
        }else {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    // 生成密钥的方法
    private static String generateKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        SecretKey secretKey = keyGen.generateKey();
        byte[] keyBytes = secretKey.getEncoded();
        StringBuilder sb = new StringBuilder();
        for (byte b : keyBytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @Override
    public ApiPreviewVO preview(ApiPreviewDTO dto) {
        ApiPreviewVO apiPreviewVO = new ApiPreviewVO();

        // 第一步：验证API配置完整
        if (StringUtils.isEmpty(dto.getApiDTO().getCreateSql())) {
            return apiPreviewVO;
        }
//        if (StringUtils.isEmpty(dto.getApiDTO().getCreateCountSql())) {
//            dto.getApiDTO().setCreateCountSql(dto.getApiDTO().getCreateSql());
//        }

        // 第二步：获取请求参数中的分页信息
        Integer current = null;
        Integer size = null;
        ParmConfigDTO paramConfigDTO_page = null;
        ParmConfigDTO paramConfigDTO_size = null;
        if (CollectionUtils.isNotEmpty(dto.getParmDTO())) {
            paramConfigDTO_page = dto.getParmDTO().stream().filter(t -> t.getParmName().equals("current")).findFirst().orElse(null);
            if (paramConfigDTO_page != null) {
                current = RegexUtils.isNumeric(paramConfigDTO_page.getParmValue());
            }
            paramConfigDTO_size = dto.getParmDTO().stream().filter(t -> t.getParmName().equals("size")).findFirst().orElse(null);
            if (paramConfigDTO_size != null) {
                size = RegexUtils.isNumeric(paramConfigDTO_size.getParmValue());
            }
        }
        if (current == null || current == 0 || size == null || size == 0) {
            // 未设置分页参数，默认查询第一页，查询数字的最大值
            current = 1;
            size = Integer.MAX_VALUE;
            //return ResultEntityBuild.buildData(ResultEnum.DS_DATA_PAGING_PARAMETERS_NOT_SET, responseVO);
        }
        log.info("数据服务【preview】分页参数【current】：" + current);
        log.info("数据服务【preview】分页参数【size】：" + size);

        // 第三步：查询数据源信息
        DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource().stream().filter(t -> t.getId() == dto.apiDTO.getDatasourceId()).findFirst().orElse(null);
        if (dataSourceConVO == null) {
            return apiPreviewVO;
        }

        // 第四步：拼接过滤条件
        String sql = "";
        String countSql = "";
        IBuildDataServiceSqlCommand dbCommand = BuildDataServiceHelper.getDBCommand(dataSourceConVO.getConType());
        if (dto.getApiDTO().getApiType() == ApiTypeEnum.SQL.getValue()) {
            // 移除请求参数中的分页条件
            if (paramConfigDTO_page != null && paramConfigDTO_size != null) {
                dto.getParmDTO().remove(paramConfigDTO_page);
                dto.getParmDTO().remove(paramConfigDTO_size);
            }
            // 获取分页条件
            String fields = dto.getApiDTO().getCreateSql();
            String orderBy = fields.split(",")[0];
            List<SqlParmDto> sqlParamsDto = ApiParmMap.INSTANCES.listDtoToSqlParmDto(dto.getParmDTO());
            String sql_Where = SqlParmUtils.SqlParams(sqlParamsDto, "@");
            if (StringUtils.isNotEmpty(sql_Where)) {
                sql_Where = SqlParmUtils.SqlParams(sqlParamsDto, sql_Where, "@", dataSourceConVO.getConType());
            }
            // 获取分页sql语句
            sql = dbCommand.buildPagingSql(dto.getApiDTO().getTableName(), fields, orderBy, current, size, sql_Where);
            countSql = dbCommand.buildQueryCountSql(dto.getApiDTO().getTableName(), sql_Where);
            log.info("数据服务【preview】普通模式SQL参数【sql】：" + sql);
            log.info("数据服务【preview】普通模式SQL参数【countSql】：" + countSql);
        } else if (dto.getApiDTO().getApiType() == ApiTypeEnum.CUSTOM_SQL.getValue()) {
            List<SqlParmDto> sqlParamsDto = ApiParmMap.INSTANCES.listDtoToSqlParmDto(dto.getParmDTO());
            if (dataSourceConVO.getConType() == DataSourceTypeEnum.DORIS
                    || dataSourceConVO.getConType() ==DataSourceTypeEnum.MYSQL) {
                List<SqlParmDto> pageNo = sqlParamsDto.stream().filter(i -> i.parmName == "@start" || i.parmName == "@end").collect(Collectors.toList());
                if (CollectionUtils.isEmpty(pageNo)) {
                    SqlParmDto sqlParmStart = new SqlParmDto();
                    sqlParmStart.parmName = "start";
                    sqlParmStart.parmValue = String.valueOf((current - 1) * size);
                    SqlParmDto sqlParmEnd = new SqlParmDto();
                    sqlParmEnd.parmName = "end";
                    sqlParmEnd.parmValue = String.valueOf(current * size);
                    sqlParamsDto.add(sqlParmStart);
                    sqlParamsDto.add(sqlParmEnd);
                }
            }
            sql = SqlParmUtils.SqlParams(sqlParamsDto, dto.getApiDTO().getCreateSql(), "@", dataSourceConVO.getConType());
            countSql = SqlParmUtils.SqlParams(sqlParamsDto, dto.getApiDTO().getCreateCountSql(), "@", dataSourceConVO.getConType());

            log.info("数据服务【preview】自定义模式SQL参数【sql】：" + sql);
            log.info("数据服务【preview】自定义模式SQL参数【countSql】：" + countSql);
        }

        // 第五步：如果是编辑，查询上次配置的字段信息，如果描述信息不为空，使用该描述。如果为空使用系统查询出来的描述信息
        List<FieldConfigPO> fieldConfigPOS = null;
        if (dto.apiId != 0) {
            QueryWrapper<FieldConfigPO> fieldConfigPOQueryWrapper = new QueryWrapper<>();
            fieldConfigPOQueryWrapper.lambda()
                    .eq(FieldConfigPO::getDelFlag, 1)
                    .eq(FieldConfigPO::getApiId, dto.apiId);
            fieldConfigPOS = apiFieldMapper.selectList(fieldConfigPOQueryWrapper);
        }

        // 第六步：判断数据源类型，加载数据库驱动，执行SQL
        Connection conn = null;
        Statement st = null;
        try {
            String conStr = dbCommand.buildSchemaConStr(dto.apiDTO.getTableFramework(), dataSourceConVO.getConStr());
            log.info("数据服务【preview】连接字符串sql语句：" + conStr);

            conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), conStr, dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
            /*
                以流的形式 TYPE_FORWARD_ONLY: 只可向前滚动查询 CONCUR_READ_ONLY: 指定不可以更新 ResultSet
                如果PreparedStatement对象初始化时resultSetType参数设置为TYPE_FORWARD_ONLY，
                在从ResultSet（结果集）中读取记录的时，对于访问过的记录就自动释放了内存。
                而设置为TYPE_SCROLL_INSENSITIVE或TYPE_SCROLL_SENSITIVE时为了保证能游标能向上移动到任意位置，
                已经访问过的所有都保留在内存中不能释放。所以大量数据加载的时候，就OOM了
                 */
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // 查询10条
            // st.setFetchSize(10);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            //获取数据集
            apiPreviewVO = resultSetToJsonArray(conn, dbCommand, rs, dto, fieldConfigPOS, dataSourceConVO.getConType());
            rs.close();
            int totalCount = 0;
            if (StringUtils.isNotEmpty(countSql)) {
                ResultSet countRs = st.executeQuery(countSql);
                if (countRs.next()) {
                    Object count = countRs.getObject(1);
                    if (count != null && RegexUtils.isNumeric(count) != null)
                        totalCount = RegexUtils.isNumeric(count);
                }
                countRs.close();
                apiPreviewVO.setTotal(totalCount);
            }
            apiPreviewVO.setCurrent(current);
            apiPreviewVO.setSize(size);
            apiPreviewVO.setPage((int) Math.ceil(1.0 * totalCount / size));
        } catch (Exception e) {
            log.error("【preview】系统异常：" + e);
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR, e.getMessage());
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                log.error("【preview】数据库关闭异常：" + ex);
                throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR, ex.getMessage());
            }
        }
        return apiPreviewVO;
    }

    /**
     * 预览结果转Json数组
     *
     * @param conn      数据库连接
     * @param dbCommand 数据库sql
     * @param rs        查询结果
     * @param pvDTO     预览请求参数
     * @return target
     */
    private static ApiPreviewVO resultSetToJsonArray(Connection conn, IBuildDataServiceSqlCommand dbCommand,
                                                     ResultSet rs, ApiPreviewDTO pvDTO, List<FieldConfigPO> fieldConfigPOS,
                                                     DataSourceTypeEnum conType)
            throws SQLException, JSONException {
        ApiPreviewVO data = new ApiPreviewVO();

        JSONArray array = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldConfigVO> fieldConfigVOS = new ArrayList<>();
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.add(jsonObj);
        }

        // 获取描述信息
        List<FieldInfoVO> tableFieldList = null;
        if (pvDTO.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()
                && StringUtils.isNotEmpty(pvDTO.apiDTO.getTableRelName())) {
            if (conType == DataSourceTypeEnum.DORIS) {
                tableFieldList = getTableFieldByDorisList(conn, dbCommand, pvDTO.apiDTO.getTableFramework(), pvDTO.apiDTO.getTableRelName());
            } else {
                tableFieldList = getTableFieldList(conn, dbCommand, pvDTO.apiDTO.getTableFramework(), pvDTO.apiDTO.getTableRelName());
            }
        }

        //获取列名、描述
        for (int i = 1; i <= columnCount; i++) {
            FieldConfigVO fieldConfigVO = new FieldConfigVO();
            // 源字段
            fieldConfigVO.fieldName = metaData.getColumnLabel(i);
            fieldConfigVO.fieldType = metaData.getColumnTypeName(i).toUpperCase();
            fieldConfigVO.fieldSort = i;
            if (pvDTO.apiId != 0)
                fieldConfigVO.apiId = pvDTO.apiId;

            if (fieldConfigVO.fieldType.contains("INT2")
                    || fieldConfigVO.fieldType.contains("INT4")
                    || fieldConfigVO.fieldType.contains("INT8")) {
                fieldConfigVO.fieldType = "INT".toLowerCase();
            }

            fieldConfigVO.fieldType = fieldConfigVO.fieldType.toLowerCase();
            // 读取不到类型，默认字符串类型
            if (fieldConfigVO.fieldType == null ||
                    fieldConfigVO.fieldType == "")
                fieldConfigVO.fieldType = "varchar";

            // 获取表字段描述
            if (CollectionUtils.isNotEmpty(tableFieldList)) {
                FieldInfoVO fieldInfoVO = tableFieldList.stream().
                        filter(item -> item.fieldName.equals(fieldConfigVO.fieldName)).findFirst().orElse(null);
                if (fieldInfoVO != null) {
                    fieldConfigVO.fieldDesc = fieldInfoVO.fieldDesc;
                }
            }
            if (CollectionUtils.isNotEmpty(fieldConfigPOS)) {
                FieldConfigPO fieldConfigPO = fieldConfigPOS.stream().filter(t -> t.getFieldName().equals(fieldConfigVO.fieldName)).findFirst().orElse(null);
                if (fieldConfigPO != null && StringUtils.isNotEmpty(fieldConfigPO.fieldDesc)) {
                    fieldConfigVO.fieldDesc = fieldConfigPO.fieldDesc;
                }
            }
            fieldConfigVOS.add(fieldConfigVO);
        }
        data.fieldVO = fieldConfigVOS.stream().collect(Collectors.toList());
        data.dataArray = array;
        array = null;
        return data;
    }

    /**
     * 查询表字段信息
     *
     * @param conn           连接
     * @param dbCommand      数据库sql
     * @param tableFramework 表架构名
     * @param tableRelName   表名称，不带架构名
     * @return statement
     */
    private static List<FieldInfoVO> getTableFieldList(Connection conn, IBuildDataServiceSqlCommand dbCommand,
                                                       String tableFramework, String tableRelName) {
        List<FieldInfoVO> fieldList = new ArrayList<>();
        if (StringUtils.isEmpty(tableRelName))
            return fieldList;
        String sql = dbCommand.buildUseExistTableFiled(tableFramework, tableRelName);
        log.info("【getTableFieldList】查询字段sql语句：" + sql);
        if (sql == null || sql.isEmpty())
            return fieldList;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                FieldInfoVO fieldInfoVO = new FieldInfoVO();
                fieldInfoVO.tableName = resultSet.getString("tableName");
                fieldInfoVO.fieldName = resultSet.getString("fieldName");
                fieldInfoVO.fieldDesc = resultSet.getString("fieldDesc");
                if (StringUtils.isNotEmpty(fieldInfoVO.tableName) && StringUtils.isNotEmpty(fieldInfoVO.fieldName)
                        && StringUtils.isNotEmpty(fieldInfoVO.fieldDesc)) {
                    fieldList.add(fieldInfoVO);
                }
            }
        } catch (Exception ex) {
            log.error("【getTableFieldList】系统异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ":" + ex.getMessage());
        }
        return fieldList;
    }

    /**
     * 查询表字段信息Doris
     *
     * @param conn           连接
     * @param dbCommand      数据库sql
     * @param tableFramework 表架构名
     * @param tableRelName   表名称，不带架构名
     * @return statement
     */
    private static List<FieldInfoVO> getTableFieldByDorisList(Connection conn, IBuildDataServiceSqlCommand dbCommand,
                                                              String tableFramework, String tableRelName) {
        String[] split = tableRelName.split(".");
        String tableName = split[split.length - 1];
        List<FieldInfoVO> fieldList = new ArrayList<>();
        if (StringUtils.isEmpty(tableRelName))
            return fieldList;
        String sql = dbCommand.buildUseExistTableFiled(tableFramework, tableRelName);
        log.info("【getTableFieldList】查询字段sql语句：" + sql);
        if (sql == null || sql.isEmpty())
            return fieldList;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                FieldInfoVO fieldInfoVO = new FieldInfoVO();
                fieldInfoVO.tableName = tableName;
                fieldInfoVO.fieldName = resultSet.getString("Field");
                fieldInfoVO.fieldDesc = resultSet.getString("Extar");
                if (StringUtils.isNotEmpty(fieldInfoVO.tableName) && StringUtils.isNotEmpty(fieldInfoVO.fieldName)
                        && StringUtils.isNotEmpty(fieldInfoVO.fieldDesc)) {
                    fieldList.add(fieldInfoVO);
                }
            }
        } catch (Exception ex) {
            log.error("【getTableFieldList】系统异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ":" + ex.getMessage());
        }
        return fieldList;
    }
}
