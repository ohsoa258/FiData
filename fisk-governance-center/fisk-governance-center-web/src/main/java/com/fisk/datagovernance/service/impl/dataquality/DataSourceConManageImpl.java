package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.dbMetaData.utils.DorisConUtils;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.access.OdsFieldQueryDTO;
import com.fisk.datagovernance.dto.dataops.DataObsSqlDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataops.DataObsSqlPO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataops.DataObsSqlMap;
import com.fisk.datagovernance.map.dataquality.DataSourceConMap;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataops.DataObsSqlService;
import com.fisk.datagovernance.service.dataquality.IDataSourceConManageService;
import com.fisk.datagovernance.service.dataquality.IDatacheckStandardsGroupService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.datasource.ExportResultVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.DwFieldQueryDTO;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
@Slf4j
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManageService {

    @Resource
    private DataSourceConMapper mapper;

    @Resource
    private UserClient userClient;

    @Resource
    private UserHelper userHelper;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    private DataModelClient dataModelClient;

    @Resource
    private MdmClient mdmClient;

    @Resource
    private RedisUtil redisUtil;

    @Value("${dataquality.metadataentity_key}")
    private String metaDataEntityKey;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private DataObsSqlService dataObsSqlService;

    @Value("${checkStandards}")
    private Boolean checkStandards;

    @Resource
    private DataManageClient dataManageClient;

    @Resource
    IDatacheckStandardsGroupService standardsGroupService;

    @Override
    public Page<DataSourceConVO> page(DataSourceConQuery query) {
        Page<DataSourceConVO> pageDTO = new Page<>();
        List<DataSourceConVO> allDataSource = getAllDataSource();
        pageDTO.setRecords(allDataSource);
        return pageDTO;
    }

    @Override
    public ResultEnum add(DataSourceConDTO dto) {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getDatasourceId, dto.getDatasourceId())
                .eq(DataSourceConPO::getDelFlag, 1);
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS;
        }
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        model.setCreateTime(LocalDateTime.now());
        model.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int insert = mapper.insert(model);
        if (insert <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum edit(DataSourceConEditDTO dto) {
        DataSourceConPO model = mapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getDatasourceId, dto.getDatasourceId())
                .eq(DataSourceConPO::getDelFlag, 1)
                .ne(DataSourceConPO::getId, dto.getId());
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
            switch (dto.getConType()) {
                case MYSQL:
                    Class.forName(DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.getConStr(), dto.getConAccount(), dto.getConPassword());
                    return ResultEnum.SUCCESS;
                case SQLSERVER:
                    Class.forName(DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.getConStr(), dto.getConAccount(), dto.getConPassword());
                    return ResultEnum.SUCCESS;
                case POSTGRESQL:
                    Class.forName(DataSourceTypeEnum.POSTGRESQL.getDriverName());
                    conn = DriverManager.getConnection(dto.getConStr(), dto.getConAccount(), dto.getConPassword());
                    return ResultEnum.SUCCESS;
                case DORIS:
                    Class.forName(DataSourceTypeEnum.DORIS.getDriverName());
                    conn = DriverManager.getConnection(dto.getConStr(), dto.getConAccount(), dto.getConPassword());
                    return ResultEnum.SUCCESS;
                case ORACLE:
                    Class.forName(DataSourceTypeEnum.ORACLE.getDriverName());
                    conn = DriverManager.getConnection(dto.getConStr(), dto.getConAccount(), dto.getConPassword());
                    return ResultEnum.SUCCESS;
                default:
                    return ResultEnum.DS_DATASOURCE_CON_WARN;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            log.error("测试连接异常：" + e);
            return ResultEnum.DATASOURCE_CONNECTERROR;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DATASOURCE_CONNECTCLOSEERROR);
            }
        }
    }

    @Override
    public DataQualityDataSourceTreeDTO getFiDataConfigMetaData() {
        // 第一步：查询数据质量模块引用了那些平台数据源
        List<DataSourceConVO> dataSourceConVOList = getFiDataDataSource();
        if (CollectionUtils.isEmpty(dataSourceConVOList)) {
            return null;
        }

        // 第二步：根据平台数据源调用不同模块的Tree接口
        DataQualityDataSourceTreeDTO fiDataMetaDataTree_Basics = new DataQualityDataSourceTreeDTO();
        fiDataMetaDataTree_Basics.setId("-10");
        fiDataMetaDataTree_Basics.setParentId("-100");
        fiDataMetaDataTree_Basics.setLabel("FiData");
        fiDataMetaDataTree_Basics.setLabelAlias("FiData");
        fiDataMetaDataTree_Basics.setLabelRelName("FiData");
        fiDataMetaDataTree_Basics.setSourceType(SourceTypeEnum.FiData.getValue());
        fiDataMetaDataTree_Basics.setLevelType(LevelTypeEnum.BASEFOLDER);

        List<DataQualityDataSourceTreeDTO> fiDataMetaDataTree_DataBaseList = new ArrayList<>();
        ResultEntity<DataQualityDataSourceTreeDTO> treeResult = null;

        dataSourceConVOList = dataSourceConVOList.stream().sorted(Comparator.comparing(DataSourceConVO::getDatasourceId)).collect(Collectors.toList());
        for (DataSourceConVO dataSourceConVO : dataSourceConVOList) {
            DataQualityDataSourceTreeDTO fiDataMetaDataTree_DataBase = null;
            switch (dataSourceConVO.getSourceBusinessType()) {
                case DW:
                    treeResult = dataModelClient.dataQuality_GetDwFolderTableTree();
                    break;
                case ODS:
                    treeResult = dataAccessClient.dataQuality_GetOdsFolderTableTree();
                    break;
                case MDM:
                    treeResult = mdmClient.dataQuality_GetMdmFolderTableTree();
                    break;
            }
            if (treeResult != null && treeResult.getData() != null) {
                fiDataMetaDataTree_DataBase = treeResult.getData();
                fiDataMetaDataTree_DataBaseList.add(fiDataMetaDataTree_DataBase);
            } else {
                log.warn("【getFiDataConfigMetaData】平台数据源返回树状结构为空：" + dataSourceConVO.getSourceBusinessType().getName());
            }
        }

        // 第三步：获取数据标准Tree结构并写入到FiData节点下
        if (checkStandards) {
            treeResult = dataManageClient.dataQuality_GetAllStandardsTree();
            if (treeResult != null && treeResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                fiDataMetaDataTree_DataBaseList.add(treeResult.getData());
            }
        }

        fiDataMetaDataTree_Basics.setChildren(fiDataMetaDataTree_DataBaseList);
        return fiDataMetaDataTree_Basics;
    }

    @Override
    public DataQualityDataSourceTreeDTO getCustomizeMetaData() {
        // 第一步：查询数据质量模块引用了那些自定义数据源
        List<DataSourceConVO> dataSourceConVOList = getCustomizeDataSource();
        if (CollectionUtils.isEmpty(dataSourceConVOList)) {
            return null;
        }

        // 第二步：根据自定义数据源调用不同模块的Tree接口
        DataQualityDataSourceTreeDTO customizeMetaDataTree_Basics = new DataQualityDataSourceTreeDTO();
        customizeMetaDataTree_Basics.setId("-20");
        customizeMetaDataTree_Basics.setParentId("-200");
        customizeMetaDataTree_Basics.setLabel("Customize");
        customizeMetaDataTree_Basics.setLabelAlias("Customize");
        customizeMetaDataTree_Basics.setLabelRelName("Customize");
        customizeMetaDataTree_Basics.setSourceType(SourceTypeEnum.custom.getValue());
        customizeMetaDataTree_Basics.setLevelType(LevelTypeEnum.BASEFOLDER);

        // 第三步：以数据库IP为维度统计IP下的数据库
        List<DataQualityDataSourceTreeDTO> customizeMetaDataTree_Ips = new ArrayList<>();
        List<String> conIp = dataSourceConVOList.stream().map(t -> t.getConIp()).distinct().collect(Collectors.toList());
        for (String ip : conIp) {
            String uuid_Ip = UUID.randomUUID().toString().replace("-", "");
            DataQualityDataSourceTreeDTO customizeMetaDataTree_Ip = new DataQualityDataSourceTreeDTO();
            customizeMetaDataTree_Ip.setId(uuid_Ip);
            customizeMetaDataTree_Ip.setParentId("-20");
            customizeMetaDataTree_Ip.setLabel(ip);
            customizeMetaDataTree_Ip.setLabelAlias(ip);
            customizeMetaDataTree_Ip.setLabelRelName(ip);
            customizeMetaDataTree_Ip.setSourceType(SourceTypeEnum.custom.getValue());
            customizeMetaDataTree_Ip.setLevelType(LevelTypeEnum.FOLDER);

            // 第四步：以数据库为维度统计数据库下的表
            List<DataQualityDataSourceTreeDTO> customizeMetaDataTree_Ip_DataBases = new ArrayList<>();
            List<DataSourceConVO> dataSourceConVOS = dataSourceConVOList.stream().filter(t -> t.getConIp().equals(ip)).collect(Collectors.toList());
            for (DataSourceConVO dataSourceConVO : dataSourceConVOS) {
                DataQualityDataSourceTreeDTO customizeMetaDataTree_DataBase = new DataQualityDataSourceTreeDTO();
                String uuid_DataBase = UUID.randomUUID().toString().replace("-", "");
                customizeMetaDataTree_DataBase.setId(uuid_DataBase);
                customizeMetaDataTree_DataBase.setParentId(uuid_Ip);
                customizeMetaDataTree_DataBase.setLabel(dataSourceConVO.getName());
                customizeMetaDataTree_DataBase.setLabelAlias(dataSourceConVO.getName());
                customizeMetaDataTree_DataBase.setLabelRelName(dataSourceConVO.getName());
                customizeMetaDataTree_DataBase.setSourceId(dataSourceConVO.getDatasourceId());
                customizeMetaDataTree_DataBase.setSourceType(SourceTypeEnum.custom.getValue());
                customizeMetaDataTree_DataBase.setLevelType(LevelTypeEnum.DATABASE);
                customizeMetaDataTree_DataBase.setChildren(getCustomizeMetaData_Table(dataSourceConVO, uuid_DataBase));
                customizeMetaDataTree_Ip_DataBases.add(customizeMetaDataTree_DataBase);
            }
            customizeMetaDataTree_Ip.setChildren(customizeMetaDataTree_Ip_DataBases);
            customizeMetaDataTree_Ips.add(customizeMetaDataTree_Ip);
        }
        customizeMetaDataTree_Basics.setChildren(customizeMetaDataTree_Ips);
        return customizeMetaDataTree_Basics;
    }

    @Override
    public ResultEntity<List<DataQualityDataSourceTreeDTO>> getTableFieldByTableId(QueryTableFieldDTO dto) {
        List<DataQualityDataSourceTreeDTO> tableFieldMetaDataTreeList = new ArrayList<>();
        // 数据源信息
        DataSourceConVO dataSourceConVO = getDataSourceByDataSourceId(dto.getSourceId());
        if (dto == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL, tableFieldMetaDataTreeList);
        }
        if (dataSourceConVO == null) {
            return ResultEntityBuild.build(ResultEnum.DATASOURCE_INFORMATION_ISNULL, tableFieldMetaDataTreeList);
        }
        log.info("【getTableFieldByTableId】请求参数[{}]", JSONObject.toJSON(dto));

        ResultEntity<List<DataQualityDataSourceTreeDTO>> treeResult = null;
        // 判断点击的表属于FiData还是自定义
        if (dto.getSourceType() == SourceTypeEnum.FiData.getValue()) {
            // 平台数据源
            switch (dataSourceConVO.getSourceBusinessType()) {
                case DW:
                    DwFieldQueryDTO queryDTO_Dw = new DwFieldQueryDTO();
                    queryDTO_Dw.setTblId(Integer.valueOf(dto.getId()));
                    queryDTO_Dw.setTblType(dto.getLabelBusinessType());
                    queryDTO_Dw.setPublishState(dto.getPublishState());
                    treeResult = dataModelClient.dataQuality_GetDwTableFieldByTableId(queryDTO_Dw);
                    break;
                case ODS:
                    OdsFieldQueryDTO queryDTO_Ods = new OdsFieldQueryDTO();
                    queryDTO_Ods.setTblId(dto.getId());
                    queryDTO_Ods.setTblType(dto.getLabelBusinessType());
                    queryDTO_Ods.setPublishState(dto.getPublishState());
                    queryDTO_Ods.setDbId(dto.getSourceId());

                    if (dto.getLabelBusinessType() == TableBusinessTypeEnum.DORIS_CATALOG_TABLE.getValue() &&
                            StringUtils.isNotEmpty(dto.getLabelFramework())) {
                        List<String> list = Arrays.asList(dto.getLabelFramework().split("\\."));
                        if (CollectionUtils.isNotEmpty(list) && list.size() == 2) {
                            queryDTO_Ods.setCatalogName(list.get(0));
                            queryDTO_Ods.setDbName(list.get(1));
                        }
                    }
                    queryDTO_Ods.setTblName(dto.getLabelRelName());
                    treeResult = dataAccessClient.dataQuality_GetOdsTableFieldByTableId(queryDTO_Ods);
                    break;
                case MDM:
                    treeResult = mdmClient.dataQuality_GetMdmTableFieldByTableId(dto.getId());
                    break;
            }
            if (treeResult != null && treeResult.getData() != null) {
                tableFieldMetaDataTreeList = treeResult.getData();
            } else {
                log.warn("【getTableFieldByTableId】平台数据源返回表字段结构为空：" + dataSourceConVO.getSourceBusinessType().getName());
            }
        } else {
            // 自定义数据源
            tableFieldMetaDataTreeList = getCustomizeMetaData_TableField(dataSourceConVO, dto.getLabelFramework(), dto.getLabelRelName(), dto.getId());
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableFieldMetaDataTreeList);
    }

    @Override
    public ResultEnum reloadDataSource(int id) {
        try {
            List<DataSourceConVO> allDataSource = getAllDataSource();
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                return ResultEnum.DS_DATASOURCE_NOTEXISTS;
            }
            SourceTypeEnum datasourceType = dataSourceConVO.getDatasourceType();
            switch (datasourceType) {
                case FiData:
                    FiDataMetaDataReqDTO reqDTO = new FiDataMetaDataReqDTO();
                    reqDTO.setDataSourceId(String.valueOf(dataSourceConVO.getDatasourceId()));
                    reqDTO.setDataSourceName(dataSourceConVO.getConDbname());
                    switch (dataSourceConVO.getSourceBusinessType()) {
                        case DW:
                        case OLAP:
                            dataModelClient.setDataModelStructure(reqDTO);
                            break;
                        case ODS:
                            dataAccessClient.setDataAccessStructure(reqDTO);
                            break;
                        case MDM:
                            mdmClient.setMDMDataStructure(reqDTO);
                            break;
                    }
                    break;
                case custom:
                    //setMetaDataToRedis(dataSourceConVO.getId(), 2);
                    break;
            }
        } catch (Exception ex) {
            log.info("【reloadDataSource】刷新数据源失败：" + ex);
            return ResultEnum.DS_DATA_SOURCE_REFRESH_FAILED;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public void exportData(ExportResultVO vo, HttpServletResponse response) {
        exportExcel(vo, response);
    }

    @Override
    public List<DataObsSqlDTO> getObsSqlByUser() {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        Long userId = loginUserInfo.getId();
        LambdaQueryWrapper<DataObsSqlPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataObsSqlPO::getDelFlag, 1);
        queryWrapper.eq(DataObsSqlPO::getCreateUser, userId);
        List<DataObsSqlPO> list = dataObsSqlService.list(queryWrapper);
        List<DataObsSqlDTO> dataObsSqlDTOS = DataObsSqlMap.INSTANCES.poListToDtoList(list);
        return dataObsSqlDTOS;
    }

    @Override
    public ResultEnum saveOrUpdateObsSql(List<DataObsSqlDTO> list) {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        Long userId = loginUserInfo.getId();
        LambdaQueryWrapper<DataObsSqlPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataObsSqlPO::getCreateUser, userId);
        List<DataObsSqlPO> poList = dataObsSqlService.list(queryWrapper);
        List<Long> ObsSqlPoId = poList.stream().map(BasePO::getId).collect(Collectors.toList());
        List<DataObsSqlPO> dataObsSqlPOS = DataObsSqlMap.INSTANCES.dtoListToPoList(list);

        //找出待删除数据id
        List<Long> Ids = dataObsSqlPOS.stream().map(DataObsSqlPO::getId).collect(Collectors.toList());
        List<Long> dels = ObsSqlPoId.stream().filter(i -> !Ids.contains(i)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dels)) {
            dataObsSqlService.removeByIds(dels);
        }
        //找出待添加数据
        List<DataObsSqlPO> adds = dataObsSqlPOS.stream().filter(i -> i.getId() == 0).collect(Collectors.toList());
        //找出待修改数据
        List<DataObsSqlPO> updates = dataObsSqlPOS.stream().filter(i -> i.getId() != 0).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(adds)) {
            dataObsSqlService.saveBatch(adds);
        }
        if (CollectionUtils.isNotEmpty(updates)) {
            dataObsSqlService.updateBatchById(updates);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @description 查找Tree节点下的表、视图的ID
     * @author dick
     * @date 2022/12/2 14:59
     * @version v1.0
     * @params sourceType 数据源类型
     * @params id 节点ID
     */
    public List<QueryTableRuleDTO> getTreeTableNode(SourceTypeEnum sourceType, String id) {
        List<QueryTableRuleDTO> list = new ArrayList<>();
        DataQualityDataSourceTreeDTO tree = new DataQualityDataSourceTreeDTO();
        tree.children = new ArrayList<>();
        if (sourceType == SourceTypeEnum.FiData) {
            DataQualityDataSourceTreeDTO fiDataConfigMetaData = getFiDataConfigMetaData();
            if (fiDataConfigMetaData != null) {
                tree.children.add(fiDataConfigMetaData);
            }
        } else if (sourceType == SourceTypeEnum.custom) {
            DataQualityDataSourceTreeDTO customizeMetaData = getCustomizeMetaData();
            if (customizeMetaData != null) {
                tree.children.add(customizeMetaData);
            }
        }
        if (CollectionUtils.isNotEmpty(tree.children)) {
            // 递归获取选择的节点
            List<DataQualityDataSourceTreeDTO> treeFolderNodes = getTreeFolderNode(tree);
            DataQualityDataSourceTreeDTO treeFolderNode = null;
            if (CollectionUtils.isNotEmpty(treeFolderNodes)) {
                treeFolderNode = treeFolderNodes.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
            }
            if (treeFolderNode != null && CollectionUtils.isNotEmpty(treeFolderNode.getChildren())) {
                list = getTreeTableNode(sourceType, treeFolderNode);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                // 根据DictKey去重表信息
                list = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(QueryTableRuleDTO::getDictKey))), ArrayList::new));
            }
        }
        return list;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @description 查询单个节点下的结构
     * @author dick
     * @date 2022/12/2 15:51
     * @version v1.0
     * @params tree
     * @params id
     */
    public List<DataQualityDataSourceTreeDTO> getTreeFolderNode(DataQualityDataSourceTreeDTO treeDTO) {
        List<DataQualityDataSourceTreeDTO> list = new ArrayList<>();
        // 存在节点才递归
        if (CollectionUtils.isNotEmpty(treeDTO.getChildren())) {
            for (int i = 0; i < treeDTO.getChildren().size(); i++) {
                DataQualityDataSourceTreeDTO model = treeDTO.getChildren().get(i);
                if (model.getLevelType() == LevelTypeEnum.TABLE ||
                        model.getLevelType() == LevelTypeEnum.VIEW ||
                        model.getLevelType() == LevelTypeEnum.FIELD) {
                    continue;
                }
                list.add(model);
                if (CollectionUtils.isNotEmpty(model.getChildren())) {
                    list.addAll(getTreeFolderNode(model));
                }
            }
        }
        return list;
    }

    /**
     * @return java.util.List<java.lang.String>
     * @description 查找Tree节点下的表、视图信息
     * @author dick
     * @date 2022/12/2 16:17
     * @version v1.0
     * @params sourceType 数据源类型
     * @params treeNode 树节点
     */
    public List<QueryTableRuleDTO> getTreeTableNode(SourceTypeEnum sourceType, DataQualityDataSourceTreeDTO treeNode) {
        List<QueryTableRuleDTO> list = new ArrayList<>();
        for (int i = 0; i < treeNode.getChildren().size(); i++) {
            DataQualityDataSourceTreeDTO tree = treeNode.getChildren().get(i);
            if (tree.getLevelType() == LevelTypeEnum.TABLE || tree.getLevelType() == LevelTypeEnum.VIEW) {
                QueryTableRuleDTO model = new QueryTableRuleDTO();
                if (sourceType == SourceTypeEnum.FiData) {
                    model.setId(tree.getId());
                } else if (sourceType == SourceTypeEnum.custom) {
                    model.setId(tree.getLabel());
                }
                model.setName(tree.getLabel());
                model.setTableType(tree.getLevelType());
                model.setTableBusinessType(TableBusinessTypeEnum.getEnum(tree.getLabelBusinessType()));
                model.setSourceId(tree.getSourceId());
                model.setSourceType(SourceTypeEnum.getEnum(tree.getSourceType()));
                // 因为DW下面有公共维度的文件夹，公共维度下的表都是一样的，所以此处加个Key用来过滤重复的表
                String dictKey = tree.getId() + tree.getLabel() + tree.getLevelType() +
                        tree.getLabelBusinessType() + tree.getSourceId() + tree.getSourceType();
                model.setDictKey(dictKey);
                list.add(model);
            }
            if (CollectionUtils.isNotEmpty(tree.getChildren())) {
                list.addAll(getTreeTableNode(sourceType, tree));
            }
        }
        return list;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 查询自定义数据源下的表信息
     * @author dick
     * @date 2024/8/29 12:17
     * @version v1.0
     * @params conPo
     */
    public List<DataQualityDataSourceTreeDTO> getCustomizeMetaData_Table(DataSourceConVO dataSourceConVO, String uuid_DataBase) {
        List<DataQualityDataSourceTreeDTO> customizeMetaDataTree_Tables = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        DorisConUtils dorisConUtils = new DorisConUtils();
        try {
            List<TablePyhNameDTO> tableNames = null;
            switch (dataSourceConVO.getConType()) {
                case MYSQL:
                    tableNames = mysqlConUtils.getTableNames(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.MYSQL);
                    break;
                case SQLSERVER:
                    tableNames = sqlServerPlusUtils.getTableNames(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.SQLSERVER);
                    break;
                case POSTGRESQL:
                    tableNames = postgresConUtils.getTableNames(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.POSTGRESQL);
                    break;
                case DORIS:
                    tableNames = dorisConUtils.getTableNames(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.DORIS);
                    break;
            }
            if (CollectionUtils.isNotEmpty(tableNames)) {
                for (TablePyhNameDTO table : tableNames) {
                    String uuid_TableId = UUID.randomUUID().toString().replace("-", "");
                    DataQualityDataSourceTreeDTO customizeMetaMetaDataTree_Table = new DataQualityDataSourceTreeDTO();
                    customizeMetaMetaDataTree_Table.setId(uuid_TableId);
                    customizeMetaMetaDataTree_Table.setParentId(uuid_DataBase);
                    customizeMetaMetaDataTree_Table.setLabel(table.getTableFullName());
                    customizeMetaMetaDataTree_Table.setLabelAlias(table.getTableFullName());
                    customizeMetaMetaDataTree_Table.setLabelRelName(table.getTableName());
                    customizeMetaMetaDataTree_Table.setLabelFramework(table.getTableFramework());
                    customizeMetaMetaDataTree_Table.setSourceId(dataSourceConVO.getDatasourceId());
                    customizeMetaMetaDataTree_Table.setSourceType(SourceTypeEnum.custom.getValue());
                    customizeMetaMetaDataTree_Table.setLevelType(LevelTypeEnum.TABLE);
                    customizeMetaDataTree_Tables.add(customizeMetaMetaDataTree_Table);
                }
            }
        } catch (Exception ex) {
            log.error("【getCustomizeMetaData_Table】获取自定义数据源的表信息时触发系统异常：", ex);
        }
        return customizeMetaDataTree_Tables;
    }

    /**
     * @return java.util.List<com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO>
     * @description 查询自定义数据源下的表字段信息
     * @author dick
     * @date 2024/8/29 12:17
     * @version v1.0
     * @params conPo
     */
    public List<DataQualityDataSourceTreeDTO> getCustomizeMetaData_TableField(DataSourceConVO dataSourceConVO
            , String tableName, String tableFramework, String tableId) {
        List<DataQualityDataSourceTreeDTO> customizeMetaDataTree_Fields = new ArrayList<>();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        DorisConUtils dorisConUtils = new DorisConUtils();
        String tableFullName = StringUtils.isNotEmpty(tableFramework) ? tableFramework + "." + tableName : tableName;
        try {
            List<TableStructureDTO> fieldNames = null;
            switch (dataSourceConVO.getConType()) {
                case MYSQL:
                    fieldNames = mysqlConUtils.getTableColumns(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.MYSQL, tableFullName);
                    break;
                case SQLSERVER:
                    fieldNames = sqlServerPlusUtils.getTableColumns(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.SQLSERVER, tableName, tableFramework);
                    break;
                case POSTGRESQL:
                    fieldNames = postgresConUtils.getTableColumns(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.POSTGRESQL, tableFullName);
                    break;
                case DORIS:
                    fieldNames = dorisConUtils.getTableColumns(dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword(), DataSourceTypeEnum.DORIS, tableFullName);
                    break;
            }
            if (CollectionUtils.isNotEmpty(fieldNames)) {
                for (TableStructureDTO field : fieldNames) {
                    String uuid_FieldId = UUID.randomUUID().toString().replace("-", "");
                    DataQualityDataSourceTreeDTO customizeMetaDataTree_Field = new DataQualityDataSourceTreeDTO();
                    customizeMetaDataTree_Field.setId(uuid_FieldId);
                    customizeMetaDataTree_Field.setParentId(tableId);
                    customizeMetaDataTree_Field.setLabel(field.getFieldName());
                    customizeMetaDataTree_Field.setLabelAlias(field.getFieldName());
                    customizeMetaDataTree_Field.setLabelType(field.getFieldType());
                    customizeMetaDataTree_Field.setLabelLength(String.valueOf(field.getFieldLength()));
                    customizeMetaDataTree_Field.setLabelDesc(field.getFieldDes());
                    customizeMetaDataTree_Field.setSourceId(dataSourceConVO.getDatasourceId());
                    customizeMetaDataTree_Field.setSourceType(SourceTypeEnum.custom.getValue());
                    customizeMetaDataTree_Field.setLevelType(LevelTypeEnum.FIELD);
                    customizeMetaDataTree_Fields.add(customizeMetaDataTree_Field);
                }
            }
        } catch (Exception ex) {
            log.error("【getCustomizeMetaData_TableField】获取自定义数据源的表字段信息时触发系统异常：", ex);
        }
        return customizeMetaDataTree_Fields;
    }

    /**
     * @return com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO
     * @description 根据平台数据源ID获取数据源信息
     * @author dick
     * @date 2024/9/2 11:16
     * @version v1.0
     * @params dataSourceId
     */
    public DataSourceConVO getDataSourceByDataSourceId(int dataSourceId) {
        List<DataSourceConVO> allDataSource = getAllDataSource();
        DataSourceConVO dataSourceConVO = null;
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            dataSourceConVO = allDataSource.stream().filter(t -> t.getDatasourceId() == dataSourceId).findFirst().orElse(null);
        }
        return dataSourceConVO;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @description 获取引用的FiData数据源信息
     * @author dick
     * @date 2024/9/2 11:17
     * @version v1.0
     * @params
     */
    public List<DataSourceConVO> getFiDataDataSource() {
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            allDataSource = allDataSource.stream().filter(t -> t.getDatasourceType().getValue() == SourceTypeEnum.FiData.getValue()).collect(Collectors.toList());
        }
        return allDataSource;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @description 获取引用的自定义数据源信息
     * @author dick
     * @date 2024/9/2 11:17
     * @version v1.0
     * @params
     */
    public List<DataSourceConVO> getCustomizeDataSource() {
        List<DataSourceConVO> allDataSource = getAllDataSource();
        if (CollectionUtils.isNotEmpty(allDataSource)) {
            allDataSource = allDataSource.stream().filter(t -> t.getDatasourceType().getValue() == SourceTypeEnum.custom.getValue()).collect(Collectors.toList());
        }
        return allDataSource;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO>
     * @description 获取引用的全部数据源信息
     * @author dick
     * @date 2024/9/2 11:17
     * @version v1.0
     * @params
     */
    public List<DataSourceConVO> getAllDataSource() {
        List<DataSourceConVO> dataSourceList = new ArrayList<>();

        // 系统管理中所有数据源
        ResultEntity<List<DataSourceDTO>> systemDataSourceResult = userClient.getAll();
        List<DataSourceDTO> systemDataSources = systemDataSourceResult != null && systemDataSourceResult.getCode() == 0 ? systemDataSourceResult.getData() : null;

        // 数据质量数据源信息
        QueryWrapper<DataSourceConPO> dataSourceConPOQueryWrapper = new QueryWrapper<>();
        dataSourceConPOQueryWrapper.lambda()
                .eq(DataSourceConPO::getDelFlag, 1);
        List<DataSourceConPO> dataSourceConPOList = baseMapper.selectList(dataSourceConPOQueryWrapper);
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 组装数据源具体字段信息
        if (CollectionUtils.isNotEmpty(dataSourceConPOList) && CollectionUtils.isNotEmpty(systemDataSources)) {
            dataSourceConPOList.forEach(t -> {
                DataSourceConVO dataSourceConVO = new DataSourceConVO();
                dataSourceConVO.setId(Math.toIntExact(t.getId()));
                dataSourceConVO.setDatasourceType(SourceTypeEnum.getEnum(t.getDatasourceType()));
                dataSourceConVO.setDatasourceId(t.getDatasourceId());
                dataSourceConVO.setCreateTime(t.getCreateTime().format(pattern));
                Optional<DataSourceDTO> first = systemDataSources.stream().filter(item -> item.getId() == t.getDatasourceId()).findFirst();
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
                    dataSourceConVO.setSourceBusinessType(dataSourceDTO.getSourceBusinessType());
                    dataSourceList.add(dataSourceConVO);
                }
            });
        }

        return dataSourceList;
    }

    /**
     * 导出Excel
     *
     * @param vo
     * @param response
     * @return
     */
    public ResultEnum exportExcel(ExportResultVO vo, HttpServletResponse response) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");
        XSSFRow row1 = sheet.createRow(0);
        if (CollectionUtils.isEmpty(vo.getHeaderList())) {
            ResultEntityBuild.build(ResultEnum.CODE_NOT_EXIST);
        }
        //添加表头
        for (int i = 0; i < vo.getHeaderList().size(); i++) {
            row1.createCell(i).setCellValue(vo.getHeaderList().get(i));
        }
        if (!CollectionUtils.isEmpty(vo.getDataArray())) {
            for (int i = 0; i < vo.getDataArray().size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                Map<String, Object> jsonObject = vo.getDataArray().get(i);
                for (int j = 0; j < vo.getHeaderList().size(); j++) {
                    row.createCell(j).setCellValue(jsonObject.get(vo.getHeaderList().get(j)) == null ? "" : jsonObject.get(vo.getHeaderList().get(j)).toString());
                }
            }
        }
        //将文件存到指定位置
        try {
            //输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment;filename=" + vo.getFileName() + ".xlsx");
            workbook.write(output);
            output.close();
        } catch (Exception e) {
            log.error("export excel error:", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return ResultEnum.SUCCESS;
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
