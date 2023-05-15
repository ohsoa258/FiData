package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.dataservice.dto.datasource.DataSourceConDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConEditDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConQuery;
import com.fisk.dataservice.dto.datasource.TestConnectionDTO;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.enums.SourceTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.mapper.DataSourceConMapper;
import com.fisk.dataservice.service.IDataSourceConManageService;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.dataservice.vo.datasource.DataSourceVO;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源接口实现类
 *
 * @author dick
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManageService {

    @Resource
    DataSourceConMapper mapper;

    @Resource
    RedisUtil redisUtil;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserHelper userHelper;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private DataModelClient dataModelClient;

    @Resource
    private MdmClient mdmClient;

    @Value("${dataservice.datasource.metadataentity_key}")
    private String metaDataEntityKey;

    @Override
    public PageDTO<DataSourceConVO> listDataSourceCons(DataSourceConQuery query) {
        PageDTO<DataSourceConVO> pageDTO = new PageDTO<>();
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            if (query != null && StringUtils.isNotEmpty(query.keyword)) {
                allDataSource = allDataSource.stream().filter(
                        t -> (t.getConDbname().contains(query.keyword)) ||
                                t.getConType().getName().contains(query.keyword)).collect(Collectors.toList());
            }
            allDataSource.forEach(t -> {
                if (t.getDatasourceType() != SourceTypeEnum.FiData) {
                    t.setConPassword("");
                }
            });
        }
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            pageDTO.setTotal(Long.valueOf(allDataSource.size()));
            query.current = query.current - 1;
            allDataSource = allDataSource.stream().sorted(Comparator.comparing(DataSourceConVO::getCreateTime).reversed()).skip((query.current - 1 + 1) * query.size).limit(query.size).collect(Collectors.toList());
        }
        pageDTO.setItems(allDataSource);
        return pageDTO;
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        if (dto.datasourceType == SourceTypeEnum.FiData) {
            queryWrapper.lambda().eq(DataSourceConPO::getDatasourceId, dto.datasourceId)
                    .eq(DataSourceConPO::getDelFlag, 1);
        } else {
            queryWrapper.lambda().eq(DataSourceConPO::getName, dto.name)
                    .eq(DataSourceConPO::getDelFlag, 1);
        }
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.DS_DATASOURCE_EXISTS;
        }
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        model.setCreateTime(LocalDateTime.now());
        Long userId = userHelper.getLoginUserInfo().getId();
        model.setCreateUser(userId.toString());
        boolean isInsert = baseMapper.insertOne(model) > 0;
        if (!isInsert)
            return ResultEnum.SAVE_DATA_ERROR;
        if (model.getDatasourceType() == SourceTypeEnum.custom.getValue()) {
            setMetaDataToRedis(model, 1);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        if (dto.datasourceType == SourceTypeEnum.FiData) {
            queryWrapper.lambda()
                    .eq(DataSourceConPO::getDatasourceId, dto.datasourceId)
                    .eq(DataSourceConPO::getDelFlag, 1)
                    .ne(DataSourceConPO::getId, dto.id);
        } else {
            queryWrapper.lambda()
                    .eq(DataSourceConPO::getName, dto.name)
                    .eq(DataSourceConPO::getDelFlag, 1)
                    .ne(DataSourceConPO::getId, dto.id);
        }
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.DS_DATASOURCE_EXISTS;
        }
        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        if (dto.getDatasourceType() == SourceTypeEnum.custom) {
            setMetaDataToRedis(model, 2);
        }
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (model.getDatasourceType() == SourceTypeEnum.custom.getValue()) {
            setMetaDataToRedis(model, 3);
        }
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @SneakyThrows
    @Override
    public ResultEnum testConnection(TestConnectionDTO dto) {
        Connection conn = null;
        try {
            conn = getStatement(dto.getConType(), dto.getConStr(), dto.getConAccount(), dto.getConPassword());
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            return ResultEnum.DS_DATASOURCE_CON_ERROR;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DS_DATASOURCE_CON_ERROR);
            }
        }
    }

    @Override
    public List<DataSourceConVO> getAll() {
        return getAllDataSource();
    }

    @Override
    public DataSourceVO getMetaDataById(int id) {
        DataSourceVO dataSourceVO = new DataSourceVO();
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1)
                .eq(DataSourceConPO::getId, id);
        DataSourceConPO dataSourceConPO = mapper.selectOne(queryWrapper);
        if (dataSourceConPO == null) {
            return dataSourceVO;
        }
        FiDataMetaDataTreeDTO fiDataMetaDataTreeDTO = new FiDataMetaDataTreeDTO();
        if (dataSourceConPO.getDatasourceType() == SourceTypeEnum.custom.getValue()) {
            String redisKey = metaDataEntityKey + "_" + id;
            Boolean exist = redisTemplate.hasKey(redisKey);
            if (!exist) {
                setMetaDataToRedis(dataSourceConPO, 1);
            }
            String json = redisTemplate.opsForValue().get(redisKey).toString();
            if (StringUtils.isNotEmpty(json)) {
                fiDataMetaDataTreeDTO = JSONObject.parseObject(json, FiDataMetaDataTreeDTO.class);
            }
        } else {
            fiDataMetaDataTreeDTO = getFiDataConfigMetaData(dataSourceConPO);
        }
        List<DataSourceConVO> allDataSource = getAllDataSource();
        DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == dataSourceConPO.getId()).findFirst().orElse(null);
        if (dataSourceConVO == null) {
            return dataSourceVO;
        }
        dataSourceConVO.setConPassword("");
        dataSourceConVO.setConAccount("");
        dataSourceVO.setDataSourceCon(dataSourceConVO);
        dataSourceVO.setTree(fiDataMetaDataTreeDTO);
        return dataSourceVO;
    }

    @Override
    public ResultEntity<Object> reloadMetaData(int id) {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1)
                .eq(DataSourceConPO::getId, id);
        DataSourceConPO dataSourceConPO = mapper.selectOne(queryWrapper);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DS_DATASOURCE_NOTEXISTS, "数据源不存在");
        }
        if (dataSourceConPO.getDatasourceType() == SourceTypeEnum.FiData.getValue()) {
            ResultEntity<DataSourceDTO> fiDataDataSourceResult =
                    userClient.getFiDataDataSourceById(dataSourceConPO.getDatasourceId());
            if (fiDataDataSourceResult.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO fiDataSourceDTO = fiDataDataSourceResult.getData();
                FiDataMetaDataReqDTO reqDTO = new FiDataMetaDataReqDTO();
                reqDTO.setDataSourceId(String.valueOf(fiDataSourceDTO.id));
                reqDTO.setDataSourceName(fiDataSourceDTO.getConDbname());
                switch (fiDataSourceDTO.id) {
                    case 1:
                    case 4:
                        // dw olap
                        dataModelClient.setDataModelStructure(reqDTO);
                        break;
                    case 2:
                        // ods
                        dataAccessClient.setDataAccessStructure(reqDTO);
                        break;
                    case 3:
                        // mdm
                        mdmClient.setMDMDataStructure(reqDTO);
                        break;
                }
            }
        } else {
            setMetaDataToRedis(dataSourceConPO, 2);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, "已重新加载数据源");
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 获取自定义表信息
     * @author dick
     * @date 2022/7/21 11:03
     * @version v1.0
     * @params
     */
    public FiDataMetaDataTreeDTO getFiDataConfigMetaData(DataSourceConPO dataSourceConPO) {
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;
        fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
        fiDataMetaDataTreeBase.setId("-10");
        fiDataMetaDataTreeBase.setParentId("-100");
        fiDataMetaDataTreeBase.setLabel("FiData");
        fiDataMetaDataTreeBase.setLabelAlias("FiData");
        fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);
        fiDataMetaDataTreeBase.children = new ArrayList<>();
        List<FiDataMetaDataDTO> fiDataMetaData = redisUtil.getFiDataMetaData(String.valueOf(dataSourceConPO.datasourceId));
        if (CollectionUtils.isNotEmpty(fiDataMetaData)) {
            fiDataMetaDataTreeBase.children.add(fiDataMetaData.get(0).children.get(0));
        }
        return fiDataMetaDataTreeBase;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 获取自定义元数据信息
     * @author dick
     * @date 2022/7/21 11:03
     * @version v1.0
     * @params
     */
    public FiDataMetaDataTreeDTO getCustomizeMetaData(DataSourceConPO dataSourceConPO) {
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;
        fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
        fiDataMetaDataTreeBase.setId("-20");
        fiDataMetaDataTreeBase.setParentId("-200");
        fiDataMetaDataTreeBase.setLabel("Customize");
        fiDataMetaDataTreeBase.setLabelAlias("Customize");
        fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);

        List<FiDataMetaDataTreeDTO> fiDataMetaDataTreeDateBases = new ArrayList<>();
        FiDataMetaDataTreeDTO fiDataMetaDataTreeDateBase = new FiDataMetaDataTreeDTO();
        fiDataMetaDataTreeDateBase.setId(String.valueOf(dataSourceConPO.id));
        fiDataMetaDataTreeDateBase.setParentId("-20");
        fiDataMetaDataTreeDateBase.setLabel(dataSourceConPO.conDbname);
        fiDataMetaDataTreeDateBase.setLabelAlias(dataSourceConPO.conDbname);
        fiDataMetaDataTreeDateBase.setLevelType(LevelTypeEnum.DATABASE);

        List<FiDataMetaDataTreeDTO> fiDataMetaDataTreeTableView = new ArrayList<>();
        FiDataMetaDataTreeDTO fiDataMetaDataTreeTable = new FiDataMetaDataTreeDTO();
        String uuid_TableFOLDERId = UUID.randomUUID().toString().replace("-", "");
        fiDataMetaDataTreeTable.setId(uuid_TableFOLDERId);
        fiDataMetaDataTreeTable.setParentId(String.valueOf(dataSourceConPO.id));
        fiDataMetaDataTreeTable.setLabel("TABLE");
        fiDataMetaDataTreeTable.setLabelAlias("TABLE");
        fiDataMetaDataTreeTable.setLevelType(LevelTypeEnum.FOLDER);
        fiDataMetaDataTreeTable.setChildren(getCustomizeMetaData_Table(dataSourceConPO, uuid_TableFOLDERId));
        fiDataMetaDataTreeTableView.add(fiDataMetaDataTreeTable);

        FiDataMetaDataTreeDTO fiDataMetaDataTreeView = new FiDataMetaDataTreeDTO();
        String uuid_ViewFOLDERId = UUID.randomUUID().toString().replace("-", "");
        fiDataMetaDataTreeView.setId(uuid_ViewFOLDERId);
        fiDataMetaDataTreeView.setParentId(String.valueOf(dataSourceConPO.id));
        fiDataMetaDataTreeView.setLabel("VIEW");
        fiDataMetaDataTreeView.setLabelAlias("VIEW");
        fiDataMetaDataTreeView.setLevelType(LevelTypeEnum.FOLDER);
        fiDataMetaDataTreeView.setChildren(getCustomizeMetaData_View(dataSourceConPO, uuid_ViewFOLDERId));
        fiDataMetaDataTreeTableView.add(fiDataMetaDataTreeView);

        fiDataMetaDataTreeDateBase.setChildren(fiDataMetaDataTreeTableView);
        fiDataMetaDataTreeDateBases.add(fiDataMetaDataTreeDateBase);

        fiDataMetaDataTreeBase.setChildren(fiDataMetaDataTreeDateBases);
        return fiDataMetaDataTreeBase;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 自定义元数据，表信息
     * @author dick
     * @date 2022/7/21 11:03
     * @version v1.0
     * @params conPo
     */
    public List<FiDataMetaDataTreeDTO> getCustomizeMetaData_Table(DataSourceConPO conPo, String uuid_TableFOLDERId) {
        List<FiDataMetaDataTreeDTO> fiDataMetaDataTrees = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        try {
            DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[conPo.conType];
            Connection connection = getStatement(dataSourceTypeEnum, conPo.getConStr(), conPo.getConAccount(), conPo.getConPassword());
            ;
            List<TablePyhNameDTO> tableNameAndColumns = null;
            switch (dataSourceTypeEnum) {
                case MYSQL:
                    // 表结构
                    tableNameAndColumns = mysqlConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DataSourceTypeEnum.MYSQL);
                    break;
                case SQLSERVER:
                    // 表结构
                    tableNameAndColumns = sqlServerPlusUtils.getTableNameAndColumnsPlus(conPo.conStr, conPo.conAccount, conPo.conPassword, DataSourceTypeEnum.SQLSERVER);
                    break;
                case POSTGRESQL:
                    // 表结构
                    tableNameAndColumns = postgresConUtils.getTableNameAndColumns(conPo.conStr, conPo.conAccount, conPo.conPassword, DataSourceTypeEnum.POSTGRESQL);
                    break;
            }
            connection.close();

            if (CollectionUtils.isNotEmpty(tableNameAndColumns)) {
                for (TablePyhNameDTO table : tableNameAndColumns) {

                    String uuid_TableId = UUID.randomUUID().toString().replace("-", "");
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_Table.setId(uuid_TableId);
                    fiDataMetaDataTree_Table.setParentId(uuid_TableFOLDERId);
                    fiDataMetaDataTree_Table.setLabel(table.tableFullName);
                    fiDataMetaDataTree_Table.setLabelAlias(table.tableFullName);
                    fiDataMetaDataTree_Table.setLabelFramework(table.tableFramework);
                    fiDataMetaDataTree_Table.setLabelRelName(table.tableName);
                    fiDataMetaDataTree_Table.setSourceId(Math.toIntExact(conPo.id));
                    fiDataMetaDataTree_Table.setSourceType(SourceTypeEnum.custom.getValue());
                    fiDataMetaDataTree_Table.setLevelType(LevelTypeEnum.TABLE);
                    fiDataMetaDataTree_Table.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                    fiDataMetaDataTree_Table.setPublishState("1");
                    List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Table_Children = new ArrayList<>();

                    if (CollectionUtils.isNotEmpty(table.fields)) {
                        for (TableStructureDTO field : table.fields) {

                            String uuid_FieldId = UUID.randomUUID().toString().replace("-", "");
                            FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = new FiDataMetaDataTreeDTO();
                            fiDataMetaDataTree_Field.setId(uuid_FieldId);
                            fiDataMetaDataTree_Field.setParentId(uuid_TableId);
                            fiDataMetaDataTree_Field.setLabel(field.fieldName);
                            fiDataMetaDataTree_Field.setLabelAlias(field.fieldName);
                            fiDataMetaDataTree_Field.setSourceId(Math.toIntExact(conPo.id));
                            fiDataMetaDataTree_Field.setSourceType(SourceTypeEnum.custom.getValue());
                            fiDataMetaDataTree_Field.setLevelType(LevelTypeEnum.FIELD);
                            fiDataMetaDataTree_Field.setLabelType(field.fieldType);
                            fiDataMetaDataTree_Field.setLabelLength(String.valueOf(field.fieldLength));
                            fiDataMetaDataTree_Field.setLabelDesc(field.fieldDes);
                            fiDataMetaDataTree_Field.setParentName(table.tableFullName);
                            fiDataMetaDataTree_Field.setParentNameAlias(table.tableFullName);
                            fiDataMetaDataTree_Field.setParentLabelFramework(table.tableFramework);
                            fiDataMetaDataTree_Field.setParentLabelRelName(table.tableName);
                            fiDataMetaDataTree_Field.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                            fiDataMetaDataTree_Field.setPublishState("1");
                            fiDataMetaDataTree_Table_Children.add(fiDataMetaDataTree_Field);
                        }
                    }
                    fiDataMetaDataTree_Table.setChildren(fiDataMetaDataTree_Table_Children);
                    fiDataMetaDataTrees.add(fiDataMetaDataTree_Table);
                }
            }
        } catch (Exception ex) {
            return fiDataMetaDataTrees;
        }
        return fiDataMetaDataTrees;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 自定义元数据，视图信息
     * @author dick
     * @date 2022/7/21 11:02
     * @version v1.0
     * @params conPo
     */
    public List<FiDataMetaDataTreeDTO> getCustomizeMetaData_View(DataSourceConPO conPo, String uuid_ViewFOLDERId) {
        List<FiDataMetaDataTreeDTO> fiDataMetaDataTrees = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        try {
            List<DataBaseViewDTO> viewNameAndColumns = null;
            switch (DataSourceTypeEnum.values()[conPo.conType]) {
                case MYSQL:
                    // 表结构
                    viewNameAndColumns = mysqlConUtils.loadViewDetails(DataSourceTypeEnum.MYSQL, conPo.conStr, conPo.conAccount, conPo.conPassword);
                    break;
                case SQLSERVER:
                    // 表结构
                    viewNameAndColumns = sqlServerPlusUtils.loadViewDetails(DataSourceTypeEnum.SQLSERVER, conPo.conStr, conPo.conAccount, conPo.conPassword);
                    break;
                case POSTGRESQL:
                    // 表结构
                    viewNameAndColumns = postgresConUtils.loadViewDetails(DataSourceTypeEnum.POSTGRESQL, conPo.conStr, conPo.conAccount, conPo.conPassword);
                    break;
            }

            if (CollectionUtils.isNotEmpty(viewNameAndColumns)) {
                for (DataBaseViewDTO view : viewNameAndColumns) {

                    String uuid_TableId = UUID.randomUUID().toString().replace("-", "");
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_View = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_View.setId(uuid_TableId);
                    fiDataMetaDataTree_View.setParentId(uuid_ViewFOLDERId);
                    fiDataMetaDataTree_View.setLabel(view.viewName);
                    fiDataMetaDataTree_View.setLabelAlias(view.viewName);
                    fiDataMetaDataTree_View.setLabelRelName(view.viewRelName);
                    fiDataMetaDataTree_View.setLabelFramework(view.viewFramework);
                    fiDataMetaDataTree_View.setSourceId(Math.toIntExact(conPo.id));
                    fiDataMetaDataTree_View.setSourceType(SourceTypeEnum.custom.getValue());
                    fiDataMetaDataTree_View.setLevelType(LevelTypeEnum.VIEW);
                    fiDataMetaDataTree_View.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                    fiDataMetaDataTree_View.setPublishState("1");
                    List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_View_Children = new ArrayList<>();

                    if (CollectionUtils.isNotEmpty(view.fields)) {
                        for (TableStructureDTO field : view.fields) {

                            String uuid_FieldId = UUID.randomUUID().toString().replace("-", "");
                            FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = new FiDataMetaDataTreeDTO();
                            fiDataMetaDataTree_Field.setId(uuid_FieldId);
                            fiDataMetaDataTree_Field.setParentId(uuid_TableId);
                            fiDataMetaDataTree_Field.setLabel(field.fieldName);
                            fiDataMetaDataTree_Field.setLabelAlias(field.fieldName);
                            fiDataMetaDataTree_Field.setSourceId(Math.toIntExact(conPo.id));
                            fiDataMetaDataTree_Field.setSourceType(SourceTypeEnum.custom.getValue());
                            fiDataMetaDataTree_Field.setLevelType(LevelTypeEnum.FIELD);
                            fiDataMetaDataTree_Field.setLabelType(field.fieldType);
                            fiDataMetaDataTree_Field.setLabelLength(String.valueOf(field.fieldLength));
                            fiDataMetaDataTree_Field.setLabelDesc(field.fieldDes);
                            fiDataMetaDataTree_Field.setParentName(view.viewName);
                            fiDataMetaDataTree_Field.setParentNameAlias(view.viewName);
                            fiDataMetaDataTree_Field.setParentLabelRelName(view.viewRelName);
                            fiDataMetaDataTree_Field.setParentLabelFramework(view.viewFramework);
                            fiDataMetaDataTree_Field.setLabelBusinessType(TableBusinessTypeEnum.NONE.getValue());
                            fiDataMetaDataTree_Field.setPublishState("1");
                            fiDataMetaDataTree_View_Children.add(fiDataMetaDataTree_Field);
                        }
                    }
                    fiDataMetaDataTree_View.setChildren(fiDataMetaDataTree_View_Children);
                    fiDataMetaDataTrees.add(fiDataMetaDataTree_View);
                }
            }
        } catch (Exception ex) {
            return fiDataMetaDataTrees;
        }
        return fiDataMetaDataTrees;
    }

    /**
     * 查询数据质量所有数据源信息，含FiData系统数据源
     *
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @author dick
     * @date 2022/6/16 23:17
     * @version v1.0
     * @params
     */
    public List<DataSourceConVO> getAllDataSource() {
        List<DataSourceConVO> dataSourceList = new ArrayList<>();
        // FiData数据源信息
        ResultEntity<List<DataSourceDTO>> fiDataDataSourceResult = userClient.getAllFiDataDataSource();
        final List<DataSourceDTO> fiDataDataSources = fiDataDataSourceResult != null && fiDataDataSourceResult.getCode() == 0
                ? userClient.getAllFiDataDataSource().getData() : null;
        // 数据质量数据源信息
        QueryWrapper<DataSourceConPO> dataSourceConPOQueryWrapper = new QueryWrapper<>();
        dataSourceConPOQueryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = baseMapper.selectList(dataSourceConPOQueryWrapper);
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            dataSourceConPOList.forEach(t -> {
                DataSourceConVO dataSourceConVO = new DataSourceConVO();
                dataSourceConVO.setId(Math.toIntExact(t.getId()));
                dataSourceConVO.setDatasourceType(SourceTypeEnum.getEnum(t.getDatasourceType()));
                dataSourceConVO.setDatasourceId(t.getDatasourceId());
                dataSourceConVO.setCreateTime(t.getCreateTime().format(pattern));
                if (t.getDatasourceType() == 2) {
                    dataSourceConVO.setName(t.getName());
                    dataSourceConVO.setConStr(t.getConStr());
                    dataSourceConVO.setConIp(t.getConIp());
                    dataSourceConVO.setConPort(t.getConPort());
                    dataSourceConVO.setConDbname(t.getConDbname());
                    dataSourceConVO.setConType(DataSourceTypeEnum.getEnum(t.getConType()));
                    dataSourceConVO.setConAccount(t.getConAccount());
                    dataSourceConVO.setConPassword(t.getConPassword());
                    dataSourceList.add(dataSourceConVO);
                } else if (t.getDatasourceType() == 1 && CollectionUtils.isNotEmpty(fiDataDataSources)) {
                    Optional<DataSourceDTO> first = fiDataDataSources.stream().filter(item -> item.getId() == t.getDatasourceId()).findFirst();
                    if (first.isPresent()) {
                        DataSourceDTO dataSourceDTO = first.get();
                        dataSourceConVO.setName(dataSourceDTO.getName());
                        dataSourceConVO.setConStr(dataSourceDTO.getConStr());
                        dataSourceConVO.setConIp(dataSourceDTO.getConIp());
                        dataSourceConVO.setConPort(dataSourceDTO.getConPort());
                        dataSourceConVO.setConDbname(dataSourceDTO.getConDbname());
                        dataSourceConVO.setConType(DataSourceTypeEnum.getEnum(dataSourceDTO.getConType().getValue()));
                        dataSourceConVO.setConAccount(dataSourceDTO.getConAccount());
                        dataSourceConVO.setConPassword(dataSourceDTO.getConPassword());
                        dataSourceList.add(dataSourceConVO);
                    }
                }
            });
        }
        return dataSourceList;
    }

    /**
     * @return java.sql.Connection
     * @description 通过配置的数据信息，建立数据库连接通道
     * @author dick
     * @date 2022/7/21 11:55
     * @version v1.0
     * @params driver
     * @params url
     * @params username
     * @params password
     */
    public Connection getStatement(DataSourceTypeEnum dataSourceTypeEnum, String connectionStr, String account, String password) {
        try {
            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
            Connection connection = dbHelper.connection(connectionStr, account, password, dataSourceTypeEnum);
            return connection;
        } catch (Exception e) {
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR);
        }
    }

    /**
     * @return void
     * @description 保存自定义数据源的元数据信息到Redis
     * @author dick
     * @date 2022/7/21 11:59
     * @version v1.0
     * @params datasourceId
     * @params operationType 操作类型 1、新增 2、修改 3、删除
     */
    public void setMetaDataToRedis(DataSourceConPO dataSourceConPO, int operationType) {
        if (StringUtils.isEmpty(metaDataEntityKey)) {
            return;
        }
        String redisKey = metaDataEntityKey + "_" + dataSourceConPO.getId();
        if (operationType == 3) {
            Boolean exist = redisTemplate.hasKey(redisKey);
            if (exist) {
                redisTemplate.delete(redisKey);
            }
        } else if (operationType == 1 || operationType == 2) {
            FiDataMetaDataTreeDTO meta = getCustomizeMetaData(dataSourceConPO);
            String json = JSONArray.toJSON(meta).toString();
            redisTemplate.opsForValue().set(redisKey, json);
        }
    }

    /**
     * @return void
     * @description 保存自定义数据源的元数据信息到Redis.定时任务调用
     * @author dick
     * @date 2022/5/11 10:33
     * @version v1.0
     */
    public void setMetaDataToRedis() {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1)
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.custom.getValue());
        List<DataSourceConPO> dataSourceConPOs = mapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataSourceConPOs)) {
            for (DataSourceConPO dataSourceConPO : dataSourceConPOs) {
                setMetaDataToRedis(dataSourceConPO, 2);
            }
        }
    }
}
