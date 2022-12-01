package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.SqlConstants;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.governance.BuildGovernanceHelper;
import com.fisk.common.service.dbBEBuild.governance.IBuildGovernanceSqlCommand;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datagovernance.dto.GetConfigDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterApiResultDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Resource
    private GetConfigDTO getConfig;


//    @Value("${dataservice.datasource.metadataentity_key}")
//    private String metaDataEntityKey;

//    @Resource
//    private RedisTemplate redisTemplate;

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
       // List<TableRuleCountDTO> tableRules = baseMapper.getFiDataTableRuleList();
        // 第三步：递归设置Tree-节点规则数量
//        if (CollectionUtils.isNotEmpty(tableRules)) {
//            fiDataMetaDataTreeBase = setFiDataRuleTreeCount(fiDataMetaDataTreeBase, tableRules);
//        }
        return fiDataMetaDataTreeBase;
    }

    @Override
    public FiDataMetaDataTreeDTO getCustomizeMetaData() {
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
     * @description 设置FiData树节点规则数量
     * @author dick
     * @date 2022/12/1 13:13
     * @version v1.0
     * @params dto Tree
     * @params tableRules 表规则数量
     */
    public FiDataMetaDataTreeDTO setFiDataRuleTreeCount(FiDataMetaDataTreeDTO dto, List<TableRuleCountDTO> tableRules) {
        FiDataMetaDataTreeDTO result = dto;
        List<FiDataMetaDataTreeDTO> dataBaseTrees = new ArrayList<>();
        // 数据库ID,声明在最外层避免循环创建实例
        List<String> dataBaseIdList = new ArrayList<>();
        dataBaseIdList.add(String.valueOf(SourceBusinessTypeEnum.DW.getValue()));
        dataBaseIdList.add(String.valueOf(SourceBusinessTypeEnum.ODS.getValue()));
        dataBaseIdList.add(String.valueOf(SourceBusinessTypeEnum.MDM.getValue()));
        dataBaseIdList.add(String.valueOf(SourceBusinessTypeEnum.OLAP.getValue()));
        // 单个库处理，减少递归次数
        int ruleCount = 0, filterCount = 0, recoveryCount = 0;
        // tree规则集合
        List<TreeRuleCountDTO> treeRules = new ArrayList<>();
        for (FiDataMetaDataTreeDTO dataTree : dto.getChildren()) {
            List<TreeRuleCountDTO> dataBaseTreeRules = setFiDataTableRuleCount(dataTree, dataTree, tableRules, treeRules, dataBaseIdList);

        }
        // 计算根节点下规则总数
        result.setCheckRuleCount(ruleCount);
        result.setFilterRuleCount(filterCount);
        result.setRecoveryRuleCount(recoveryCount);
        result.setChildren(dataBaseTrees);
        return result;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 设置表的规则数量
     * @author dick
     * @date 2022/12/1 13:12
     * @version v1.0
     * @params dto Tree
     * @params tableRules 表规则数量
     * @params dataBaseIdList 数据库ID集合
     */
    public List<TreeRuleCountDTO> setFiDataTableRuleCount(FiDataMetaDataTreeDTO dto, FiDataMetaDataTreeDTO oldDto,
                                                          List<TableRuleCountDTO> tableRules, List<TreeRuleCountDTO> treeRules,
                                                          List<String> dataBaseIdList) {
        if (CollectionUtils.isNotEmpty(dto.getChildren())) {
            for (int i = 0; i < dto.getChildren().size(); i++) {
                FiDataMetaDataTreeDTO dataTreeDTO = dto.getChildren().get(i);
                if (dataTreeDTO.getLevelType() != LevelTypeEnum.TABLE) {
                    setFiDataTableRuleCount(dataTreeDTO, oldDto, tableRules, treeRules, dataBaseIdList);
                } else {
                    List<TableRuleCountDTO> ruleList = tableRules.stream().filter(t -> t.getSourceId() == dataTreeDTO.getSourceId() && t.getTableUnique().equals(dataTreeDTO.getId())).collect(Collectors.toList());
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
                        if (dataTreeDTO.getCheckRuleCount() > 0 || dataTreeDTO.getFilterRuleCount() > 0 || dataTreeDTO.getRecoveryRuleCount() > 0) {
                            treeRules = setFiDataFolderRuleCount(dataTreeDTO.getParentId(), dataTreeDTO.getCheckRuleCount(),
                                    dataTreeDTO.getFilterRuleCount(), dataTreeDTO.getRecoveryRuleCount(), oldDto, oldDto, treeRules, dataBaseIdList);
                        }
                    }
                }
            }
        }
        return treeRules;
    }

    /**
     * @return com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO
     * @description 递归设置文件夹下的规则数量
     * @author dick
     * @date 2022/12/1 11:58
     * @version v1.0
     * @params id 初始值为表的父级ID，初始化后为文件夹的父级ID
     * @params ruleCount 校验规则数量
     * @params filterCount 清洗规则数量
     * @params recoveryCount 回收规则数量
     * @params dto Tree
     * @params dataBaseIdList 数据库ID集合
     */
    public List<TreeRuleCountDTO> setFiDataFolderRuleCount(String id, int ruleCount, int filterCount, int recoveryCount,
                                                           FiDataMetaDataTreeDTO dto, FiDataMetaDataTreeDTO oldDto,
                                                           List<TreeRuleCountDTO> treeRuleList, List<String> dataBaseIdList) {
        TreeRuleCountDTO treeRule = new TreeRuleCountDTO();

        // 递归到了库，保存库的规则数量并结束递归
        if (dataBaseIdList.contains(id) && dto.getLevelType() == LevelTypeEnum.DATABASE) {
            treeRule.setId(id);
            treeRule.setLevelTypeEnum(LevelTypeEnum.DATABASE);
            treeRule.setCheckRuleCount(dto.getCheckRuleCount() + ruleCount);
            treeRule.setFilterRuleCount(dto.getFilterRuleCount() + filterCount);
            treeRule.setRecoveryRuleCount(dto.getRecoveryRuleCount() + recoveryCount);
            treeRuleList.add(treeRule);
            return treeRuleList;
        }
        // 递归设置每个节点的父节点的规则数量
        if (CollectionUtils.isNotEmpty(dto.getChildren())) {
            for (int i = 0; i < dto.getChildren().size(); i++) {
                FiDataMetaDataTreeDTO treeDTO = dto.getChildren().get(i);
                String id_temp = id;
                if (treeDTO.getId().equals(id)) {
                    // 保存规则数量
                    treeRule.setId(id);
                    treeRule.setLevelTypeEnum(dto.getLevelType());
                    treeRule.setCheckRuleCount(ruleCount);
                    treeRule.setFilterRuleCount(filterCount);
                    treeRule.setRecoveryRuleCount(recoveryCount);
                    treeRuleList.add(treeRule);
                    id_temp = dto.getParentId();
                    treeDTO = oldDto;
                }
                if (CollectionUtils.isNotEmpty(treeDTO.getChildren())) {
                    setFiDataFolderRuleCount(id_temp, ruleCount, filterCount, recoveryCount, treeDTO, oldDto, treeRuleList, dataBaseIdList);
                }
            }
        }
        return treeRuleList;
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
