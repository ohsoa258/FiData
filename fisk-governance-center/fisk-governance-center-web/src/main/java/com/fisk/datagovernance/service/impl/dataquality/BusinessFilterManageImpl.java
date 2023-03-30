package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessTaskDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.QueryTableRuleDTO;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.BusinessFilterMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterMapper;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/23 12:56
 */
@Service
@Slf4j
public class BusinessFilterManageImpl extends ServiceImpl<BusinessFilterMapper, BusinessFilterPO> implements IBusinessFilterManageService {

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private BusinessFilterManageImpl businessFilterManageImpl;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private ExternalInterfaceImpl externalInterfaceImpl;

    @Resource
    private BusinessFilterApiManageImpl businessFilterApiManageImpl;

    @Override
    public List<BusinessFilterVO> getAllRule(BusinessFilterQueryDTO query) {
        // 第一步：参数验证
        List<BusinessFilterVO> filterRule = new ArrayList<>();
        if (query == null) {
            return filterRule;
        }
        try {
            // 第二步：查询某个节点下的表信息，没选择节点默认查询所有规则
            List<QueryTableRuleDTO> queryTableParams = new ArrayList<>();
            if (query.getLevelType() == LevelTypeEnum.TABLE || query.getLevelType() == LevelTypeEnum.VIEW) {
                QueryTableRuleDTO queryTableParam = new QueryTableRuleDTO();
                queryTableParam.setId(query.getUniqueId());
                queryTableParam.setTableType(query.getLevelType());
                queryTableParam.setTableBusinessType(query.getTableBusinessType());
                queryTableParam.setSourceId(query.getDatasourceId());
                queryTableParam.setSourceType(query.getSourceTypeEnum());
                queryTableParams.add(queryTableParam);
            } else if (query.getLevelType() == LevelTypeEnum.BASEFOLDER || query.getLevelType() == LevelTypeEnum.DATABASE || query.getLevelType() == LevelTypeEnum.FOLDER) {
                List<QueryTableRuleDTO> treeTableNodes = dataSourceConManageImpl.getTreeTableNode_main(query.sourceTypeEnum, query.getUniqueId());
                if (CollectionUtils.isNotEmpty(treeTableNodes)) {
                    queryTableParams.addAll(treeTableNodes);
                }
            }
            // 第三步：获取所有表规则
            List<BusinessFilterVO> allRule = baseMapper.getAllRule();
            if (CollectionUtils.isEmpty(allRule)) {
                return filterRule;
            }
            // 第四步：筛选满足条件的表/视图的规则
            if (CollectionUtils.isNotEmpty(queryTableParams)) {
                for (QueryTableRuleDTO dto : queryTableParams) {
                    List<BusinessFilterVO> rules = null;
                    int tableType = 0;
                    if (dto.getTableType() == LevelTypeEnum.TABLE) {
                        tableType = 1;
                    } else if (dto.getTableType() == LevelTypeEnum.VIEW) {
                        tableType = 2;
                    }
                    int finalTableType = tableType;
                    if (dto.getSourceType() == SourceTypeEnum.FiData) {
                        // 通过数据源ID+表类型+表业务类型+表ID 定位到表的规则
                        rules = allRule.stream().filter(t -> t.getFiDataSourceId() == dto.getSourceId() &&
                                t.getTableType() == finalTableType &&
                                t.getTableBusinessType() == dto.getTableBusinessType() &&
                                t.getTableUnique().equals(dto.getId())).collect(Collectors.toList());
                    } else if (dto.getSourceType() == SourceTypeEnum.custom) {
                        // 通过数据源ID+表类型+表业务类型+表名称 定位到表的规则
                        rules = allRule.stream().filter(t -> t.getDatasourceId() == dto.getSourceId() &&
                                t.getTableType() == finalTableType &&
                                t.getTableBusinessType() == dto.getTableBusinessType() &&
                                t.getTableUnique().equals(dto.getId())).collect(Collectors.toList());
                    }
                    if (CollectionUtils.isNotEmpty(rules)) {
                        filterRule.addAll(rules);
                    }
                }
            } else {
                filterRule = allRule;
            }
            if (CollectionUtils.isEmpty(filterRule)) {
                return filterRule;
            }
            // 第五步：基于筛选后的表查询表字段详情
            List<DataTableFieldDTO> filterFiDataTables = new ArrayList<>();
            filterRule.forEach(t -> {
                if (t.getSourceTypeEnum() == SourceTypeEnum.custom) {
                    return;
                }
                DataTableFieldDTO dto = new DataTableFieldDTO();
                dto.setId(t.getTableUnique());
                dto.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(t.getFiDataSourceId()));
                dto.setTableBusinessTypeEnum(TableBusinessTypeEnum.getEnum(t.getTableBusinessType()));
                filterFiDataTables.add(dto);
            });
            List<FiDataMetaDataDTO> tableFields = null;
            if (CollectionUtils.isNotEmpty(filterFiDataTables)) {
                tableFields = dataSourceConManageImpl.getTableFieldName(filterFiDataTables);
            }
            // 第六步：表信息填充
            if (CollectionUtils.isNotEmpty(tableFields)) {
                for (BusinessFilterVO ruleDto : filterRule) {
                    FiDataMetaDataTreeDTO f_table = null;
                    if (ruleDto.getSourceTypeEnum() == SourceTypeEnum.FiData) {
                        FiDataMetaDataDTO fiDataMetaDataDTO = tableFields.stream().filter(t -> t.getDataSourceId() == ruleDto.getFiDataSourceId()).findFirst().orElse(null);
                        if (fiDataMetaDataDTO != null && CollectionUtils.isNotEmpty(fiDataMetaDataDTO.getChildren())) {
                            f_table = fiDataMetaDataDTO.getChildren().stream().filter(t -> t.getId().equals(ruleDto.getTableUnique())).findFirst().orElse(null);
                        }
                    }
                    if (f_table != null) {
                        ruleDto.setTableName(f_table.getLabel());
                        ruleDto.setTableAlias(f_table.getLabelAlias());
                    } else {
                        ruleDto.setTableName(ruleDto.getTableUnique());
                        ruleDto.setTableAlias(ruleDto.getTableUnique());
                    }
                }
            }
            // 第七步：排序设置
            filterRule = filterRule.stream().sorted(
                    // 1.先按照表名称排正序
                    Comparator.comparing(BusinessFilterVO::getTableAlias, Comparator.naturalOrder())
                            // 2.再按照调度类型排正序
                            .thenComparing(BusinessFilterVO::getTriggerScene, Comparator.naturalOrder())
                            // 3.再按照执行顺序排正序
                            .thenComparing(BusinessFilterVO::getRuleSort, Comparator.naturalOrder())
            ).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("[businessFilter]-[getAllRule]-ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return filterRule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(BusinessFilterDTO dto) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {
            //第一步：查询数据质量数据源表主键id
            if (dto.getSourceTypeEnum() == SourceTypeEnum.FiData) {
                int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(dto.getSourceTypeEnum(), dto.getDatasourceId());
                if (idByDataSourceId == 0)
                    return ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS;
                dto.setDatasourceId(idByDataSourceId);
            }
            //第二步：转换DTO对象为PO对象
            BusinessFilterPO businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo(dto);
            if (businessFilterPO == null)
                return ResultEnum.SAVE_DATA_ERROR;
            //第三步：保存业务清洗规则信息
            int i = baseMapper.insert(businessFilterPO);
            if (i <= 0)
                return ResultEnum.SAVE_DATA_ERROR;
            //第四步：调用元数据接口同步最新的规则信息
            externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        } catch (Exception ex) {
            log.error("[businessFilter]-[addData]-ex:" + ex);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, ex);
        }
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(BusinessFilterEditDTO dto) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        try {
            //第一步：查询修改的数据是否存在
            BusinessFilterPO businessFilterPO = baseMapper.selectById(dto.id);
            if (businessFilterPO == null)
                return ResultEnum.DATA_NOTEXISTS;
            //第二步：转换DTO对象为PO对象
            businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo_Edit(dto);
            if (businessFilterPO == null)
                return ResultEnum.SAVE_DATA_ERROR;
            //第三步：保存业务清洗信息
            int i = baseMapper.updateById(businessFilterPO);
            if (i <= 0)
                return ResultEnum.SAVE_DATA_ERROR;
            //第四步：调用元数据接口同步最新的规则信息
            externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        } catch (Exception ex) {
            log.error("[businessFilter]-[editData]-ex:" + ex);
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, ex);
        }
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        if (id == 0) {
            return ResultEnum.PARAMTER_ERROR;
        }
        try {
            BusinessFilterPO businessFilterPO = baseMapper.selectById(id);
            if (businessFilterPO == null)
                return ResultEnum.SUCCESS;
            // 调用元数据接口同步最新的规则信息
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(businessFilterPO.getDatasourceId());
            if (dataSourceConPO != null) {
                SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(dataSourceConPO.getDatasourceType());
                externalInterfaceImpl.synchronousTableBusinessMetaData(businessFilterPO.getDatasourceId(), sourceTypeEnum, businessFilterPO.getTableBusinessType(), businessFilterPO.getTableUnique());
            }
            return baseMapper.deleteByIdWithFill(businessFilterPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.DELETE_ERROR;
        } catch (Exception ex) {
            log.error("[businessFilter]-[deleteData]-ex:" + ex);
            throw new FkException(ResultEnum.DELETE_ERROR, ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editFilterRuleSort(List<BusinessFilterSortDto> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<Integer> collect = dto.stream().map(BusinessFilterSortDto::getId).distinct().collect(Collectors.toList());
        QueryWrapper<BusinessFilterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1).in(BusinessFilterPO::getId, collect);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessFilterPOS))
            return ResultEnum.DATA_NOTEXISTS;
        businessFilterPOS.forEach(e -> {
            Optional<BusinessFilterSortDto> first = dto.stream().filter(item -> item.getId() == e.getId()).findFirst();
            if (first.isPresent()) {
                BusinessFilterSortDto businessFilterSortDto = first.get();
                if (businessFilterSortDto != null) {
                    e.setRuleSort(businessFilterSortDto.getModuleExecSort());
                }
            }
        });
        boolean b = businessFilterManageImpl.updateBatchById(businessFilterPOS);
        return b ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<BusinessFilter_ProcessAssemblyVO>> getProcessAssembly() {
        return null;
    }

    @Override
    public ResultEntity<List<BusinessFilter_ProcessTaskVO>> getProcessDetail(long ruleId) {
        return null;
    }

    @Override
    public ResultEnum addProcess(BusinessFilter_ProcessTaskDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editProcess(BusinessFilter_ProcessTaskDTO dto) {
        return null;
    }

    @Override
    public ResultEntity<List<BusinessFilterResultVO>> collProcess(long ruleId) {
        return null;
    }

    @Override
    public ResultEntity<String> collAuthApi(BusinessFilterSaveDTO dto) {
        return businessFilterApiManageImpl.collAuthApi(dto);
    }

    @Override
    public ResultEnum collApi(BusinessFilterSaveDTO dto) {
        return businessFilterApiManageImpl.collApi(dto);
    }
}
