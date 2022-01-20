package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.dto.datamodel.AppRegistrationDataDTO;
import com.fisk.dataaccess.dto.datamodel.TableAccessDataDTO;
import com.fisk.dataaccess.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsQueryDTO;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.entity.*;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.map.TableAccessMap;
import com.fisk.dataaccess.map.TableBusinessMap;
import com.fisk.dataaccess.map.TableFieldsMap;
import com.fisk.dataaccess.mapper.*;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.utils.sql.MysqlConUtils;
import com.fisk.dataaccess.utils.sql.SqlServerConUtils;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.TableNameVO;
import com.fisk.dataaccess.vo.pgsql.NifiVO;
import com.fisk.dataaccess.vo.pgsql.TableListVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasWriteBackDataDTO;
import com.fisk.task.dto.daconfig.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import com.fisk.task.enums.OdsDataSyncTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TableAccessImpl extends ServiceImpl<TableAccessMapper, TableAccessPO> implements ITableAccess {

    @Resource
    private TableFieldsImpl tableFieldsImpl;
    @Resource
    private TableAccessMapper accessMapper;
    @Resource
    private TableFieldsMapper fieldsMapper;
    @Resource
    private AppRegistrationImpl appRegistrationImpl;
    @Resource
    private AppRegistrationMapper registrationMapper;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private TableSyncmodeImpl syncmodeImpl;
    @Resource
    private TableSyncmodeMapper syncmodeMapper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private TableBusinessImpl businessImpl;
    @Resource
    private TableBusinessMapper businessMapper;
    @Resource
    private NifiConfigMapper nifiConfigMapper;
    @Resource
    private NifiConfigImpl nifiConfigImpl;
    @Value("${spring.datasource.url}")
    private String jdbcStr;
    @Value("${spring.datasource.username}")
    private String user;
    @Value("${spring.datasource.password}")
    private String password;
    @Resource
    private GenerateCondition generateCondition;
    @Resource
    private GetMetadata getMetadata;
    @Resource
    private TableSyncmodeImpl tableSyncmodeImpl;
    @Resource
    private EtlIncrementalMapper etlIncrementalMapper;
    @Value("${pgsql-datamodel.url}")
    public String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    public String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    public String pgsqlDatamodelPassword;

    @Value("${pgsql-ods.url}")
    public String pgsqlOdsUrl;
    @Value("${pgsql-ods.username}")
    public String pgsqlOdsUsername;
    @Value("${pgsql-ods.password}")
    public String pgsqlOdsPassword;

    /**
     * 添加物理表(实时)
     *
     * @param tableAccessDTO 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addRealTimeData(TableAccessDTO tableAccessDTO) {

        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        // 数据保存: 添加应用的时候,相同的表名不可以再次添加
        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        String tableName = modelAccess.getTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
        tableNameVO.appId = tableAccessDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEnum.Table_NAME_EXISTS;
        }

        if (tableAccessDTO.appId < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        modelAccess.setCreateUser(String.valueOf(userId));
        modelAccess.appId = tableAccessDTO.appId;

        // 0是实时物理表，1是非实时物理表
        modelAccess.setSyncSrc(tableAccessDTO.getSyncSrc());
        modelAccess.setIsRealtime(0);

        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);

        if (!saveAccess) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 保存tb_table_fields数据
        boolean saveFields = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            modelField.setCreateUser(String.valueOf(userId));

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);

            saveFields = tableFieldsImpl.save(modelField);
        }

        if (!saveFields) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return saveFields ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 添加物理表(非实时)
     *
     * @param tableAccessNonDTO dto
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<AtlasIdsVO> addNonRealTimeData(TableAccessNonDTO tableAccessNonDTO) {
        // 当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessNonDTO.toEntity(TableAccessPO.class);
        // 判断table_name是否已存在(不同应用注册下,名称可以相同)
        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        // TODO: tableName 物理表名称
        String tableName = modelAccess.getTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
//        tableNameVO.appId = modelReg.id;
        tableNameVO.appId = tableAccessNonDTO.appId;
        tableNameVO.tableName = tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            return ResultEntityBuild.build(ResultEnum.Table_NAME_EXISTS);
        }

        if (tableAccessNonDTO.appId < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }
        modelAccess.setAppId(tableAccessNonDTO.appId);
        // 0是实时物理表，1是非实时物理表
        modelAccess.setSyncSrc(tableAccessNonDTO.getSyncSrc());
        // 非实时
        modelAccess.setIsRealtime(1);
        // 当前登录人
        modelAccess.setCreateUser(String.valueOf(userId));
        // 2.保存tb_table_access数据
        boolean saveAccess = this.save(modelAccess);
        if (!saveAccess) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存tb_table_fields数据
        boolean saveField = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessNonDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
            modelField.setTableAccessId(modelAccess.getId());

            // 1是实时物理表的字段，0是非实时物理表的字段
            modelField.setIsRealtime(1);
            modelField.setCreateUser(String.valueOf(userId));

            saveField = tableFieldsImpl.save(modelField);
        }

        if (!saveField) {
//            return ResultEnum.SAVE_DATA_ERROR;
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // TODO 新增tb_table_business业务时间表
        // 保存tb_table_business数据
        TableBusinessDTO businessDTO = tableAccessNonDTO.getBusinessDTO();
        TableBusinessPO modelBusiness = TableBusinessMap.INSTANCES.dtoToPo(businessDTO);
        modelBusiness.setAccessId(modelAccess.getId());
        boolean saveBusiness = this.businessImpl.save(modelBusiness);
        if (!saveBusiness) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存tb_table_syncmode数据
        TableSyncmodeDTO syncmodeDTO = tableAccessNonDTO.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = syncmodeDTO.toEntity(TableSyncmodePO.class);
        modelSync.setId(modelAccess.getId());

        boolean saveSync = syncmodeImpl.save(modelSync);
        if (!saveSync) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        AtlasIdsVO atlasIdsVO = new AtlasIdsVO();
        atlasIdsVO.userId = userId;
        // 应用注册id
        atlasIdsVO.appId = String.valueOf(tableAccessNonDTO.appId);
        atlasIdsVO.dbId = String.valueOf(modelAccess.getId());
        atlasIdsVO.tableName = tableName;

        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    /**
     * 修改物理表(实时)
     *
     * @param tableAccessDTO dto
     * @return 执行结果
     */
    @Override
    public ResultEnum updateRealTimeData(TableAccessDTO tableAccessDTO) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.dto->po
        TableAccessPO modelAccess = tableAccessDTO.toEntity(TableAccessPO.class);

        modelAccess.setUpdateUser(String.valueOf(userId));

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 保存tb_table_fields数据: 分为更新和添加数据
        boolean updateFields = true;
        boolean saveField = true;
        List<TableFieldsDTO> fieldsDTOList = tableAccessDTO.getList();

        for (TableFieldsDTO tableFieldsDTO : fieldsDTOList) {
            // 0: 旧数据不操作  1: 修改  2: 新增
            int funcType = tableFieldsDTO.getFuncType();
            // 修改
            if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setUpdateUser(String.valueOf(userId));
                updateFields = tableFieldsImpl.updateById(modelField);
                // 新增
            } else if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                // 还要绑定tb_table_access id
                modelField.setTableAccessId(modelAccess.getId());
                modelField.setUpdateUser(String.valueOf(userId));
                saveField = tableFieldsImpl.save(modelField);
            }
        }

        if (!updateFields || !saveField) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        return updateFields ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 修改物理表(非实时)
     *
     * @param dto dto
     * @return 执行结果
     */
    @Override
    public ResultEnum updateNonRealTimeData(TableAccessNonDTO dto) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // dto->po
        TableAccessPO modelAccess = dto.toEntity(TableAccessPO.class);

        modelAccess.setUpdateUser(String.valueOf(userId));

        // 2.保存tb_table_access数据
        boolean updateAccess = this.updateById(modelAccess);

        if (!updateAccess) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 3.保存tb_table_fields数据
        boolean updateField = true;
        boolean saveField = true;
        List<TableFieldsDTO> list = dto.getList();

        for (TableFieldsDTO tableFieldsDTO : list) {
            // 0: 未操作的数据  1: 新增  2: 编辑
            int funcType = tableFieldsDTO.getFuncType();
            if (funcType == 2) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setUpdateUser(String.valueOf(userId));
                updateField = tableFieldsImpl.updateById(modelField);
            } else if (funcType == 1) {
                TableFieldsPO modelField = tableFieldsDTO.toEntity(TableFieldsPO.class);
                modelField.setTableAccessId(modelAccess.getId());
                modelField.setUpdateUser(String.valueOf(userId));
                saveField = tableFieldsImpl.save(modelField);
            }
        }

        List<TableFieldsPO> originalData = tableFieldsImpl.query().eq("table_access_id", list.get(0).tableAccessId).list();
        List<TableFieldsPO> webData = TableFieldsMap.INSTANCES.listDtoToPo(list);
        List<TableFieldsPO> collect = originalData.stream().filter(item -> !webData.contains(item)).collect(Collectors.toList());

        try {
            collect.stream().map(e -> {
                return fieldsMapper.deleteByIdWithFill(e);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        if (!updateField) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        if (!saveField) {
            return ResultEnum.DATAACCESS_SAVEFIELD_ERROR;
        }

        // TODO 新增tb_table_business业务时间表
        // 4.保存tb_table_business数据
        TableBusinessDTO businessDTO = dto.getBusinessDTO();
        TableBusinessPO modelBusiness = TableBusinessMap.INSTANCES.dtoToPo(businessDTO);
        boolean updateBusiness = this.businessImpl.updateById(modelBusiness);
        if (!updateBusiness) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 5.保存tb_table_syncmode数据
        boolean updateSync;
        TableSyncmodeDTO tableSyncmodeDTO = dto.getTableSyncmodeDTO();
        TableSyncmodePO modelSync = tableSyncmodeDTO.toEntity(TableSyncmodePO.class);
        updateSync = syncmodeImpl.updateById(modelSync);

        return updateSync ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 物理表接口首页分页查询
     *
     * @param key  key
     * @param page page
     * @param rows rows
     * @return 查询结果
     */
    @Override
    public Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows) {
        // 新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }

    /**
     * 回显实时表
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public TableAccessNonDTO getData(long id) {

        // 查询tb_table_access数据
        TableAccessPO modelAccess = this.query().eq("id", id).eq("del_flag", 1).one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableAccessNonDTO dto = TableAccessMap.INSTANCES.poToDtoNon(modelAccess);

        // 将应用名称封装进去
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", modelAccess.getAppId()).one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dto.setAppName(modelReg.getAppName());

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();

        List<TableFieldsDTO> listField = new ArrayList<>();
        for (TableFieldsPO modelField : list) {
            TableFieldsDTO tableFieldsDTO = TableFieldsMap.INSTANCES.poToDto(modelField);

            listField.add(tableFieldsDTO);
        }

        dto.setList(listField);

        // 查询tb_table_syncmode
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        TableSyncmodeDTO sdto = new TableSyncmodeDTO(modelSync);
        dto.setTableSyncmodeDTO(sdto);

        // 只有存在业务时间覆盖时,才会给前端展示
        if (modelSync != null && modelSync.syncMode == 4) {
            // 查询tb_table_business
            QueryWrapper<TableBusinessPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TableBusinessPO::getAccessId, id);
            TableBusinessPO modelBusiness = businessMapper.selectOne(queryWrapper);
            TableBusinessDTO businessDTO = TableBusinessMap.INSTANCES.poToDto(modelBusiness);
            dto.setBusinessDTO(businessDTO);
        }

        return dto;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.TableFieldsDTO>
     * @description sql语句更新后，过滤已删除的源字段
     * @author Lock
     * @date 2021/12/28 14:48
     * @version v1.0
     * @params sourceFieldList
     * @params queryDto
     */
    private List<FieldNameDTO> filterSqlFieldList(List<TableFieldsDTO> sourceFieldList, OdsQueryDTO queryDto) {

        OdsResultDTO resultDto = getDataAccessQueryList(queryDto);
        // 执行sql
        List<FieldNameDTO> fieldNameDTOList = resultDto.fieldNameDTOList;
        List<TableFieldsDTO> dtoList = new ArrayList<>();
        // 重新组装库中的字段属性
        sourceFieldList.forEach(e -> {
            TableFieldsDTO dto = new TableFieldsDTO();
            dto.id = e.id;
            dto.tableAccessId = e.tableAccessId;
            dto.sourceFieldName = e.sourceFieldName;
            dto.fieldName = e.fieldName;
            dto.isPrimarykey = e.isPrimarykey;
            dto.isRealtime = e.isRealtime;
            dto.fieldDes = e.fieldDes;
            dto.fieldType = e.fieldType;
            dto.fieldLength = e.fieldLength;
            dtoList.add(dto);
        });
        // 组装执行sql后的字段属性
        fieldNameDTOList.forEach(e -> dtoList.stream().filter(f -> f.sourceFieldName.equals(e.sourceFieldName)).forEachOrdered(f -> {
            e.id = f.id;
            e.tableAccessId = Math.toIntExact(f.tableAccessId);
            e.sourceFieldName = f.sourceFieldName;
            e.fieldName = f.fieldName;
            e.isPrimarykey = f.isPrimarykey;
            e.isRealtime = f.isRealtime;
            e.fieldDes = f.fieldDes;
            e.fieldType = f.fieldType;
            e.fieldLength = String.valueOf(f.fieldLength);
        }));
        return fieldNameDTOList;
    }


    /**
     * 根据应用名称,获取物理表名及表对应的字段(非实时)
     *
     * @param appName appName
     * @return 查询结果
     */
    @Override
    public List<TablePyhNameDTO> getTableFields(String appName) {

        // 1.根据应用名称查询表id
        AppRegistrationPO modelReg = appRegistrationImpl.query()
                .eq("app_name", appName)
                .eq("del_flag", 1)
                .one();

        // tb_app_registration表id
        long appid = modelReg.getId();

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();
        String dbName = modelDataSource.dbName;

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        List<TablePyhNameDTO> list = new ArrayList<>();
        switch (modelDataSource.driveType) {
            case "mysql":
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
//                list = mysqlConUtils.getTableNameAndColumns(url, user, pwd);
                break;
            case "sqlserver":
                list = new SqlServerConUtils().getTableNameAndColumns(url, user, pwd, dbName);
                break;
            default:
                break;
        }

        return list;
    }

    @Override
    public List<TablePyhNameDTO> getTableFieldsByAppId(long appId) {

        // 2.根据app_id查询关联表tb_app_datasource的connect_str  connect_account  connect_pwd
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appId).one();
        String url = modelDataSource.getConnectStr();
        String user = modelDataSource.getConnectAccount();
        String pwd = modelDataSource.getConnectPwd();
        String dbName = modelDataSource.dbName;

        // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
        List<TablePyhNameDTO> list = new ArrayList<>();
        switch (modelDataSource.driveType) {
            case "mysql":
            case "oracle":
                // 3.调用MysqlConUtils,连接远程数据库,获取所有表及对应字段
                MysqlConUtils mysqlConUtils = new MysqlConUtils();
                list = mysqlConUtils.getTableNameAndColumns(url, user, pwd, com.fisk.dataaccess.enums.DriverTypeEnum.MYSQL);
                break;
            case "sqlserver":
                list = new SqlServerConUtils().getTableNameAndColumns(url, user, pwd, dbName);
                break;
            default:
                return null;
        }

        return list;
    }

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<NifiVO> deleteData(long id) {

        UserInfo userInfo = userHelper.getLoginUserInfo();

        // 1.删除tb_table_access数据
        TableAccessPO modelAccess = this.getById(id);
        if (modelAccess == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        int deleteAccess = accessMapper.deleteByIdWithFill(modelAccess);
        if (deleteAccess < 0) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 2.删除tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query()
                .eq("table_access_id", id)
                .list();
        try {
            // 判断是否存在表字段
            if (!CollectionUtils.isEmpty(list)) {
                list.forEach(e -> fieldsMapper.deleteByIdWithFill(e));
            }
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        // 查询tb_table_syncmode
        TableSyncmodePO modelSync = this.syncmodeMapper.getData(id);
        if (modelSync != null && modelSync.syncMode == 4) {
            TableBusinessPO modelBusiness = businessImpl.query().eq("access_id", id).one();
            if (modelBusiness != null) {
                int deleteBusiness = businessMapper.deleteByIdWithFill(modelBusiness);
                if (deleteBusiness < 0) {
                    return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", modelAccess.appId).eq("del_flag", 1).one();

        NifiVO vo = new NifiVO();
        List<TableListVO> voList = new ArrayList<>();
        TableListVO tableListVO = new TableListVO();

        tableListVO.userId = userInfo.id;
//        tableListVO.tableAtlasId = modelAccess.atlasTableId;
        tableListVO.tableName = registrationPo.appAbbreviation + "_" + modelAccess.tableName;
        voList.add(tableListVO);
        List<Long> tableIdList = new ArrayList<>();
        tableIdList.add(id);

        vo.appId = String.valueOf(modelAccess.appId);
        vo.userId = userInfo.id;
        vo.appAtlasId = registrationPo.atlasInstanceId;
        vo.tableList = voList;
        vo.tableIdList = tableIdList;
        log.info("删除的物理表信息,{}", vo);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, vo);

    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_BUILDTABLE_AND_COLUMN)
    @Override
    public ResultEntity<AtlasEntityDbTableColumnDTO> getAtlasBuildTableAndColumn(long id, long appid) {
        AtlasEntityDbTableColumnDTO dto = null;
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelAccess == null || modelDataSource == null || modelSync == null || modelReg == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        dto = new AtlasEntityDbTableColumnDTO();
        dto.dbId = modelDataSource.getAtlasDbId();
        dto.tableName = modelAccess.getTableName();
        dto.createUser = modelAccess.getCreateUser();
        // TODO:驱动类型(改为枚举类型)
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
            switch (modelDataSource.driveType) {
                case "sqlserver":
                    dto.dbType = DbTypeEnum.sqlserver;
                    break;
                case "mysql":
                    dto.dbType = DbTypeEnum.mysql;
                    break;
                case "postgresql":
                    dto.dbType = DbTypeEnum.postgresql;
                    break;
                case "oracle":
                    dto.dbType = DbTypeEnum.oracle;
                    break;
                default:
                    break;
            }
        }
        // TODO 新增cron表达式
        dto.cornExpress = modelSync.cornExpression;
        // TODO 新增appAbbreviation syncType syncField
        dto.appAbbreviation = modelReg.appAbbreviation;
        switch (modelSync.syncMode) {
            case 1:
                dto.syncType = OdsDataSyncTypeEnum.full_volume;
                break;
            case 2:
                dto.syncType = OdsDataSyncTypeEnum.timestamp_incremental;
                dto.syncField = modelSync.syncField;
                break;
            case 3:
                dto.syncType = OdsDataSyncTypeEnum.business_time_cover;
                break;
            default:
                break;
        }
        dto.tableId = String.valueOf(modelAccess.getId());
        List<AtlasEntityColumnDTO> columns = new ArrayList<>();
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        if (list.isEmpty()) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        for (TableFieldsPO po : list) {
            AtlasEntityColumnDTO atlasEntityColumnDTO = new AtlasEntityColumnDTO();
            atlasEntityColumnDTO.setColumnId(po.getId());
            atlasEntityColumnDTO.setColumnName(po.getFieldName());
            atlasEntityColumnDTO.setComment(po.getFieldDes());
            if (po.fieldLength == 0) {
                atlasEntityColumnDTO.setDataType(po.getFieldType());
            } else {

                atlasEntityColumnDTO.setDataType(po.getFieldType() + "(" + po.fieldLength + ")");
            }
            atlasEntityColumnDTO.setIsKey("" + po.getIsPrimarykey() + "");
            atlasEntityColumnDTO.setGuid(po.atlasFieldId);

            columns.add(atlasEntityColumnDTO);
        }
        dto.columns = columns;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_WRITEBACKDATA)
    @Override
    public ResultEntity<AtlasWriteBackDataDTO> getAtlasWriteBackDataDTO(long appid, long id) {
        AtlasWriteBackDataDTO dto = new AtlasWriteBackDataDTO();
        // 查询tb_app_registration
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        // 查询tb_app_datasource
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        if (modelReg == null || modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        dto.appId = modelReg.atlasInstanceId;
        dto.atlasTableId = modelDataSource.atlasDbId;
        // 查询tb_table_access
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        dto.tableId = modelAccess.atlasTableId;
        AtlasEntityDbTableColumnDTO atlasDTO = new AtlasEntityDbTableColumnDTO();
        atlasDTO.dbId = modelDataSource.getAtlasDbId();
        atlasDTO.tableName = modelAccess.getTableName();
        atlasDTO.createUser = modelAccess.getCreateUser();
        // TODO:驱动类型
        if (StringUtils.isNotBlank(modelDataSource.driveType)) {
            switch (modelDataSource.driveType) {
                case "sqlserver":
                    atlasDTO.dbType = DbTypeEnum.sqlserver;
                    break;
                case "mysql":
                    atlasDTO.dbType = DbTypeEnum.mysql;
                    break;
                case "postgresql":
                    atlasDTO.dbType = DbTypeEnum.postgresql;
                    break;
                case "oracle":
                    atlasDTO.dbType = DbTypeEnum.oracle;
                    break;
                default:
                    break;
            }
        }
        List<AtlasEntityColumnDTO> columns = new ArrayList<>();

        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        if (list.isEmpty()) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        for (TableFieldsPO po : list) {
            AtlasEntityColumnDTO atlasEntityColumnDTO = new AtlasEntityColumnDTO();
            atlasEntityColumnDTO.setColumnId(po.getId());
            atlasEntityColumnDTO.setColumnName(po.getFieldName());
            atlasEntityColumnDTO.setComment(po.getFieldDes());
            if (po.fieldLength == 0) {
                atlasEntityColumnDTO.setDataType(po.getFieldType());
            } else {
                atlasEntityColumnDTO.setDataType(po.getFieldType() + "(" + po.fieldLength + ")");
            }
            atlasEntityColumnDTO.setIsKey("" + po.getIsPrimarykey() + "");
            columns.add(atlasEntityColumnDTO);
        }
        dto.columnsKeys = columns;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAtlasTableIdAndDorisSql(AtlasWriteBackDataDTO dto) {
        // 应用注册id
        long appid = Long.parseLong(dto.appId);
        // 物理表id
        long tableId = Long.parseLong(dto.tableId);
        // 物理表: tb_table_access
        TableAccessPO modelAccess = this.query().eq("id", tableId).eq("app_id", appid).eq("del_flag", 1).one();
        if (modelAccess == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        modelAccess.atlasTableId = dto.atlasTableId;
        boolean update = this.updateById(modelAccess);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<AtlasEntityColumnDTO> list = dto.columnsKeys;
        boolean updateField = true;
        for (AtlasEntityColumnDTO columnDTO : list) {
            // 根据本表id查询tb_table_fields
            TableFieldsPO modelFields = this.tableFieldsImpl.query().eq("id", columnDTO.columnId).one();
            if (modelFields == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            // 回写的字段GUID
            modelFields.atlasFieldId = columnDTO.getGuid();
            // 更新tb_table_fields表数据
            updateField = this.tableFieldsImpl.updateById(modelFields);
            if (!updateField) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return updateField ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.DATAACCESS_CONFIG)
    @Override
    public ResultEntity<DataAccessConfigDTO> dataAccessConfig(long id, long appid) {
        String mysqlType = "mysql";
        String sqlserverType = "sqlserver";
        String postgresqlType = "postgresql";
        // 增量
        int syncModel = 4;
        DataAccessConfigDTO dto = new DataAccessConfigDTO();
        // app组配置
        GroupConfig groupConfig = new GroupConfig();
        //任务组配置
        TaskGroupConfig taskGroupConfig = new TaskGroupConfig();
        // 数据源jdbc配置
        DataSourceConfig sourceDsConfig = new DataSourceConfig();
        // 目标源jdbc连接
        DataSourceConfig targetDsConfig = new DataSourceConfig();
        // 增量配置库源jdbc连接
        DataSourceConfig cfgDsConfig = new DataSourceConfig();
        // 表及表sql
        ProcessorConfig processorConfig = new ProcessorConfig();
        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        AppRegistrationPO modelReg = this.appRegistrationImpl.query().eq("id", appid).eq("del_flag", 1).one();
        if (modelReg == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(modelReg.getAppName());
        groupConfig.setAppDetails(modelReg.getAppDes());
        //3.数据源jdbc配置
        AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("app_id", appid).eq("del_flag", 1).one();
        if (modelDataSource == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        sourceDsConfig.setJdbcStr(modelDataSource.getConnectStr());
        // 选择驱动类型
        if (Objects.equals(modelDataSource.driveType, sqlserverType)) {
            sourceDsConfig.setType(DriverTypeEnum.SQLSERVER);
        } else if (Objects.equals(modelDataSource.driveType, mysqlType)) {
            sourceDsConfig.setType(DriverTypeEnum.MYSQL);
        } else if (Objects.equals(modelDataSource.driveType, postgresqlType)) {
            sourceDsConfig.setType(DriverTypeEnum.POSTGRESQL);
        }
        sourceDsConfig.setUser(modelDataSource.getConnectAccount());
        sourceDsConfig.setPassword(modelDataSource.getConnectPwd());
        // 5.表及表sql
        TableSyncmodePO modelSync = syncmodeMapper.getData(id);
        if (modelSync == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        // TODO: 新增同步方式
        targetDsConfig.syncMode = modelSync.syncMode;
        // 增量的时候,获取业务时间覆盖对象
        if (targetDsConfig.syncMode == syncModel) {
            TableBusinessPO modelBusiness = businessImpl.query().eq("access_id", id).one();
            dto.businessDTO = TableBusinessMap.INSTANCES.poToDto(modelBusiness);
        }
        TableAccessPO modelAccess = this.query().eq("id", id).eq("app_id", appid).eq("del_flag", 1).one();
        // 2.任务组配置
        taskGroupConfig.setAppName(modelAccess.getTableName());
        taskGroupConfig.setAppDetails(modelAccess.getTableDes());
        if (modelAccess == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        // corn_expression
        processorConfig.scheduleExpression = modelSync.getCornExpression();

        if (modelSync.syncField != null) {
            // 增量字段
            processorConfig.syncField = modelSync.syncField;
        }
        String timerDriver = "Timer driven";
        String corn = "CORN driven";
        if (timerDriver.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.TIMER;
        } else if (corn.equalsIgnoreCase(modelSync.timerDriver)) {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.CRON;
        } else {
            processorConfig.scheduleType = SchedulingStrategyTypeEnum.EVENT;
        }
        // TODO  新增: 增量配置库源jdbc连接
        cfgDsConfig.setType(DriverTypeEnum.MYSQL);
        cfgDsConfig.setJdbcStr(jdbcStr);
        cfgDsConfig.setUser(user);
        cfgDsConfig.setPassword(password);
        // TODO: 2021/9/4 nifi流程需要物理表字段
        List<TableFieldsPO> list = this.tableFieldsImpl.query().eq("table_access_id", id).eq("del_flag", 1).list();
        List<TableFieldsDTO> fieldsDTOList = TableFieldsMap.INSTANCES.listPoToDto(list);
        String businessKeyAppend = "";
        if (list != null && !list.isEmpty()) {
            targetDsConfig.tableFieldsList = fieldsDTOList;
            // 封装业务主键
            businessKeyAppend = list.stream().filter(e -> e.isPrimarykey == 1).map(e -> e.fieldName + ",").collect(Collectors.joining());
        }

        if (businessKeyAppend.length() > 0) {
            dto.businessKeyAppend = businessKeyAppend.substring(0, businessKeyAppend.length() - 1);
        }
        dto.groupConfig = groupConfig;
        dto.taskGroupConfig = taskGroupConfig;
        dto.sourceDsConfig = sourceDsConfig;
        dto.targetDsConfig = targetDsConfig;
        dto.processorConfig = processorConfig;
        dto.cfgDsConfig = cfgDsConfig;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    public TableAccessPO insertTableAccessPo(TableAccessPO tableAccessPo) {
        accessMapper.insertTableAccessPo(tableAccessPo);
        return tableAccessPo;
    }

    @Override
    public BuildNifiFlowDTO createPgToDorisConfig(String tableName, String selectSql) {
        //添五张表tb_app_registration  tb_app_datasource   tb_table_access  tb_nifi_setting; tb_etl_Incremental
        BuildNifiFlowDTO buildNifiFlowDTO = new BuildNifiFlowDTO();
        AppRegistrationPO appRegistrationPo = new AppRegistrationPO();
        AppDataSourcePO appDataSourcePo = new AppDataSourcePO();
        TableAccessPO tableAccessPo = new TableAccessPO();
        TableSyncmodePO tableSyncmodePo = new TableSyncmodePO();
        appRegistrationPo.appName = "postgerToDoris";
        appRegistrationPo.appDes = "postgerToDoris";
        appRegistrationPo.appType = 1;
        appRegistrationPo.delFlag = 1;
        //后面加上AppRegistrationPO.id
        appDataSourcePo.appId = appRegistrationPo.id;
        //ConnectStr   driveType   ConnectAccount   ConnectPwd
        appDataSourcePo.driveType = "postgresql";
        appDataSourcePo.connectAccount = pgsqlDatamodelUsername;
        appDataSourcePo.connectPwd = pgsqlDatamodelPassword;
        appDataSourcePo.connectStr = pgsqlDatamodelUrl;
        //后续补上AppRegistrationPO.id
        tableAccessPo.appId = appRegistrationPo.id;
        tableAccessPo.isRealtime = 1;
        tableAccessPo.tableName = tableName;
        tableAccessPo.delFlag = 1;
        //可能都要返回id
        insertTableAccessPo(tableAccessPo);
        tableSyncmodePo.syncMode = 1;
        //tableAccess.id
        tableSyncmodePo.id = tableAccessPo.id;
        EtlIncrementalPO etlIncrementalPo = new EtlIncrementalPO();
        etlIncrementalPo.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
        etlIncrementalMapper.insert(etlIncrementalPo);
        buildNifiFlowDTO.appId = appRegistrationPo.id;
        buildNifiFlowDTO.id = tableAccessPo.id;
        buildNifiFlowDTO.synchronousTypeEnum = SynchronousTypeEnum.PGTODORIS;
        return buildNifiFlowDTO;
    }

    @Override
    public Page<TableAccessVO> listData(TableAccessQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        if (query.key != null && query.key.length() > 0) {
            querySql.append(" and table_name like concat('%', " + "'").append(query.key).append("'").append(", '%') ");
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        TableAccessPageDTO data = new TableAccessPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        List<FilterFieldDTO> list = new ArrayList<>();
        list = getMetadata.getMetadataList(
                "dmp_datainput_db",
                "tb_table_access",
                "a",
                FilterSqlConstants.TABLE_ACCESS_SQL);
        List<FilterFieldDTO> fieldDTOList = getMetadata.getMetadataList(
                "dmp_datainput_db",
                "tb_table_syncmode",
                "b",
                FilterSqlConstants.TABLE_SYNCMODE_SQL);
        list.addAll(fieldDTOList);
        return list;
    }

    @Override
    public List<TableNameDTO> getDataAccessMeta() {

        List<TableNameDTO> list = new ArrayList<>();

        // 获取所有表
        List<TableNameDTO> listTableName = baseMapper.listTableName();

        for (TableNameDTO tableNameDTO : listTableName) {

            getTableFieldsById(list, tableNameDTO);
        }

        return list;
    }

    /**
     * 封装物理表及字段
     *
     * @param list         list
     * @param tableNameDTO tableNameDTO
     */
    private void getTableFieldsById(List<TableNameDTO> list, TableNameDTO tableNameDTO) {
        TableNameDTO dto = new TableNameDTO();
        List<FieldNameDTO> listFieldName = fieldsMapper.listTableName(tableNameDTO.id);
        dto.id = tableNameDTO.id;
        dto.tableName = tableNameDTO.tableName;
        dto.field = listFieldName;
        list.add(dto);
    }

    @Override
    public List<DataAccessTreeDTO> getTree() {

        List<DataAccessTreeDTO> appTree = registrationMapper.listAppTree();

        return appTree.stream().map(e -> {
            e.setList(baseMapper.listTableNameTree(e.id).stream().map(f -> {
                f.flag = 1;
                f.setPid(e.id);
                f.appType = e.appType;
                return f;
            }).collect(Collectors.toList()));
            e.flag = 1;
            return e;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TableNameDTO> getTableName(long id) {
        List<TableNameDTO> list = new ArrayList<>();

        // 获取物理表
        List<TableNameDTO> listTableName = baseMapper.listTableNameByAppId(id);

        // 根据tb_access_id获取表字段及id
        for (TableNameDTO tableNameDTO : listTableName) {
            getTableFieldsById(list, tableNameDTO);
        }

        return list;
    }

    @Override
    public TableAccessDTO getTableAccess(int id) {
        TableAccessDTO dto = new TableAccessDTO();
        TableAccessPO po = accessMapper.selectById(id);
        if (po == null) {
            return dto;
        }
        return dto = TableAccessMap.INSTANCES.poToDto(po);
    }

    @Override
    public List<FieldNameDTO> getTableFieldId(int id) {
        QueryWrapper<TableFieldsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id").lambda().eq(TableFieldsPO::getTableAccessId, id);
        List<FieldNameDTO> list = fieldsMapper.listTableName(id);
        return list;
    }

    @Override
    public List<ChannelDataDTO> getTableId() {

        // select id,app_name from tb_app_registration where del_flag=1 ORDER BY create_time DESC;
        List<AppRegistrationPO> list = appRegistrationImpl.list(Wrappers.<AppRegistrationPO>lambdaQuery()
                .select(AppRegistrationPO::getId, AppRegistrationPO::getAppName)
                .orderByDesc(AppRegistrationPO::getCreateTime));

        // list: po -> dto
        List<ChannelDataDTO> channelDataDTOList = AppRegistrationMap.INSTANCES.listPoToChannelDataDto(list);

        // 查询当前应用下面的所有表
        channelDataDTOList.forEach(dto -> {
            // select id,table_name from tb_table_access where app_id =#{dto.id} and del_flag = 1
            List<TableAccessPO> poList = this.list(Wrappers.<TableAccessPO>lambdaQuery()
                    .eq(TableAccessPO::getAppId, dto.id)
                    // publish=3: 正在发布 -> 1:发布成功
                    .or()
                    .eq(TableAccessPO::getPublish, 3)
                    .eq(TableAccessPO::getPublish, 1)
                    .select(TableAccessPO::getId, TableAccessPO::getTableName));
            // list: po->dto 并赋值给dto.list
            dto.list = TableAccessMap.INSTANCES.listPoToChannelDataDto(poList);
        });

        return channelDataDTOList;
    }

    @Override
    public List<AppRegistrationDataDTO> getDataAppRegistrationMeta() {

        List<AppRegistrationDataDTO> list = new ArrayList<>();

        //获取所有应用注册列表
        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();
        List<AppRegistrationPO> poList = registrationMapper.selectList(queryWrapper);
        if (poList == null || poList.size() == 0) {
            return list;
        }
        list = AppRegistrationMap.INSTANCES.listPoToDtoList(poList);
        //获取所有表配置数据
        QueryWrapper<TableAccessPO> wrapper = new QueryWrapper<>();
        // 只需要发布状态为3: 正在发布 -> 1:发布成功
        wrapper.lambda().eq(TableAccessPO::getPublish, 3).or().eq(TableAccessPO::getPublish, 1);
        List<TableAccessPO> tableAccessList = accessMapper.selectList(wrapper);
        if (tableAccessList == null || tableAccessList.size() == 0) {
            return list;
        }
        //获取表中所有字段配置数据
        QueryWrapper<TableFieldsPO> tableFieldsQueryWrapper = new QueryWrapper<>();
        List<TableFieldsPO> tableFieldsList = fieldsMapper.selectList(tableFieldsQueryWrapper
        );
        for (AppRegistrationDataDTO item : list) {
            item.tableDtoList = TableAccessMap.INSTANCES.poListToDtoList(tableAccessList.stream()
                    .filter(e -> e.appId == item.id).collect(Collectors.toList()));
            item.tableDtoList.stream().map(e -> e.tableName = "ods_" + item.appAbbreviation + "_" + e.tableName).collect(Collectors.toList());
            if (item.tableDtoList.size() == 0 || tableFieldsList == null || tableFieldsList.size() == 0) {
                continue;
            }
            item.tableDtoList.stream().map(e -> e.type = 1).collect(Collectors.toList());
            for (TableAccessDataDTO tableAccessDataDTO : item.tableDtoList) {
                tableAccessDataDTO.fieldDtoList = TableFieldsMap.INSTANCES.poListToDtoList(tableFieldsList.stream()
                        .filter(e -> e.tableAccessId == tableAccessDataDTO.id).collect(Collectors.toList()));
                if (tableAccessDataDTO.fieldDtoList == null || tableAccessDataDTO.fieldDtoList.size() == 0) {
                    continue;
                }
                tableAccessDataDTO.fieldDtoList.stream().map(e -> e.type = 2).collect(Collectors.toList());
            }
        }
        // 反转: 倒序排序
        Collections.reverse(list);
        return list;
    }

/*
    @Override
    public List<FieldNameDTO> getTableFieldByQuery(String query) {
        List<FieldNameDTO> list = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcStr, user, password);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                FieldNameDTO dto = new FieldNameDTO();
                dto.id = rs.getInt("id");
                dto.fieldName = rs.getString("field_name");
                dto.fieldType = rs.getString("field_type");
                dto.fieldLength = rs.getString("field_length");
                dto.fieldDes = rs.getString("field_des");
                dto.tableAccessId = rs.getInt("table_access_id");
                list.add(dto);
            }
            rs.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return list;
    }
*/

    @Override
    public ResultEnum addTableAccessData(TbTableAccessDTO dto) {

        // dto -> po
        TableAccessPO model = TableAccessMap.INSTANCES.tbDtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 同一应用下表名不可重复
        boolean flag = this.checkTableName(dto);
        if (flag) {
            return ResultEnum.Table_NAME_EXISTS;
        }

        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public TbTableAccessDTO getTableAccessData(long id) {

        TableAccessPO model = this.query().eq("id", id).one();
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TbTableAccessDTO dto = TableAccessMap.INSTANCES.tbPoToDto(model);
        dto.appName = appRegistrationImpl.query().eq("id", model.appId).one().appName;
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateTableAccessData(TbTableAccessDTO dto) {

        TableAccessPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // sql保存时丢失
        if (dto.sqlFlag == 1 && "".equals(dto.sqlScript)) {
            return ResultEnum.SQL_EXCEPT_CLEAR;
        }

        // 判断名称是否重复
        QueryWrapper<TableAccessPO> queryWrapper = new QueryWrapper<>();
        // 限制在同一应用下
        queryWrapper.lambda().eq(TableAccessPO::getTableName, dto.tableName).eq(TableAccessPO::getAppId, model.appId);
        TableAccessPO tableAccessPo = baseMapper.selectOne(queryWrapper);
        if (tableAccessPo != null && tableAccessPo.id != dto.id) {
            return ResultEnum.TABLE_IS_EXIST;
        }

        // dto -> po
        TableAccessPO po = TableAccessMap.INSTANCES.tbDtoToPo(dto);

        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteTableAccessData(long id) {
        // 参数校验
        TableAccessPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        return accessMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TbTableAccessDTO> getTableAccessListData(long appId) {

        List<TableAccessPO> list = this.query().eq("app_id", appId).list();

        return TableAccessMap.INSTANCES.listTbPoToDto(list);
    }

    /**
     * 判断同一应用下表名不可重复
     *
     * @param dto dto
     * @return true: 重复, false: 不重复
     */
    private boolean checkTableName(TbTableAccessDTO dto) {
        boolean flag = false;

        List<TableNameVO> appIdAndTableNameList = this.baseMapper.getAppIdAndTableName();
        // 查询表名对应的应用注册id
        TableNameVO tableNameVO = new TableNameVO();
        tableNameVO.appId = dto.appId;
        tableNameVO.tableName = dto.tableName;
        if (appIdAndTableNameList.contains(tableNameVO)) {
            flag = true;
        }
        return flag;
    }

    @Override
    public OdsResultDTO getTableFieldByQuery(OdsQueryDTO query) {
        OdsResultDTO array = new OdsResultDTO();
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(pgsqlOdsUrl, pgsqlOdsUsername, pgsqlDatamodelPassword);
            Statement st = conn.createStatement();
            //获取总条数
            String getTotalSql = "select count(*) as total from(" + query.querySql + ") as tab";
            ResultSet rSet = st.executeQuery(getTotalSql);
            int rowCount = 0;
            if (rSet.next()) {
                rowCount = rSet.getInt("total");
            }
            rSet.close();
            //分页获取数据
            int offset = (query.pageIndex - 1) * query.pageSize;
            query.querySql = query.querySql + " limit " + query.pageSize + " offset " + offset;
            ResultSet rs = st.executeQuery(query.querySql);
            //获取数据集
            array = resultSetToJsonArrayDataModel(rs);
            array.pageIndex = query.pageIndex;
            array.pageSize = query.pageSize;
            array.total = rowCount;
            rs.close();
        } catch (ClassNotFoundException | SQLException e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ":" + e.getMessage());
        }
        return array;
    }

    public static OdsResultDTO resultSetToJsonArrayDataAccess(ResultSet rs) throws SQLException, JSONException {
        OdsResultDTO data = new OdsResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        int count = 1;
        // 预览展示10行
        int row = 10;
        while (rs.next() && count <= row) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if ("fi_batch_code".equals(columnName) || tableName.equals("ods_" + columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            String fieldType1 = "date";
            String fieldType2 = "time";
            //源表
            dto.sourceTableName = metaData.getTableName(i);
            // 源字段
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if ("fi_batch_code".equals(dto.fieldName) || tableName.equals("ods_" + dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toUpperCase();
            if (dto.fieldType.contains("INT2") || dto.fieldType.contains("INT4") || dto.fieldType.contains("INT8")) {
                dto.fieldType = "INT";
            }
            if (dto.fieldType.toLowerCase().contains(fieldType1) || dto.fieldType.toLowerCase().contains(fieldType2)) {
                dto.fieldLength = "50";
            } else {
                dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));
            }

            // 转换表字段类型和长度
            List<String> list = transformField(dto.fieldType, dto.fieldLength);
            dto.fieldType = list.get(0);
            dto.fieldLength = list.get(1);

            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    public static OdsResultDTO resultSetToJsonArrayDataModel(ResultSet rs) throws SQLException, JSONException {
        OdsResultDTO data = new OdsResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if ("fi_batch_code".equals(columnName) || tableName.equals("ods_" + columnName)
                        || "fi_createtime".equals(columnName)
                        || "fi_updatetime".equals(columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            String fieldType1 = "date";
            String fieldType2 = "time";
            //源表
            dto.sourceTableName = metaData.getTableName(i);
            // 源字段
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if ("fi_batch_code".equals(dto.fieldName) || tableName.equals("ods_" + dto.fieldName)
                    || "fi_createtime".equals(dto.fieldName)
                    || "fi_updatetime".equals(dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toUpperCase();
            if (dto.fieldType.contains("INT2") || dto.fieldType.contains("INT4") || dto.fieldType.contains("INT8")) {
                dto.fieldType = "INT";
            }
            if (dto.fieldType.toLowerCase().contains(fieldType1) || dto.fieldType.toLowerCase().contains(fieldType2)) {
                dto.fieldLength = "50";
            } else {
                dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));
            }

            // 转换表字段类型和长度
            List<String> list = transformField(dto.fieldType, dto.fieldLength);
            dto.fieldType = list.get(0);
            dto.fieldLength = list.get(1);

            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    /**
     * 转换表字段类型和长度
     *
     * @param fieldType   fieldType
     * @param fieldLength fieldLength
     * @return target
     */
    private static List<String> transformField(String fieldType, String fieldLength) {

        Integer textLength = 5000;
        String timeType = "date";

        // 浮点型
        List<String> floatType = new ArrayList<>();
        floatType.add("double");

        // 文本类型
        List<String> textTpye = new ArrayList<>();
        textTpye.add("text");

        // 字符型
        List<String> charType = new ArrayList<>();
        charType.add("");

//        List<String> timeType = new ArrayList<>();
//        timeType.add("datetime");
//        timeType.add("");
//        timeType.add("");

        // Number型
        // 整型
        List<String> integerType = new ArrayList<>();
        integerType.add("tinyint");
        integerType.add("smallint");
        integerType.add("mediumint");
        integerType.add("int");
        integerType.add("integer");
        integerType.add("bigint");
        // 精确数值型
        List<String> accurateType = new ArrayList<>();
        accurateType.add("decimal");
        accurateType.add("numeric");
        // 货币、近似数值型
        List<String> otherType = new ArrayList<>();
        otherType.add("money");
        otherType.add("smallmoney");
        otherType.add("float");
        otherType.add("real");

        //
        List<String> fieldList = new LinkedList<>();

        // boolean类型长度放开
        if (integerType.contains(fieldType.toLowerCase())) {
            fieldList.add("INT");
            fieldList.add("0");
        } else if (textTpye.contains(fieldType.toLowerCase())) {
            fieldList.add("TEXT");
            fieldList.add("0");
        } else if (accurateType.contains(fieldType.toLowerCase()) || otherType.contains(fieldType.toLowerCase())) {
            fieldList.add("FLOAT");
            fieldList.add("0");
        } else if (Integer.parseInt(fieldLength) <= 1) {
            fieldList.add("VARCHAR");
            fieldList.add("20");
        } else if (Integer.parseInt(fieldLength) >= textLength) {
            fieldList.add("VARCHAR");
            fieldList.add("5000");
        } else if (fieldType.toLowerCase().contains(timeType)) {
            fieldList.add("TIMESTAMP");
            fieldList.add("6");
        } else {
            fieldList.add("VARCHAR");
            fieldList.add(fieldLength);
        }
        return fieldList;
    }

    @Override
    public OdsResultDTO getDataAccessQueryList(OdsQueryDTO query) {
        String mysqlDriver = "mysql";
        String sqlserverDriver = "sqlserver";
        AppDataSourcePO po = appDataSourceImpl.query().eq("app_id", query.appId).one();

        OdsResultDTO array = new OdsResultDTO();
        try {
            Statement st = null;
            if (po.driveType.equalsIgnoreCase(mysqlDriver)) {
                Connection conn = getStatement(DriverTypeEnum.MYSQL.getName(), po.connectStr, po.connectAccount, po.connectPwd);
                // 以流的形式    第一个参数: 只可向前滚动查询     第二个参数: 指定不可以更新 ResultSet
                /*
                如果PreparedStatement对象初始化时resultSetType参数设置为TYPE_FORWARD_ONLY，
                在从ResultSet（结果集）中读取记录的时，对于访问过的记录就自动释放了内存。
                而设置为TYPE_SCROLL_INSENSITIVE或TYPE_SCROLL_SENSITIVE时为了保证能游标能向上移动到任意位置，
                已经访问过的所有都保留在内存中不能释放。所以大量数据加载的时候，就OOM了
                 */
                st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                // 每次流10条
                st.setFetchSize(10);
            } else if (po.driveType.equalsIgnoreCase(sqlserverDriver)) {
                //1.加载驱动程序
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                //2.获得数据库的连接
                Connection conn = DriverManager.getConnection(po.connectStr, po.connectAccount, po.connectPwd);
                st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setFetchSize(10);
            } else if (po.driveType.equalsIgnoreCase(DataSourceTypeEnum.ORACLE.getName())) {
                //1.加载驱动程序
                Class.forName(com.fisk.dataaccess.enums.DriverTypeEnum.ORACLE.getName());
                //2.获得数据库的连接
                Connection conn = DriverManager.getConnection(po.connectStr, po.connectAccount, po.connectPwd);
                st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setFetchSize(10);
            }
            assert st != null;
            Map<String, String> converSql = converSql(query.tableName, query.querySql, po.driveType);
            String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            ResultSet rs = st.executeQuery(sql);
            //获取数据集
            array = resultSetToJsonArrayDataAccess(rs);
            rs.close();
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return array;
    }

    private Map<String, String> converSql(String tableName, String sql, String driveType) {
        Map<String, String> paramMap = new HashMap<>();
        if(sql.contains(SystemVariableTypeEnum.START_TIME.getValue())||sql.contains(SystemVariableTypeEnum.END_TIME.getValue())){
            Map<String, Date> etlIncremental = etlIncrementalMapper.getEtlIncrementalByTableName(tableName);
            if (etlIncremental != null) {
                Date startTime = etlIncremental.get(SystemVariableTypeEnum.START_TIME.getName());
                Date endTime = etlIncremental.get(SystemVariableTypeEnum.END_TIME.getName());
                if (startTime != null) {
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), String.valueOf(startTime));
                    paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), String.valueOf(startTime));
                } else {
                    sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                    paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                }
                if (endTime != null) {
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), String.valueOf(endTime));
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), String.valueOf(endTime));
                } else {
                    sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                    paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                }
            } else {
                sql = sql.replaceAll(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
                sql = sql.replaceAll(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                paramMap.put(SystemVariableTypeEnum.END_TIME.getValue(), "0000-00-00");
                paramMap.put(SystemVariableTypeEnum.START_TIME.getValue(), "0000-00-00");
            }
        }
        paramMap.put(SystemVariableTypeEnum.QUERY_SQL.getValue(),sql);
        return paramMap;
    }

    @Override
    public ResultEntity<BuildPhysicalTableDTO> getBuildPhysicalTableDTO(long tableId, long appId) {

        BuildPhysicalTableDTO dto = new BuildPhysicalTableDTO();

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", appId).one();
        TableAccessPO tableAccessPo = this.query().eq("id", tableId).one();
        AppDataSourcePO dataSourcePo = appDataSourceImpl.query().eq("app_id", appId).one();
        List<TableFieldsPO> listPo = tableFieldsImpl.query().eq("table_access_id", tableId).list();
        if (tableAccessPo == null || registrationPo == null || dataSourcePo == null || CollectionUtils.isEmpty(listPo)) {
            return ResultEntityBuild.build(ResultEnum.NIFI_NOT_FIND_DATA);
        }
        TableSyncmodePO tableSyncmodePo = tableSyncmodeImpl.query().eq("id", tableId).one();
        dto.syncMode=tableSyncmodePo.syncMode;
        DbTypeEnum dbTypeEnum = DbTypeEnum.getValue(dataSourcePo.driveType);
        switch (dbTypeEnum) {
            case sqlserver:
                dto.driveType = DbTypeEnum.sqlserver;
                break;
            case mysql:
                dto.driveType = DbTypeEnum.mysql;
                break;
            case oracle:
                dto.driveType = DbTypeEnum.oracle;
            default:
                break;
        }
        dto.tableFieldsDTOS = TableFieldsMap.INSTANCES.listPoToDto(listPo);
        dto.appAbbreviation = registrationPo.appAbbreviation;
        dto.tableName = tableAccessPo.tableName;
        Map<String, String> converSql = converSql(registrationPo.appAbbreviation + "_" + tableAccessPo.tableName, tableAccessPo.sqlScript, dataSourcePo.driveType);
        String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
        dto.selectSql = sql;
        dto.queryStartTime=converSql.get(SystemVariableTypeEnum.START_TIME.getValue());
        dto.queryEndTime=converSql.get(SystemVariableTypeEnum.END_TIME.getValue());
        //        dto.selectSql = tableAccessPo.sqlScript;
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dto);
    }

    @Override
    public void updateTablePublishStatus(ModelPublishStatusDTO dto) {
        TableAccessPO model = baseMapper.selectById(dto.tableId);
        if (model != null) {
            model.publish = dto.publish;
            baseMapper.updateById(model);
        }
    }

    @Override
    public ResultEntity<ComponentIdDTO> getAppNameAndTableName(DataAccessIdsDTO dto) {

        AppRegistrationPO registrationPo = appRegistrationImpl.query().eq("id", dto.appId).one();
        TableAccessPO accessPo = this.query().eq("id", dto.tableId).one();

        ComponentIdDTO componentIdDTO = new ComponentIdDTO();
        componentIdDTO.appName = registrationPo == null ? "" : registrationPo.appName;
        componentIdDTO.tableName = accessPo == null ? "" : accessPo.tableName;
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

    @Override
    public List<FieldNameDTO> getFieldList(TableAccessNonDTO dto) {

        // 查询tb_table_access数据
        TableAccessPO modelAccess = this.query().eq("id", dto.id).one();
        if (modelAccess == null) {
            throw new FkException(ResultEnum.TABLE_NOT_EXIST);
        }
        // 将应用名称封装进去
        AppRegistrationPO modelReg = appRegistrationImpl.query().eq("id", modelAccess.getAppId()).one();
        if (modelReg == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询tb_table_fields数据
        List<TableFieldsPO> list = tableFieldsImpl.query().eq("table_access_id", dto.id).list();

        List<TableFieldsDTO> listField = list.stream().map(TableFieldsMap.INSTANCES::poToDto).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(listField)) {
            OdsQueryDTO queryDto = new OdsQueryDTO();
            queryDto.appId = modelReg.id;
            queryDto.querySql = modelAccess.sqlScript;
            queryDto.tableName = modelReg.appAbbreviation + "_" + modelAccess.tableName;
            return filterSqlFieldList(listField, queryDto);
        }
        return null;
    }

    /**
     * 连接数据库
     *
     * @param driver   driver
     * @param url      url
     * @param username username
     * @param password password
     * @return statement
     */
    private Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return conn;
    }
}
