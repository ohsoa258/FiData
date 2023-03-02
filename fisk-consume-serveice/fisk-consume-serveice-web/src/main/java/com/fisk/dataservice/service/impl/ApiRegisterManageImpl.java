package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.Dto.SqlWhereDto;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.dataservice.BuildDataServiceHelper;
import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import com.fisk.dataservice.dto.api.*;
import com.fisk.dataservice.dto.appserviceconfig.AppTableServiceConfigDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.*;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.vo.api.*;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.dataservice.vo.fileservice.FileServiceVO;
import com.fisk.dataservice.vo.tableservice.TableServiceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    TableSyncModeImpl tableSyncModeImpl;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private TableServiceMapper tableServiceMapper;

    @Resource
    private FileServiceMapper fileServiceMapper;

    @Resource
    UserHelper userHelper;

    @Resource
    private UserClient userClient;

    @Override
    public Page<ApiConfigVO> getAll(ApiRegisterQueryDTO query) {
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
                });
            }
        }
        return all;
    }

    @Override
    public PageDTO<ApiSubVO> getApiSubAll(ApiSubQueryDTO dto) {
        PageDTO<ApiSubVO> pageDTO = new PageDTO<>();

        List<ApiSubVO> apiSubVOS = new ArrayList<>();
        List<ApiConfigPO> apiConfigPOS = baseMapper.getList(dto.keyword);
        if (CollectionUtils.isNotEmpty(apiConfigPOS)) {
            apiSubVOS = ApiRegisterMap.INSTANCES.poToApiSubVO(apiConfigPOS);
            List<AppServiceConfigPO> subscribeListByAppId = appServiceConfigMapper.getSubscribeListByAppId(dto.appId);
            if (CollectionUtils.isNotEmpty(subscribeListByAppId)) {
                apiSubVOS.forEach(e -> {
                    subscribeListByAppId
                            .stream()
                            .filter(item -> item.getServiceId() == e.id
                                    && item.getType() == AppServiceTypeEnum.API.getValue())
                            .findFirst()
                            .ifPresent(user -> e.apiSubState = 1);
                });
            }
            pageDTO.setTotal(Long.valueOf(apiSubVOS.size()));
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
        if (apiConfigPO == null)
            return ResultEnum.SAVE_DATA_ERROR;
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(apiConfigPO.getDatasourceId());
        if (dataSourceConPO == null)
            return ResultEnum.DS_DATASOURCE_NOTEXISTS;
        String apiCode = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        apiConfigPO.setApiCode(apiCode);
        apiConfigPO.setCreateTime(LocalDateTime.now());
        Long userId = userHelper.getLoginUserInfo().getId();
        apiConfigPO.setCreateUser(userId.toString());
        isInsert = baseMapper.insertOne(apiConfigPO) > 0;
        if (!isInsert)
            return ResultEnum.SAVE_DATA_ERROR;
        apiId = (int) apiConfigPO.getId();

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
        List<ParmConfigPO> parmConfigPOS = ApiParmMap.INSTANCES.listDtoToPo(dto.parmDTO);
        if (CollectionUtils.isNotEmpty(parmConfigPOS)) {
            parmConfigPOS.forEach(e -> {
                e.apiId = apiId;
            });
            isInsert = apiParmManageImpl.saveBatch(parmConfigPOS);
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
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(dto.apiDTO.getDatasourceId());
        if (dataSourceConPO == null)
            return ResultEnum.DS_DATASOURCE_NOTEXISTS;
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
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteData(int apiId) {
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null) {
            return ResultEnum.DS_API_EXISTS;
        }
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<ApiRegisterDetailVO> detail(int apiId) {
        ApiRegisterDetailVO apiRegisterDetailVO = new ApiRegisterDetailVO();

        // 第一步：查询API信息,selectById仅查询有效的
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_API_EXISTS, apiRegisterDetailVO);
        apiRegisterDetailVO.apiVO = ApiRegisterMap.INSTANCES.poToVo(model);
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(model.getDatasourceId());
        apiRegisterDetailVO.apiVO.setDatasourceType(dataSourceConPO.getDatasourceType());

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
    public ResultEnum setField(List<FieldConfigEditDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<FieldConfigPO> fieldList = ApiFieldMap.INSTANCES.listDtoToPo(dto);
        return apiFieldManageImpl.updateBatchById(fieldList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ApiPreviewVO preview(ApiPreviewDTO dto) {
        ApiPreviewVO apiPreviewVO = new ApiPreviewVO();
        String sql = dto.apiDTO.getCreateSql();

        // 查询数据源信息
        DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource().stream().filter(t -> t.getId() == dto.apiDTO.getDatasourceId()).findFirst().orElse(null);
        if (dataSourceConVO == null)
            return apiPreviewVO;

        // 第一步：拼接过滤条件
        if (dto.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()) {
            sql = String.format("SELECT %s FROM %s WHERE 1=1 ", sql, dto.apiDTO.getTableName());
            if (CollectionUtils.isNotEmpty(dto.whereDTO)) {
                List<SqlWhereDto> sqlWhereList = ApiFilterConditionMap.INSTANCES.listDtoToSqlWhereDto(dto.whereDTO);
                String s = SqlParmUtils.SqlWhere(sqlWhereList);
                if (s != null && s.length() > 0)
                    sql += s;
            }
        }

        // 第二步：拼接参数条件
        if (CollectionUtils.isNotEmpty(dto.parmDTO)) {
            List<SqlParmDto> sqlParamsDto = ApiParmMap.INSTANCES.listDtoToSqlParmDto(dto.parmDTO);
            String s = SqlParmUtils.SqlParams(sqlParamsDto, sql, "@",dataSourceConVO.getConType());
            if (s != null && s.length() > 0)
                sql = s;
        }

        // 第四步：如果是编辑，查询上次配置的字段信息，如果描述信息不为空，使用该描述。如果为空使用系统查询出来的描述信息
        List<FieldConfigPO> fieldConfigPOS = null;
        if (dto.apiId != 0) {
            QueryWrapper<FieldConfigPO> fieldConfigPOQueryWrapper = new QueryWrapper<>();
            fieldConfigPOQueryWrapper.lambda()
                    .eq(FieldConfigPO::getDelFlag, 1)
                    .eq(FieldConfigPO::getApiId, dto.apiId);
            fieldConfigPOS = apiFieldMapper.selectList(fieldConfigPOQueryWrapper);
        }

        Connection conn = null;
        Statement st = null;
        try {
            IBuildDataServiceSqlCommand dbCommand = BuildDataServiceHelper.getDBCommand(dataSourceConVO.getConType());
            String conStr = dbCommand.buildSchemaConStr(dto.apiDTO.getTableFramework(), dataSourceConVO.getConStr());
            log.info("【preview】连接字符串sql语句：" + conStr);
            log.info("【preview】查询sql语句：" + sql);
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
            st.setFetchSize(10);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            //获取数据集
            apiPreviewVO = resultSetToJsonArray(conn, dbCommand, rs, dto, fieldConfigPOS);
            rs.close();
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
                                                     ResultSet rs, ApiPreviewDTO pvDTO, List<FieldConfigPO> fieldConfigPOS)
            throws SQLException, JSONException {
        ApiPreviewVO data = new ApiPreviewVO();

        JSONArray array = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldConfigVO> fieldConfigVOS = new ArrayList<>();
        int count = 1;
        while (rs.next() && count <= 10) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }

        // 获取描述信息
        List<FieldInfoVO> tableFieldList = null;
        if (pvDTO.apiDTO.getApiType() == ApiTypeEnum.SQL.getValue()
                && StringUtils.isNotEmpty(pvDTO.apiDTO.getTableRelName())) {
            tableFieldList = getTableFieldList(conn, dbCommand, pvDTO.apiDTO.getTableFramework(), pvDTO.apiDTO.getTableRelName());
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
}
