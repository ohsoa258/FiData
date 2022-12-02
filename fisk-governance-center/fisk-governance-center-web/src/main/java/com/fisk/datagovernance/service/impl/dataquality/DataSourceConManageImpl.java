package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.DataSourceConMap;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
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
    private DataSourceConMapper mapper;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public PageDTO<DataSourceConVO> page(DataSourceConQuery query) {
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
    public ResultEnum add(DataSourceConDTO dto) {
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
            return ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS;
        }
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum edit(DataSourceConEditDTO dto) {
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
            return ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS;
        }
        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delete(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
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
                return ResultEnum.DS_DATASOURCE_CON_ERROR;
            }
        }
    }

    @Override
    public FiDataMetaDataTreeDTO getFiDataConfigMetaData() {
        // 第一步：获取Tree
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.FiData.getValue())
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            dataSourceConPOList = dataSourceConPOList.stream()
                    .sorted(Comparator.comparing(DataSourceConPO::getDatasourceId))
                    .collect(Collectors.toList());
            fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
            fiDataMetaDataTreeBase.setId("-10");
            fiDataMetaDataTreeBase.setParentId("-100");
            fiDataMetaDataTreeBase.setLabel("FiData");
            fiDataMetaDataTreeBase.setLabelAlias("FiData");
            fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);
            fiDataMetaDataTreeBase.setSourceType(SourceTypeEnum.FiData.getValue());
            fiDataMetaDataTreeBase.children = new ArrayList<>();
            for (DataSourceConPO dataSourceConPO : dataSourceConPOList) {
                List<FiDataMetaDataDTO> fiDataMetaData = redisUtil.getFiDataMetaData(String.valueOf(dataSourceConPO.datasourceId));
                if (CollectionUtils.isNotEmpty(fiDataMetaData)) {
                    fiDataMetaDataTreeBase.children.add(fiDataMetaData.get(0).children.get(0));
                }
            }
        }
        // 第二步：获取表规则
        List<TableRuleCountDTO> tableRules = baseMapper.getFiDataTableRuleList();
        // 第三步：递归设置Tree-节点规则数量
        if (CollectionUtils.isNotEmpty(tableRules)) {
            fiDataMetaDataTreeBase = setFiDataRuleTree(SourceTypeEnum.FiData, fiDataMetaDataTreeBase, tableRules);
        }
        return fiDataMetaDataTreeBase;
    }

    @Override
    public FiDataMetaDataTreeDTO getCustomizeMetaData() {
        // 第一步：获取Tree
        FiDataMetaDataTreeDTO fiDataMetaDataTreeBase = null;
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceType, SourceTypeEnum.custom.getValue())
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = mapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(dataSourceConPOList)) {
            fiDataMetaDataTreeBase = new FiDataMetaDataTreeDTO();
            fiDataMetaDataTreeBase.setId("-20");
            fiDataMetaDataTreeBase.setParentId("-200");
            fiDataMetaDataTreeBase.setLabel("Customize");
            fiDataMetaDataTreeBase.setLabelAlias("Customize");
            fiDataMetaDataTreeBase.setLevelType(LevelTypeEnum.BASEFOLDER);
            fiDataMetaDataTreeBase.setSourceType(SourceTypeEnum.custom.getValue());
            List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Ips = new ArrayList<>();
            List<String> conIp = dataSourceConPOList.stream().map(t -> t.getConIp()).distinct().collect(Collectors.toList());
            for (String ip : conIp) {
                String uuid_Ip = UUID.randomUUID().toString().replace("-", "");
                FiDataMetaDataTreeDTO fiDataMetaDataTree_Ip = new FiDataMetaDataTreeDTO();
                fiDataMetaDataTree_Ip.setId(uuid_Ip);
                fiDataMetaDataTree_Ip.setParentId("-20");
                fiDataMetaDataTree_Ip.setLabel(ip);
                fiDataMetaDataTree_Ip.setLabelAlias(ip);
                fiDataMetaDataTree_Ip.setLevelType(LevelTypeEnum.FOLDER);
                fiDataMetaDataTree_Ip.setSourceType(SourceTypeEnum.custom.getValue());
                List<FiDataMetaDataTreeDTO> fiDataMetaDataTree_Ip_DataBases = new ArrayList<>();
                List<DataSourceConPO> dataSourceConPOS = dataSourceConPOList.stream().filter(t -> t.getConIp().equals(ip)).collect(Collectors.toList());
                for (DataSourceConPO dataSource : dataSourceConPOS) {
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_DataBase = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_DataBase.setId(String.valueOf(dataSource.getId()));
                    fiDataMetaDataTree_DataBase.setParentId(uuid_Ip);
                    fiDataMetaDataTree_DataBase.setLabel(dataSource.name);
                    fiDataMetaDataTree_DataBase.setLabelAlias(dataSource.name);
                    fiDataMetaDataTree_DataBase.setLevelType(LevelTypeEnum.DATABASE);
                    fiDataMetaDataTree_DataBase.setSourceId(Math.toIntExact(dataSource.id));
                    fiDataMetaDataTree_DataBase.setSourceType(SourceTypeEnum.custom.getValue());
                    fiDataMetaDataTree_DataBase.setChildren(getCustomizeMetaData_Table(dataSource));
                    fiDataMetaDataTree_Ip_DataBases.add(fiDataMetaDataTree_DataBase);
                }
                fiDataMetaDataTree_Ip.setChildren(fiDataMetaDataTree_Ip_DataBases);
                fiDataMetaDataTree_Ips.add(fiDataMetaDataTree_Ip);
            }
            fiDataMetaDataTreeBase.children = new ArrayList<>();
            fiDataMetaDataTreeBase.children.addAll(fiDataMetaDataTree_Ips);
        }
        // 第二步：获取表规则
        List<TableRuleCountDTO> tableRules = baseMapper.getCustomizeTableRuleList();
        // 第三步：递归设置Tree-节点规则数量
        if (CollectionUtils.isNotEmpty(tableRules)) {
            fiDataMetaDataTreeBase = setCustomizeRuleTree(SourceTypeEnum.custom, fiDataMetaDataTreeBase, tableRules);
        }
        return fiDataMetaDataTreeBase;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 查询自定义数据源下的表信息
     * @author dick
     * @date 2022/12/1 12:17
     * @version v1.0
     * @params conPo
     */
    public List<FiDataMetaDataTreeDTO> getCustomizeMetaData_Table(DataSourceConPO conPo) {
        List<FiDataMetaDataTreeDTO> fiDataMetaDataTrees = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        try {
            List<TablePyhNameDTO> tableNameAndColumns = null;
            switch (DataSourceTypeEnum.values()[conPo.conType]) {
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
            if (CollectionUtils.isNotEmpty(tableNameAndColumns)) {
                for (TablePyhNameDTO table : tableNameAndColumns) {

                    String uuid_TableId = UUID.randomUUID().toString().replace("-", "");
                    FiDataMetaDataTreeDTO fiDataMetaDataTree_Table = new FiDataMetaDataTreeDTO();
                    fiDataMetaDataTree_Table.setId(uuid_TableId);
                    fiDataMetaDataTree_Table.setParentId(String.valueOf(conPo.id));
                    fiDataMetaDataTree_Table.setLabel(table.tableFullName);
                    fiDataMetaDataTree_Table.setLabelAlias(table.tableFullName);
                    fiDataMetaDataTree_Table.setSourceId(Math.toIntExact(conPo.id));
                    fiDataMetaDataTree_Table.setSourceType(SourceTypeEnum.custom.getValue());
                    fiDataMetaDataTree_Table.setLevelType(LevelTypeEnum.TABLE);
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
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @description 查询数据质量所有数据源信息，含FiData系统数据源
     * @author dick
     * @date 2022/12/1 12:17
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
     * @return int
     * @description 根据FiData系统数据源的Id查询数据质量数据源ID
     * @author dick
     * @date 2022/12/1 12:16
     * @version v1.0
     * @params sourceTypeEnum
     * @params datasourceId
     */
    public int getIdByDataSourceId(SourceTypeEnum sourceTypeEnum, int datasourceId) {
        int id = 0;
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (sourceTypeEnum == SourceTypeEnum.FiData) {
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getDatasourceId() == datasourceId).findFirst().orElse(null);
            if (dataSourceConVO != null) {
                id = dataSourceConVO.getId();
            }
        }
        return id;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO>
     * @description 查询缓存中FiData的表字段名称
     * @author dick
     * @date 2022/12/1 12:15
     * @version v1.0
     * @params dtoList
     */
    public List<FiDataMetaDataDTO> getTableFieldName(List<DataTableFieldDTO> dtoList) {
        List<FiDataMetaDataDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(dtoList)) {
            return result;
        }
        List<DataSourceConfigEnum> dataSourceConfigEnums = dtoList.stream().map(DataTableFieldDTO::getDataSourceConfigEnum).distinct().collect(Collectors.toList());
        for (int i = 0; i < dataSourceConfigEnums.size(); i++) {
            List<FiDataMetaDataTreeDTO> fiDataMetaData = null;
            DataSourceConfigEnum dataSourceConfigEnum = dataSourceConfigEnums.get(i);
            fiDataMetaData = redisUtil.getFiDataTableMetaData(String.valueOf(dataSourceConfigEnum.getValue()));
            if (CollectionUtils.isEmpty(fiDataMetaData)) {
                continue;
            }
            FiDataMetaDataDTO fiDataMetaDataDTO = new FiDataMetaDataDTO();
            List<FiDataMetaDataTreeDTO> children = new ArrayList<>();
            List<DataTableFieldDTO> tableFieldDTOS = dtoList.stream().filter(t -> t.dataSourceConfigEnum == dataSourceConfigEnum).collect(Collectors.toList());
            for (int j = 0; j < tableFieldDTOS.size(); j++) {
                DataTableFieldDTO dataTableFieldDTO = tableFieldDTOS.get(j);
                FiDataMetaDataTreeDTO fiDataMetaDataTreeDTO = fiDataMetaData.stream().filter(t -> t.getId().equals(dataTableFieldDTO.getId())
                        && t.labelBusinessType == dataTableFieldDTO.getTableBusinessTypeEnum().getValue()).findFirst().orElse(null);
                if (fiDataMetaDataTreeDTO != null) {
                    children.add(fiDataMetaDataTreeDTO);
                }
            }
            if (CollectionUtils.isNotEmpty(children)) {
                fiDataMetaDataDTO.setDataSourceId(dataSourceConfigEnum.getValue());
                fiDataMetaDataDTO.setChildren(children);
                result.add(fiDataMetaDataDTO);
            }
        }
        return result;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 设置Customize树节点规则数量
     * @author dick
     * @date 2022/12/1 13:13
     * @version v1.0
     * @params sourceType 数据源类型
     * @params dto Tree
     * @params tableRules 表规则数量
     */
    public FiDataMetaDataTreeDTO setCustomizeRuleTree(SourceTypeEnum sourceType, FiDataMetaDataTreeDTO dataTree, List<TableRuleCountDTO> tableRules) {
        // 递归获取所有树节点，平铺成列表(不包含表、视图、字段)
        List<TreeRuleTileDTO> treeTile = getTreeTile(sourceType, dataTree);
        // 递归设置表节点规则数量
        HashMap<FiDataMetaDataTreeDTO, List<TreeRuleTileDTO>> map = setTableRuleCount(sourceType, dataTree, tableRules, treeTile);
        FiDataMetaDataTreeDTO treeResult = map.keySet().stream().collect(Collectors.toList()).get(0);
        List<TreeRuleTileDTO> treeTileResult = map.values().stream().collect(Collectors.toList()).get(0);
        // 递归设置各个节点规则数量
        FiDataMetaDataTreeDTO resultTree = setTreeRuleCount(sourceType, treeResult, treeTileResult);
        return resultTree;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 设置FiData树节点规则数量
     * @author dick
     * @date 2022/12/1 13:13
     * @version v1.0
     * @params sourceType 数据源类型
     * @params dto Tree
     * @params tableRules 表规则数量
     */
    public FiDataMetaDataTreeDTO setFiDataRuleTree(SourceTypeEnum sourceType, FiDataMetaDataTreeDTO dto, List<TableRuleCountDTO> tableRules) {
        FiDataMetaDataTreeDTO result = dto;
        // 单个库处理，减少递归次数
        int ruleCount = 0, filterCount = 0, recoveryCount = 0;
        // 统计节点规则后的库
        List<FiDataMetaDataTreeDTO> dataBaseTrees = new ArrayList<>();
        for (FiDataMetaDataTreeDTO dataTree : dto.getChildren()) {
            // 递归获取所有树节点，平铺成列表(不包含表、视图、字段)
            List<TreeRuleTileDTO> treeTile = getTreeTile(sourceType, dataTree);
            // 递归设置表节点规则数量
            HashMap<FiDataMetaDataTreeDTO, List<TreeRuleTileDTO>> map = setTableRuleCount(sourceType, dataTree, tableRules, treeTile);
            FiDataMetaDataTreeDTO treeResult = map.keySet().stream().collect(Collectors.toList()).get(0);
            List<TreeRuleTileDTO> treeTileResult = map.values().stream().collect(Collectors.toList()).get(0);
            // 递归设置表规则父级节点规则数量
            FiDataMetaDataTreeDTO resultTree = setTreeRuleCount(sourceType, treeResult, treeTileResult);
            ruleCount += resultTree.getCheckRuleCount();
            filterCount += resultTree.getFilterRuleCount();
            recoveryCount += resultTree.getRecoveryRuleCount();
            dataBaseTrees.add(resultTree);
        }
        // 计算根节点下规则总数
        result.setCheckRuleCount(ruleCount);
        result.setFilterRuleCount(filterCount);
        result.setRecoveryRuleCount(recoveryCount);
        result.setChildren(dataBaseTrees);
        return result;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 获取所有树节点，平铺成列表(不包含表、视图、字段)
     * @author dick
     * @date 2022/12/2 10:05
     * @version v1.0
     * @params sourceType 数据源类型
     * @params treeDTO Tree
     */
    public List<TreeRuleTileDTO> getTreeTile(SourceTypeEnum sourceType, FiDataMetaDataTreeDTO treeDTO) {
        List<TreeRuleTileDTO> list = new ArrayList<>();
        // 存在节点才递归
        if (CollectionUtils.isNotEmpty(treeDTO.getChildren())) {
            TreeRuleTileDTO tileDTO = new TreeRuleTileDTO();
            if ((treeDTO.getLevelType() == LevelTypeEnum.DATABASE && sourceType == SourceTypeEnum.FiData) ||
                    (treeDTO.getLevelType() == LevelTypeEnum.BASEFOLDER && sourceType == SourceTypeEnum.custom)) {
                tileDTO.setId(treeDTO.getId());
                tileDTO.setName(treeDTO.getLabel());
                tileDTO.setLabelBusinessType(treeDTO.getLabelBusinessType());
                tileDTO.setParentId(treeDTO.getParentId());
                tileDTO.setLevelTypeEnum(treeDTO.getLevelType());
                list.add(tileDTO);
            }
            for (int i = 0; i < treeDTO.getChildren().size(); i++) {
                FiDataMetaDataTreeDTO model = treeDTO.getChildren().get(i);
                if (model.getLevelType() == LevelTypeEnum.TABLE ||
                        model.getLevelType() == LevelTypeEnum.VIEW ||
                        model.getLevelType() == LevelTypeEnum.FIELD) {
                    continue;
                }
                tileDTO = new TreeRuleTileDTO();
                tileDTO.setId(model.getId());
                tileDTO.setName(model.getLabel());
                tileDTO.setLabelBusinessType(model.getLabelBusinessType());
                tileDTO.setParentId(model.getParentId());
                tileDTO.setLevelTypeEnum(model.getLevelType());
                list.add(tileDTO);
                if (CollectionUtils.isNotEmpty(model.getChildren())) {
                    list.addAll(getTreeTile(sourceType, model));
                }
            }
        }
        return list;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 设置表下的规则数量
     * @author dick
     * @date 2022/12/1 13:12
     * @version v1.0
     * @params sourceType 数据源类型
     * @params dto Tree
     * @params tableRules 表规则数量
     * @params treeTiles 平铺的父级列表
     */
    public HashMap<FiDataMetaDataTreeDTO, List<TreeRuleTileDTO>> setTableRuleCount(SourceTypeEnum sourceType, FiDataMetaDataTreeDTO dto, List<TableRuleCountDTO> tableRules, List<TreeRuleTileDTO> treeTiles) {
        if (CollectionUtils.isNotEmpty(dto.getChildren())) {
            for (int i = 0; i < dto.getChildren().size(); i++) {
                FiDataMetaDataTreeDTO dataTreeDTO = dto.getChildren().get(i);
                if (dataTreeDTO.getLevelType() != LevelTypeEnum.TABLE && dataTreeDTO.getLevelType() != LevelTypeEnum.VIEW) {
                    setTableRuleCount(sourceType, dataTreeDTO, tableRules, treeTiles);
                } else {
                    int tableType = 0;
                    if (dataTreeDTO.getLevelType() == LevelTypeEnum.TABLE) {
                        tableType = 1;
                    } else if (dataTreeDTO.getLevelType() == LevelTypeEnum.VIEW) {
                        tableType = 2;
                    }
                    int finalTableType = tableType;
                    // 通过数据源ID+表类型+表业务类型+表ID/表名称 定位到表的规则
                    String tableUnique = "";
                    if (sourceType == SourceTypeEnum.FiData) {
                        tableUnique = dataTreeDTO.getId();
                    } else if (sourceType == SourceTypeEnum.custom) {
                        tableUnique = dataTreeDTO.getLabel();
                    }
                    String finalTableUnique = tableUnique;
                    List<TableRuleCountDTO> ruleList = tableRules.stream().filter(
                            t -> t.getSourceId() == dataTreeDTO.getSourceId()
                                    && t.getTableType() == finalTableType
                                    && t.getTableBusinessType() == dataTreeDTO.getLabelBusinessType()
                                    && t.getTableUnique().equals(finalTableUnique)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(ruleList)) {
                        // 获取校验规则数量
                        TableRuleCountDTO checkRule = ruleList.stream().filter(t -> t.getTableRuleType().equals("校验规则")).findFirst().orElse(null);
                        if (checkRule != null)
                            dataTreeDTO.setCheckRuleCount(checkRule.getTableRuleCount());
                        // 获取清洗规则数量
                        TableRuleCountDTO filterRule = ruleList.stream().filter(t -> t.getTableRuleType().equals("清洗规则")).findFirst().orElse(null);
                        if (filterRule != null)
                            dataTreeDTO.setFilterRuleCount(filterRule.getTableRuleCount());
                        // 获取回收规则数量
                        TableRuleCountDTO recoveryRule = ruleList.stream().filter(t -> t.getTableRuleType().equals("回收规则")).findFirst().orElse(null);
                        if (recoveryRule != null)
                            dataTreeDTO.setRecoveryRuleCount(recoveryRule.getTableRuleCount());
                        // 设置表的父节点规则数量
                        if (CollectionUtils.isNotEmpty(treeTiles) &&
                                (dataTreeDTO.getCheckRuleCount() > 0 || dataTreeDTO.getFilterRuleCount() > 0 || dataTreeDTO.getRecoveryRuleCount() > 0)) {
                            treeTiles = setFolderRuleCount(sourceType, dataTreeDTO.getParentId(), dataTreeDTO.getCheckRuleCount(), dataTreeDTO.getFilterRuleCount(), dataTreeDTO.getRecoveryRuleCount(), treeTiles);
                        }
                    }
                }
            }
        }
        HashMap<FiDataMetaDataTreeDTO, List<TreeRuleTileDTO>> map = new HashMap<>();
        map.put(dto, treeTiles);
        return map;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.dto.dataquality.datasource.TreeRuleTileDTO>
     * @description 设置文件夹下的规则数量
     * @author dick
     * @date 2022/12/2 12:50
     * @version v1.0
     * @params sourceType 数据源类型
     * @params id 父级id
     * @params ruleCount 校验规则数量
     * @params filterCount 清洗规则数量
     * @params recoveryCount 回收规则数量
     * @params treeTiles 平铺的父级列表
     */
    public List<TreeRuleTileDTO> setFolderRuleCount(SourceTypeEnum sourceType, String id, int ruleCount, int filterCount, int recoveryCount, List<TreeRuleTileDTO> treeTiles) {
        // 递归到了根节点，推出递归
        if ((id.equals("-10") && sourceType == SourceTypeEnum.FiData) ||
                (id.equals("-200") && sourceType == SourceTypeEnum.custom)) {
            return treeTiles;
        }
        // 查找节点的父级节点
        List<TreeRuleTileDTO> parentList = treeTiles.stream().filter(t -> t.getId().equals(id)).collect(Collectors.toList());
        // 设置父级节点的规则数量
        if (CollectionUtils.isNotEmpty(parentList)) {
            for (int i = 0; i < parentList.size(); i++) {
                TreeRuleTileDTO tileDTO = parentList.get(i);
                int t_ruleCount = tileDTO.getCheckRuleCount() + ruleCount;
                int t_filterCount = tileDTO.getFilterRuleCount() + filterCount;
                int t_recoveryCount = tileDTO.getRecoveryRuleCount() + recoveryCount;
                tileDTO.setCheckRuleCount(t_ruleCount);
                tileDTO.setFilterRuleCount(t_filterCount);
                tileDTO.setRecoveryRuleCount(t_recoveryCount);
                setFolderRuleCount(sourceType, tileDTO.getParentId(), ruleCount, filterCount, recoveryCount, treeTiles);
            }
        }
        return treeTiles;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 设置Tree各个节点的规则数量
     * @author dick
     * @date 2022/12/2 12:51
     * @version v1.0
     * @params sourceType 数据源类型
     * @params tree 树
     * @params treeTile 平铺的父级列表
     */
    public FiDataMetaDataTreeDTO setTreeRuleCount(SourceTypeEnum sourceType, FiDataMetaDataTreeDTO tree, List<TreeRuleTileDTO> treeTile) {
        // 存在节点和存在规则数量才开始递归设置
        if (CollectionUtils.isNotEmpty(tree.getChildren()) && CollectionUtils.isNotEmpty(treeTile)) {
            TreeRuleTileDTO tileDTO = null;
            if ((tree.getLevelType() == LevelTypeEnum.DATABASE && sourceType == SourceTypeEnum.FiData)
                    || (tree.getLevelType() == LevelTypeEnum.BASEFOLDER && sourceType == SourceTypeEnum.custom)) {
                tileDTO = treeTile.stream().filter(t -> t.getId().equals(tree.getId())).findFirst().orElse(null);
                if (tileDTO != null) {
                    tree.setCheckRuleCount(tileDTO.getCheckRuleCount());
                    tree.setFilterRuleCount(tileDTO.getFilterRuleCount());
                    tree.setRecoveryRuleCount(tileDTO.getRecoveryRuleCount());
                }
            }
            for (int i = 0; i < tree.getChildren().size(); i++) {
                FiDataMetaDataTreeDTO dataTree = tree.getChildren().get(i);
                // 表、视图、字段 直接跳过
                if (dataTree.getLevelType() == LevelTypeEnum.TABLE ||
                        dataTree.getLevelType() == LevelTypeEnum.VIEW ||
                        dataTree.getLevelType() == LevelTypeEnum.FIELD) {
                    continue;
                }
                tileDTO = treeTile.stream().filter(t -> t.getId().equals(dataTree.getId())).findFirst().orElse(null);
                if (tileDTO != null) {
                    dataTree.setCheckRuleCount(tileDTO.getCheckRuleCount());
                    dataTree.setFilterRuleCount(tileDTO.getFilterRuleCount());
                    dataTree.setRecoveryRuleCount(tileDTO.getRecoveryRuleCount());
                }
                setTreeRuleCount(sourceType, dataTree, treeTile);
            }
        }
        return tree;
    }

    /**
     * @return java.sql.Connection
     * @description 创建数据库连接对象
     * @author dick
     * @date 2022/12/1 12:16
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params connectionStr
     * @params account
     * @params password
     */
    public static Connection getStatement(DataSourceTypeEnum dataSourceTypeEnum, String connectionStr, String account, String password) {
        try {
            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
            Connection connection = dbHelper.connection(connectionStr, account,
                    password, dataSourceTypeEnum);
            return connection;
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR);
        }
    }
}
