package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.CronUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter.BusinessFilterSaveDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_ProcessTaskDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.process.BusinessFilter_SaveProcessDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.QueryTableRuleDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.map.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterMapper;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterResultVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.*;
import com.fisk.dataservice.enums.ProcessAssemblyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
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

    @Resource
    private BusinessFilter_ProcessAssemblyManageImpl processAssemblyManageImpl;

    @Resource
    private BusinessFilter_ProcessExpressManageImpl processExpressManageImpl;

    @Resource
    private BusinessFilter_ProcessFieldAssignManageImpl processFieldAssignManageImpl;

    @Resource
    private BusinessFilter_ProcessFieldRuleManageImpl processFieldRuleManageImpl;

    @Resource
    private BusinessFilter_ProcessTaskManageImpl processTaskManageImpl;

    @Resource
    private BusinessFilter_ProcessTriggerManageImpl processTriggerManageImpl;

    @Resource
    private BusinessFilter_ProcessSqlScriptManageImpl processSqlScriptManageImpl;

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
            throw new FkException(ResultEnum.ERROR, ex);
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
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int ruleId) {
        if (ruleId == 0) {
            return ResultEnum.PARAMTER_ERROR;
        }
        try {
            BusinessFilterPO businessFilterPO = baseMapper.selectById(ruleId);
            if (businessFilterPO == null)
                return ResultEnum.SUCCESS;
            // 第一步：删除清洗规则
            if (baseMapper.deleteByIdWithFill(businessFilterPO) <= 0)
                return ResultEnum.DELETE_ERROR;
            // 第二步：删除清洗规则下的工作区流程信息
            if (!deleteProcess(ruleId))
                return ResultEnum.DELETE_ERROR;
            // 第三步：调用元数据接口同步最新的规则信息
            DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(businessFilterPO.getDatasourceId());
            if (dataSourceConPO != null)
                externalInterfaceImpl.synchronousTableBusinessMetaData(businessFilterPO.getDatasourceId(), SourceTypeEnum.getEnum(dataSourceConPO.getDatasourceType()), businessFilterPO.getTableBusinessType(), businessFilterPO.getTableUnique());
            // 第四步：禁用调度任务（rule维度）
            if (businessFilterPO.getTriggerScene() == 1) {

            }
        } catch (Exception ex) {
            log.error("[businessFilter]-[deleteData]-ex:" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return ResultEnum.SUCCESS;
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
    public List<BusinessFilter_ProcessAssemblyVO> getProcessAssembly() {
        return processAssemblyManageImpl.getVOList();
    }

    @Override
    public ResultEntity<List<BusinessFilter_ProcessTaskVO>> getProcessDetail(long ruleId) {
        List<BusinessFilter_ProcessTaskVO> processTaskList = new ArrayList<>();
        if (ruleId == 0) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, processTaskList);
        }
        try {
            // 第一步：查询清洗规则信息
            BusinessFilterPO businessFilterPO = baseMapper.selectById(ruleId);
            if (businessFilterPO == null)
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_THE_CLEANING_RULE_DOES_NOT_EXIST, processTaskList);
            // 第二步：查询工作区-流程信息
            List<BusinessFilter_ProcessTaskVO> processTaskVOList = processTaskManageImpl.getVOList(ruleId);
            if (CollectionUtils.isEmpty(processTaskVOList))
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, processTaskList);
            // 第三步：查询工作区-条件表达式
            List<BusinessFilter_ProcessExpressVO> processExpressVOList = processExpressManageImpl.getVOList(ruleId);
            // 第四步：查询工作区-字段赋值
            List<BusinessFilter_ProcessFieldAssignVO> processFieldAssignVOList = processFieldAssignManageImpl.getVOList(ruleId);
            // 第五步：查询工作区-字段规则
            List<BusinessFilter_ProcessFieldRuleVO> processFieldRuleVOList = processFieldRuleManageImpl.getVOList(ruleId);
            // 第六步：查询工作区-调度配置
            List<BusinessFilter_ProcessTriggerVO> processTriggerVOList = processTriggerManageImpl.getVOList(ruleId);
            // 第七步：查询工作区-SQL脚本
            List<BusinessFilter_ProcessSqlScriptVO> processSqlScriptVOList = processSqlScriptManageImpl.getVOList(ruleId);

            // 第八步：组装数据-将工作区的信息组装到流程中
            for (BusinessFilter_ProcessTaskVO processTaskVO : processTaskVOList) {
                ProcessAssemblyTypeEnum processAssemblyTypeEnum = ProcessAssemblyTypeEnum.getEnum(processTaskVO.getAssemblyCode());
                if (processAssemblyTypeEnum == ProcessAssemblyTypeEnum.NONE)
                    throw new FkException(ResultEnum.ERROR, "清洗组件缺失");

                List<BusinessFilter_ProcessFieldRuleVO> filter_processFieldRuleVOList = null;
                switch (processAssemblyTypeEnum) {
                    case TRIGGER:
                        BusinessFilter_ProcessTriggerVO filter_processTriggerVO = processTriggerVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode())).findFirst().orElse(null);
                        if (filter_processTriggerVO != null) {
                            if (filter_processTriggerVO.getTriggerScene() == 1 && StringUtils.isNotEmpty(filter_processTriggerVO.getTriggerValue())) {
                                String nextExecutionTime = "";
                                if (filter_processTriggerVO.getTriggerType() == "TIMER DRIVEN") {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Calendar nowTime = Calendar.getInstance();
                                    nowTime.add(Calendar.SECOND, Integer.parseInt(filter_processTriggerVO.getTriggerValue()));
                                    nextExecutionTime = sdf.format(nowTime.getTime());
                                } else if (filter_processTriggerVO.getTriggerType() == "CRON DRIVEN") {
                                    nextExecutionTime = CronUtils.getCronExpress(filter_processTriggerVO.getTriggerValue());
                                }
                                filter_processTriggerVO.setNextExecutionTime(nextExecutionTime);
                            }
                            processTaskVO.setProcessTriggerInfo(filter_processTriggerVO);
                        }
                        break;
                    case CONDITIONAL_EXPRESS:
                        BusinessFilter_ProcessExpressVO filter_processExpressVO = processExpressVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode())).findFirst().orElse(null);
                        if (filter_processExpressVO != null) {
                            filter_processFieldRuleVOList = processFieldRuleVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode()) && t.getFkDataCode().equals(filter_processExpressVO.getDataCode())).collect(Collectors.toList());
                            filter_processExpressVO.setExpressRuleList(filter_processFieldRuleVOList);
                            processTaskVO.setProcessExpressInfo(filter_processExpressVO);
                        }
                        break;
                    case SQL_SCRIPT:
                        BusinessFilter_ProcessSqlScriptVO filter_processSqlScriptVO = processSqlScriptVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode())).findFirst().orElse(null);
                        if (filter_processSqlScriptVO != null) {
                            processTaskVO.setProcessSqlScriptInfo(filter_processSqlScriptVO);
                        }
                        break;
                    case OPENAPI:
                        break;
                    case FIELD_ASSIGNMENT:
                        BusinessFilter_ProcessFieldAssignVO filter_processFieldAssignVO = processFieldAssignVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode())).findFirst().orElse(null);
                        if (filter_processFieldAssignVO != null) {
                            filter_processFieldRuleVOList = processFieldRuleVOList.stream().filter(t -> t.getTaskCode().equals(processTaskVO.getTaskCode()) && t.getFkDataCode().equals(filter_processFieldAssignVO.getDataCode())).collect(Collectors.toList());
                            filter_processFieldAssignVO.setFieldRuleList(filter_processFieldRuleVOList);
                            processTaskVO.setProcessFieldAssignInfo(filter_processFieldAssignVO);
                        }
                        break;
                }
                processTaskList.add(processTaskVO);
            }
        } catch (Exception ex) {
            log.error("[businessFilter]-[getProcessDetail]-ex:" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, processTaskList);
    }

    @Override
    public String getProcessTaskCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveProcess(BusinessFilter_SaveProcessDTO dto) {
        if (dto == null || dto.getRuleId() == 0 || CollectionUtils.isEmpty(dto.getProcessTaskList())) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        boolean isSavePass = false;
        int ruleId = dto.getRuleId();

        try {
            // 第一步：查询清洗规则信息
            BusinessFilterPO businessFilterPO = baseMapper.selectById(ruleId);
            if (businessFilterPO == null)
                return ResultEnum.DATA_QUALITY_THE_CLEANING_RULE_DOES_NOT_EXIST;
            // 第二步：dto转换为po
            BusinessFilter_ProcessTriggerPO processTriggerPO = null;
            List<BusinessFilter_ProcessTaskPO> processTaskPOList = new ArrayList<>();
            List<BusinessFilter_ProcessExpressPO> processExpressPOList = new ArrayList<>();
            List<BusinessFilter_ProcessFieldAssignPO> processFieldAssignPOList = new ArrayList<>();
            List<BusinessFilter_ProcessFieldRulePO> processFieldRulePOList = new ArrayList<>();
            List<BusinessFilter_ProcessSqlScriptPO> processSqlScriptPOList = new ArrayList<>();
            for (BusinessFilter_ProcessTaskDTO processTaskDTO : dto.getProcessTaskList()) {
                ProcessAssemblyTypeEnum processAssemblyTypeEnum = ProcessAssemblyTypeEnum.getEnum(processTaskDTO.getAssemblyCode());
                if (processAssemblyTypeEnum == ProcessAssemblyTypeEnum.NONE)
                    throw new FkException(ResultEnum.ERROR, "清洗组件缺失");

                String taskCode = processTaskDTO.getTaskCode();
                List<BusinessFilter_ProcessFieldRulePO> filter_processFieldRulePOList = null;
                BusinessFilter_ProcessTaskPO processTaskPO = BusinessFilter_ProcessTaskMap.INSTANCES.dtoToPo(processTaskDTO);
                processTaskPO.setRuleId(ruleId);
                switch (processAssemblyTypeEnum) {
                    case TRIGGER:
                        processTriggerPO = BusinessFilter_ProcessTriggerMap.INSTANCES.dtoToPo(processTaskDTO.getProcessTriggerInfo());
                        if (processTriggerPO != null) {
                            processTriggerPO.setRuleId(ruleId);
                            processTriggerPO.setTaskCode(taskCode);
                            processTaskPO.setCustomDescribe(processTriggerPO.getCustomDescribe());
                        }
                        break;
                    case CONDITIONAL_EXPRESS:
                        BusinessFilter_ProcessExpressPO processExpressPO = BusinessFilter_ProcessExpressMap.INSTANCES.dtoToPo(processTaskDTO.getProcessExpressInfo());
                        if (processExpressPO != null) {
                            processExpressPO.setRuleId(ruleId);
                            processExpressPO.setTaskCode(taskCode);
                            processExpressPO.setDataCode(getProcessTaskCode());
                            processTaskPO.setCustomDescribe(processExpressPO.getCustomDescribe());
                            processExpressPOList.add(processExpressPO);
                            filter_processFieldRulePOList = BusinessFilter_ProcessFieldRuleMap.INSTANCES.dtoListToPoList(processTaskDTO.getProcessExpressInfo().getExpressRuleList());
                            if (CollectionUtils.isNotEmpty(filter_processFieldRulePOList)) {
                                filter_processFieldRulePOList.forEach(processFieldRulePO -> {
                                    processFieldRulePO.setRuleId(ruleId);
                                    processFieldRulePO.setTaskCode(taskCode);
                                    processFieldRulePO.setFkDataCode(processExpressPO.getDataCode());
                                });
                                processFieldRulePOList.addAll(filter_processFieldRulePOList);
                            }
                        }
                        break;
                    case SQL_SCRIPT:
                        BusinessFilter_ProcessSqlScriptPO processSqlScriptPO = BusinessFilter_ProcessSqlScriptMap.INSTANCES.dtoToPo(processTaskDTO.getProcessSqlScriptInfo());
                        if (processSqlScriptPO != null) {
                            processSqlScriptPO.setRuleId(ruleId);
                            processSqlScriptPO.setTaskCode(taskCode);
                            processSqlScriptPOList.add(processSqlScriptPO);
                        }
                        break;
                    case OPENAPI:
                        break;
                    case FIELD_ASSIGNMENT:
                        BusinessFilter_ProcessFieldAssignPO processFieldAssignPO = BusinessFilter_ProcessFieldAssignMap.INSTANCES.dtoToPo(processTaskDTO.getProcessFieldAssignInfo());
                        if (processFieldAssignPO != null) {
                            processFieldAssignPO.setRuleId(ruleId);
                            processFieldAssignPO.setTaskCode(taskCode);
                            processFieldAssignPO.setDataCode(getProcessTaskCode());
                            processTaskPO.setCustomDescribe(processFieldAssignPO.getCustomDescribe());
                            processFieldAssignPOList.add(processFieldAssignPO);
                            filter_processFieldRulePOList = BusinessFilter_ProcessFieldRuleMap.INSTANCES.dtoListToPoList(processTaskDTO.getProcessExpressInfo().getExpressRuleList());
                            if (CollectionUtils.isNotEmpty(filter_processFieldRulePOList)) {
                                filter_processFieldRulePOList.forEach(processFieldRulePO -> {
                                    processFieldRulePO.setRuleId(ruleId);
                                    processFieldRulePO.setTaskCode(taskCode);
                                    processFieldRulePO.setFkDataCode(processFieldAssignPO.getDataCode());
                                });
                                processFieldRulePOList.addAll(filter_processFieldRulePOList);
                            }
                        }
                        break;
                }
                processTaskPOList.add(processTaskPO);
            }
            if (processTriggerPO == null)
                return ResultEnum.DATA_QUALITY_PLEASE_CONFIGURE_TRIGGER;
            // 第三步：根据ruleId删除工作区流程配置
            if (!deleteProcess(ruleId))
                return ResultEnum.SAVE_DATA_ERROR;
            // 第四步：保存工作区流程配置到数据库
            isSavePass = processTriggerManageImpl.save(processTriggerPO);
            if (CollectionUtils.isNotEmpty(processTaskPOList))
                isSavePass = processTaskManageImpl.saveBatch(processTaskPOList);
            if (CollectionUtils.isNotEmpty(processExpressPOList))
                isSavePass = processExpressManageImpl.saveBatch(processExpressPOList);
            if (CollectionUtils.isNotEmpty(processFieldAssignPOList))
                isSavePass = processFieldAssignManageImpl.saveBatch(processFieldAssignPOList);
            if (CollectionUtils.isNotEmpty(processFieldRulePOList))
                isSavePass = processFieldRuleManageImpl.saveBatch(processFieldRulePOList);
            if (CollectionUtils.isNotEmpty(processSqlScriptPOList))
                isSavePass = processSqlScriptManageImpl.saveBatch(processSqlScriptPOList);
            // 第五步：清洗规则回写设置调度信息
            businessFilterPO.setTriggerScene(processTriggerPO.getTriggerScene());
            isSavePass = baseMapper.updateById(businessFilterPO) > 0;
            // 第六步：创建/更新调度任务（rule维度）
            if (processTriggerPO.getTriggerScene() == 1 && StringUtils.isNotEmpty(processTriggerPO.getTriggerValue())) {

            } else {
                // 删除调度任务（rule维度）

            }
        } catch (Exception ex) {
            log.error("[businessFilter]-[saveProcess]-ex:" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return isSavePass ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<BusinessFilterResultVO>> collProcess(long ruleId) {
        List<BusinessFilterResultVO> filterResultList = new ArrayList<>();
        if (ruleId == 0) {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_NOTNULL, filterResultList);
        }
        try {
            // 第一步：查询清洗规则信息
            BusinessFilterPO businessFilterPO = baseMapper.selectById(ruleId);
            if (businessFilterPO == null)
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_THE_CLEANING_RULE_DOES_NOT_EXIST, filterResultList);
            // 第二步：查询清洗规则工作区流程信息
            // 第三步：工作区流程递归获取清洗SQL

        } catch (Exception ex) {
            log.error("[businessFilter]-[collProcess]-ex:" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
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

    private boolean deleteProcess(int ruleId) {
        boolean isDeletePass = true;
        List<BusinessFilter_ProcessTaskPO> processTaskPOList = processTaskManageImpl.getPOList(ruleId);
        List<BusinessFilter_ProcessTriggerPO> processTriggerPOList = processTriggerManageImpl.getPOList(ruleId);
        List<BusinessFilter_ProcessExpressPO> processExpressPOList = processExpressManageImpl.getPOList(ruleId);
        List<BusinessFilter_ProcessFieldAssignPO> processFieldAssignPOList = processFieldAssignManageImpl.getPOList(ruleId);
        List<BusinessFilter_ProcessFieldRulePO> processFieldRulePOList = processFieldRuleManageImpl.getPOList(ruleId);
        List<BusinessFilter_ProcessSqlScriptPO> processSqlScriptPOList = processSqlScriptManageImpl.getPOList(ruleId);
        isDeletePass = removePOList(processTaskPOList, processTaskManageImpl);
        isDeletePass = removePOList(processTriggerPOList, processTriggerManageImpl);
        isDeletePass = removePOList(processExpressPOList, processExpressManageImpl);
        isDeletePass = removePOList(processFieldAssignPOList, processFieldAssignManageImpl);
        isDeletePass = removePOList(processFieldRulePOList, processFieldRuleManageImpl);
        isDeletePass = removePOList(processSqlScriptPOList, processSqlScriptManageImpl);
        return isDeletePass;
    }

    private boolean removePOList(List<? extends BasePO> poList, IService<? extends BasePO> service) {
        if (CollectionUtils.isNotEmpty(poList)) {
            List<Long> removeIdList = poList.stream().map(BasePO::getId).collect(Collectors.toList());
            return service.removeByIds(removeIdList);
        }
        return true;
    }
}
