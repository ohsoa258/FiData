package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.dto.dataquality.datasource.QueryTableRuleDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckExtendMap;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验实现类
 * @date 2022/3/23 12:56
 */
@Service
@Slf4j
public class DataCheckManageImpl extends ServiceImpl<DataCheckMapper, DataCheckPO> implements IDataCheckManageService {

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private DataCheckLogsManageImpl dataCheckLogsManage;

    @Resource
    private DataCheckExtendManageImpl dataCheckExtendManageImpl;

    @Resource
    private UserHelper userHelper;

    private static final String WARN = "warn";
    private static final String FAIL = "fail";
    private static final String SUCCESS = "success";

    @Override
    public List<DataCheckVO> getAllRule(DataCheckQueryDTO query) {
        // 第一步：参数验证
        List<DataCheckVO> filterRule = new ArrayList<>();
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
                queryTableParam.setSourceType(query.getSourceType());
                queryTableParams.add(queryTableParam);
            } else if (query.getLevelType() == LevelTypeEnum.BASEFOLDER
                    || query.getLevelType() == LevelTypeEnum.DATABASE
                    || query.getLevelType() == LevelTypeEnum.FOLDER) {
                List<QueryTableRuleDTO> treeTableNodes = dataSourceConManageImpl.getTreeTableNode_main(query.getSourceType(), query.getUniqueId());
                if (CollectionUtils.isNotEmpty(treeTableNodes)) {
                    queryTableParams.addAll(treeTableNodes);
                }
            }
            // 第三步：获取所有表校验规则
            List<DataCheckVO> allRule = baseMapper.getAllRule();
            if (CollectionUtils.isEmpty(allRule)) {
                return filterRule;
            }
            // 第四步：筛选满足条件的表/视图的规则
            if (CollectionUtils.isNotEmpty(queryTableParams)) {
                for (QueryTableRuleDTO dto : queryTableParams) {
                    List<DataCheckVO> rules = null;
                    TableTypeEnum tableType = TableTypeEnum.NONE;
                    if (dto.getTableType() == LevelTypeEnum.TABLE) {
                        tableType = TableTypeEnum.TABLE;
                    } else if (dto.getTableType() == LevelTypeEnum.VIEW) {
                        tableType = TableTypeEnum.VIEW;
                    }
                    TableTypeEnum finalTableType = tableType;
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
//            List<DataTableFieldDTO> filterFiDataTables = new ArrayList<>();
//            filterRule.forEach(t -> {
//                if (t.getSourceType() == SourceTypeEnum.custom) {
//                    return;
//                }
//                DataTableFieldDTO dto = new DataTableFieldDTO();
//                dto.setId(t.getTableUnique());
//                dto.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(t.getFiDataSourceId()));
//                dto.setTableBusinessTypeEnum(t.getTableBusinessType());
//                filterFiDataTables.add(dto);
//            });
//            List<FiDataMetaDataDTO> tableFields = null;
//            if (CollectionUtils.isNotEmpty(filterFiDataTables)) {
//                tableFields = dataSourceConManageImpl.getTableFieldName(filterFiDataTables);
//            }
            // 第六步：表字段信息填充
//            filterRule = dataCheckExtendManageImpl.setTableFieldName(filterRule, tableFields);
            List<Integer> ruleIds = filterRule.stream().map(DataCheckVO::getId).collect(Collectors.toList());
            List<DataCheckExtendVO> dataCheckExtendVOList = dataCheckExtendMapper.getDataCheckExtendByRuleIdList(ruleIds);
            if (CollectionUtils.isNotEmpty(dataCheckExtendVOList)) {
                filterRule.forEach(t -> {
                    List<DataCheckExtendVO> dataCheckExtendVOS = dataCheckExtendVOList.stream().filter(k -> k.getRuleId() == t.getId()).collect(Collectors.toList());
                    t.setDataCheckExtends(dataCheckExtendVOS);
                });
            }
            // 第七步：排序设置
            filterRule = filterRule.stream().sorted(
                    // 1.先按照表名称排正序
                    Comparator.comparing(DataCheckVO::getTableAlias, Comparator.naturalOrder())
                            // 2.再按照执行节点排正序
                            .thenComparing(DataCheckVO::getRuleExecuteNode, Comparator.naturalOrder())
                            // 3.再按照执行顺序排正序
                            .thenComparing(DataCheckVO::getRuleExecuteSort, Comparator.naturalOrder())
            ).collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("【getAllRule】查询校验规则列表异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return filterRule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataCheckDTO dto) {
        //第一步：验证模板是否存在以及表规则是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.getTemplateId());
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：转换DTO对象为PO对象
        DataCheckPO dataCheckPO = DataCheckMap.INSTANCES.dtoToPo(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        //第三步：保存数据校验
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        dataCheckPO.setCreateTime(LocalDateTime.now());
        dataCheckPO.setCreateUser(String.valueOf(loginUserInfo.getId()));
        int i = baseMapper.insertOne(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验扩展属性
        if (CollectionUtils.isNotEmpty(dto.getDataCheckExtends())) {
            List<DataCheckExtendPO> dataCheckExtends = new ArrayList<>();
            dto.getDataCheckExtends().forEach(t -> {
                DataCheckExtendPO dataCheckExtendPO = DataCheckExtendMap.INSTANCES.dtoToPo(t);
                dataCheckExtendPO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                dataCheckExtends.add(dataCheckExtendPO);
            });
            dataCheckExtendManageImpl.saveBatch(dataCheckExtends);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(DataCheckEditDTO dto) {
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.getTemplateId());
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        DataCheckPO dataCheckPO = baseMapper.selectById(dto.getId());
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        //第二步：转换DTO对象为PO对象
        dataCheckPO = DataCheckMap.INSTANCES.dtoToPo_Edit(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        //第三步：保存数据校验信息
        int i = baseMapper.updateById(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验扩展属性
        if (CollectionUtils.isNotEmpty(dto.getDataCheckExtends())) {
            dataCheckExtendMapper.updateByRuleId(dto.getId());
            List<DataCheckExtendPO> dataCheckExtends = new ArrayList<>();
            dto.getDataCheckExtends().forEach(t -> {
                DataCheckExtendPO dataCheckExtendPO = DataCheckExtendMap.INSTANCES.dtoToPo(t);
                dataCheckExtendPO.setRuleId(Math.toIntExact(dto.getId()));
                dataCheckExtends.add(dataCheckExtendPO);
            });
            dataCheckExtendManageImpl.saveBatch(dataCheckExtends);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        DataCheckPO dataCheckPO = baseMapper.selectById(id);
        if (dataCheckPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 删除数据校验扩展属性
        dataCheckExtendMapper.updateByRuleId(id);
        return baseMapper.deleteByIdWithFill(dataCheckPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(DataCheckWebDTO dto) {
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
        List<DataCheckLogsPO> dataCheckLogs = new ArrayList<>();
        ResultEnum resultEnum = ResultEnum.SUCCESS;

        try {
            // 第一步：查询数据源信息
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            if (allDataSource == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS, dataCheckResults);
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getDatasourceId() == dto.getFiDataDataSourceId()).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS, dataCheckResults);
            }

            // 第二步：查询数据校验模块下的模板
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda()
                    .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATA_CHECK_MODULE.getValue())
                    .eq(TemplatePO::getTemplateState, 1)
                    .eq(TemplatePO::getDelFlag, 1);
            List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
            if (CollectionUtils.isEmpty(templatePOList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, dataCheckResults);
            }

            // 第三步：查询配置的表检查规则信息
            Set<String> tableUniques = dto.body.keySet();
            List<Long> templateIds = templatePOList.stream().map(TemplatePO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda()
                    .eq(DataCheckPO::getDatasourceId, dataSourceConVO.getId())
                    .eq(DataCheckPO::getDelFlag, 1)
                    .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .eq(DataCheckPO::getRuleExecuteNode, RuleExecuteNodeTypeEnum.BEFORE_SYNCHRONIZATION.getValue())
                    .in(DataCheckPO::getTableUnique, tableUniques)
                    .in(DataCheckPO::getTemplateId, templateIds)
                    .orderByAsc(DataCheckPO::getRuleExecuteSort);
            List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckPOList)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResults);
            }

            // 第四步：查询表检查规则的扩展属性
            List<Long> ruleIds = dataCheckPOList.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                    .in(DataCheckExtendPO::getRuleId, ruleIds);
            List<DataCheckExtendPO> dataCheckExtends = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckExtends)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATACHECK_RULE_ERROR, dataCheckResults);
            }

            // 第五步：循环规则，解析数据，验证数据是否合规
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                TemplatePO templatePO = templatePOList.stream()
                        .filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst()
                        .orElse(null);
                DataCheckExtendPO dataCheckExtendPO = dataCheckExtends.stream()
                        .filter(item -> item.getRuleId() == dataCheckPO.getId()).findFirst()
                        .orElse(null);
                if (templatePO == null) {
                    log.info("【interfaceCheckData】模板为空，当前检查规则标识为：" + dataCheckPO.getId());
                    log.info("【interfaceCheckData】模板为空，当前检查规则名称为：" + dataCheckPO.getRuleName());
                    continue;
                }
                if (dataCheckExtendPO == null) {
                    log.info("【interfaceCheckData】扩展属性为空，当前检查规则标识为：" + dataCheckPO.getId());
                    log.info("【interfaceCheckData】扩展属性为空，当前检查规则名称为：" + dataCheckPO.getRuleName());
                    continue;
                }
                JSONArray data = dto.body.get(dataCheckPO.getTableUnique());
                if (CollectionUtils.isEmpty(data)) {
                    log.info("【interfaceCheckData】数据集为空，当前检查规则标识为：" + dataCheckPO.getId());
                    log.info("【interfaceCheckData】数据集为空，当前检查规则名称为：" + dataCheckPO.getRuleName());
                    continue;
                }

                // 获取表和字段信息，将其进行转义处理
                String tableName = "";
                String tableNameFormat = "";
                if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                    tableNameFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getSchemaName()) + ".";
                    tableName = dataSourceConVO.getConType() + ".";
                }
                tableNameFormat += nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getTableName());
                tableName += dataCheckPO.getTableName();

                String fieldName = "";
                String fieldNameFormat = "";
                if (StringUtils.isNotEmpty(dataCheckExtendPO.getFieldName())) {
                    fieldNameFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckExtendPO.getFieldName());
                    fieldName = dataCheckExtendPO.getFieldName();
                }
                DataCheckSyncParamDTO dataCheckSyncParamDTO = new DataCheckSyncParamDTO();
                dataCheckSyncParamDTO.setTableName(tableName);
                dataCheckSyncParamDTO.setTableNameFormat(tableNameFormat);
                dataCheckSyncParamDTO.setFieldName(fieldName);
                dataCheckSyncParamDTO.setFieldNameFormat(fieldNameFormat);
                dataCheckSyncParamDTO.setBatchNumber(dto.getBatchNumber());
                dataCheckSyncParamDTO.setSmallBatchNumber(dto.getSmallBatchNumber());

                DataCheckResultVO dataCheckResult = null;
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case NULL_CHECK:
                        dataCheckResult = interface_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case RANGE_CHECK:
                        dataCheckResult = interface_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case STANDARD_CHECK:
                        dataCheckResult = interface_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case DUPLICATE_DATA_CHECK:
                        dataCheckResult = interface_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case FLUCTUATION_CHECK:
                        dataCheckResult = interface_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case PARENTAGE_CHECK:
                        dataCheckResult = interface_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case REGEX_CHECK:
                        dataCheckResult = interface_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                    case SQL_SCRIPT_CHECK:
                        dataCheckResult = interface_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data, dataCheckSyncParamDTO);
                        break;
                }
                if (dataCheckResult != null) {
                    // 第六步：验证规则是否全部校验通过，并记录日志
                    if (dataCheckResult.getCheckResult().equals(FAIL)) {
                        resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                    }
                    // 第七步：记录数据校验日志
                    if (dataCheckExtendPO.getRecordErrorData() == 1) {
                        DataCheckLogsPO dataCheckLogsPO = new DataCheckLogsPO();
                        dataCheckLogsPO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                        dataCheckLogsPO.setTemplateId(Math.toIntExact(templatePO.getId()));
                        dataCheckLogsPO.setFiDatasourceId(dataSourceConVO.getDatasourceId());
                        dataCheckLogsPO.setLogType(DataCheckLogTypeEnum.INTERFACE_DATA_CHECK_LOG.getValue());
                        dataCheckLogsPO.setSchemaName(dataCheckResult.getCheckSchema());
                        dataCheckLogsPO.setTableName(dataCheckSyncParamDTO.getTableName());
                        dataCheckLogsPO.setFieldName(dataCheckSyncParamDTO.getFieldName());
                        dataCheckLogsPO.setCheckTemplateName(dataCheckResult.getCheckTemplateName());
                        dataCheckLogsPO.setCheckBatchNumber(dataCheckSyncParamDTO.getBatchNumber());
                        dataCheckLogsPO.setCheckSmallBatchNumber(dataCheckSyncParamDTO.getSmallBatchNumber());
                        dataCheckLogsPO.setCheckTotalCount(dataCheckResult.getCheckTotalCount());
                        dataCheckLogsPO.setCheckFailCount(dataCheckResult.getCheckFailCount());
                        dataCheckLogsPO.setCheckResult(dataCheckResult.getCheckResult().toString());
                        dataCheckLogsPO.setCheckMsg(dataCheckResult.getCheckResultMsg());
                        dataCheckLogsPO.setErrorData(dataCheckResult.getCheckErrorData());
                        dataCheckLogs.add(dataCheckLogsPO);
                        // 清空校验不通过的数据字段，减少返回的字节流
                        dataCheckResult.checkErrorData = null;
                    }
                    dataCheckResults.add(dataCheckResult);
                }
            }

            // 第八步：保存数据检查日志
            if (CollectionUtils.isNotEmpty(dataCheckLogs)) {
                dataCheckLogsManage.saveLog(dataCheckLogs);
            }
        } catch (Exception ex) {
            log.error("【interfaceCheckData】执行异常：" + ex);
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(resultEnum, dataCheckResults);
    }

    public DataCheckResultVO interface_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                 DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                fieldValues.add(jsonObject.getString(fName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过空值检查
        List<String> fieldValueFilters = fieldValues.stream().filter(item -> StringUtils.isEmpty(item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tName, fName, TemplateTypeEnum.NULL_CHECK.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.NULL_CHECK.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(fieldValueFilters));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tName, fName, TemplateTypeEnum.NULL_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                  DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                fieldValues.add(jsonObject.getString(fName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过值域验证
        List<String> errorDataList = new ArrayList<>();
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                // 序列范围
                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                errorDataList = RegexUtils.subtractValid(fieldValues, list, true);
                break;
            case VALUE_RANGE:
                // 取值范围
                Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                for (String item : fieldValues) {
                    if (StringUtils.isNotEmpty(item)) {
                        Integer value = Integer.valueOf(item);
                        if (value < lowerBound_Int || value > upperBound_Int) {
                            errorDataList.add(item);
                        }
                    } else {
                        errorDataList.add(item);
                    }
                }
                break;
            case DATE_RANGE:
                // 日期范围
                for (String item : fieldValues) {
                    if (StringUtils.isNotEmpty(item)) {
                        LocalDateTime dateTime = LocalDateTime.parse(item, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = dateTime.format(formatter);

                        String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                        String[] timeRange = timeRangeString.split("~");
                        LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                        LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);

                        LocalDateTime formattedDateTime = LocalDateTime.parse(formattedDate, formatter);

                        if (formattedDateTime.isBefore(startTime) || formattedDateTime.isAfter(endTime)) {
                            errorDataList.add(item);
                        }
                    } else {
                        errorDataList.add(item);
                    }
                }
                break;
        }
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tName, fName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tName, fName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                     DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                fieldValues.add(jsonObject.getString(fName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过规范检查
        List<String> errorDataList = new ArrayList<>();
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        switch (standardCheckTypeEnum) {
            case DATE_FORMAT:
                // 日期格式
                List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                for (String dateStr : fieldValues) {
                    boolean validDateFormat = DateTimeUtils.isValidDateFormat(dateStr, list);
                    if (!validDateFormat) {
                        errorDataList.add(dateStr);
                    }
                }
                break;
            case URL_ADDRESS:
                // 字符精度长度范围
                int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                for (String item : fieldValues) {
                    if (StringUtils.isNotEmpty(item)) {
                        List<String> values = Arrays.asList(item.split(dataCheckExtendPO.getStandardCheckTypeLengthSeparator()));
                        if (values.stream().count() >= 2) {
                            String value = values.get(Math.toIntExact(values.stream().count() - 1));
                            if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                errorDataList.add(item);
                            }
                        }
                    } else {
                        errorDataList.add(item);
                    }
                }
                break;
            case BASE64_BYTE_STREAM:
                for (String item : fieldValues) {
                    // URL地址
                    boolean validURL = RegexUtils.isValidURL(item, false);
                    if (!validURL) {
                        errorDataList.add(item);
                    }
                }
                break;
            case CHARACTER_PRECISION_LENGTH_RANGE:
                for (String item : fieldValues) {
                    // BASE64字节流
                    boolean validBase64String = RegexUtils.isBase64String(item, false);
                    if (!validBase64String) {
                        errorDataList.add(item);
                    }
                }
                break;
        }

        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                          DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值并判断是否重复
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        List<String> fieldValues = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            String value = "";
            for (String fieldName : fieldNames) {
                if (jsonObject.containsKey(fieldName)) {
                    value += jsonObject.getString(fieldName) + "_";
                } else {
                    dataCheckResultVO.setCheckResult(FAIL);
                    dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
                    return dataCheckResultVO;
                }
            }
            if (fieldValues.contains(value.toLowerCase())) {
                JSONObject obj = new JSONObject();
                for (String fieldName : fieldNames) {
                    obj.put(fieldName, jsonObject.getString(fieldName));
                }
                jsonArray.add(obj);
            }
            fieldValues.add(value.toLowerCase());
        }

        if (CollectionUtils.isNotEmpty(jsonArray)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                        DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                fieldValues.add(jsonObject.getString(fName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
            return dataCheckResultVO;
        }

        // 第三步：获取字段聚合后的数据
        boolean isValid = true;
        double thresholdValue = dataCheckExtendPO.getFluctuateCheckValue();
        double realityValue = 0.0;
        FluctuateCheckTypeEnum fluctuateCheckTypeEnum = FluctuateCheckTypeEnum.getEnum(dataCheckExtendPO.getFluctuateCheckType());
        switch (fluctuateCheckTypeEnum) {
            case AVG:
                List<Double> filterValues = fieldValues.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(Double::parseDouble)
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(filterValues)) {
                    double sum = filterValues.stream().mapToDouble(Double::doubleValue).sum();
                    realityValue = sum / filterValues.size();
                }
                break;
            case MIN:
                Optional<Double> optionalMin = fieldValues.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(Double::parseDouble)
                        .min(Double::compareTo);
                if (optionalMin.isPresent()) {
                    realityValue = optionalMin.get();
                }
                break;
            case MAX:
                Optional<Double> optionalMax = fieldValues.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(Double::parseDouble)
                        .max(Double::compareTo);
                if (optionalMax.isPresent()) {
                    realityValue = optionalMax.get();
                }
                break;
            case SUM:
                realityValue = fieldValues.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(Double::parseDouble)
                        .mapToDouble(Double::doubleValue).sum();
                break;
            case COUNT:
                realityValue = fieldValues.stream().count();
                break;
        }

        // 第四步：判断字段值是否通过波动检查
        FluctuateCheckOperatorEnum fluctuateCheckOperatorEnum = FluctuateCheckOperatorEnum.getEnumByName(dataCheckExtendPO.getFluctuateCheckOperator());
        switch (fluctuateCheckOperatorEnum) {
            case GREATER_THAN:
                if (realityValue > thresholdValue) {
                    isValid = false;
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (realityValue >= thresholdValue) {
                    isValid = false;
                }
                break;
            case EQUAL:
                if (realityValue == thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN:
                if (realityValue < thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (realityValue <= thresholdValue) {
                    isValid = false;
                }
                break;
        }
        if (!isValid) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                // 字段名称
                jsonObject.put("FieldName", fName);
                // 聚合值
                jsonObject.put("AggregateValue", realityValue);
                // 波动阈值
                jsonObject.put("ThresholdValue", thresholdValue);
                jsonArray.add(jsonObject);
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                      DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 调用元数据接口查询表上下游血缘信息
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String upTableName = "";
        String downTableName = "";

        boolean isValid = false;
        ParentageCheckTypeEnum parentageCheckTypeEnum = ParentageCheckTypeEnum.getEnum(dataCheckExtendPO.getParentageCheckType());
        switch (parentageCheckTypeEnum) {
            case CHECK_UPSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(upTableName)) {
                    isValid = true;
                }
                break;
            case CHECK_DOWNSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(downTableName)) {
                    isValid = true;
                }
                break;
            case CHECK_UPSTREAM_AND_DOWNSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(upTableName) && StringUtils.isNotEmpty(downTableName)) {
                    isValid = true;
                }
                break;
        }

        if (!isValid) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                  DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                fieldValues.add(jsonObject.getString(fName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过正则表达式验证
        List<String> errorDataList = new ArrayList<>();
        for (String item : fieldValues) {
            boolean isValid = RegexUtils.isValidPattern(item, dataCheckExtendPO.getRegexpCheckValue(), false);
            if (!isValid) {
                errorDataList.add(item);
            }
        }

        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                      DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：建立数据库连接执行SQL查询语句
        Connection conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray jsonArray = AbstractCommonDbHelper.execQueryResultArrays(dataCheckExtendPO.getSqlCheckValue(), conn);
        // 固定返回checkstate，通过为1，未通过为0，取第一行的checkstate字段判断
        boolean isValid = false;
        String checkState = "";
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.containsKey("checkstate")) {
                checkState = jsonObject.getString("checkstate");
                if (checkState != null && checkState.equals("1")) {
                    isValid = true;
                }
            }
        }

        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        if (!isValid) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_GetCheckResultBasisInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                               DataCheckExtendPO dataCheckExtendPO) {
        DataCheckResultVO dataCheckResultVO = new DataCheckResultVO();
        dataCheckResultVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
        dataCheckResultVO.setRuleName(dataCheckPO.getRuleName());
        dataCheckResultVO.setCheckDataBase(dataSourceConVO.getConDbname());
        dataCheckResultVO.setCheckSchema(dataCheckPO.getSchemaName());
        dataCheckResultVO.setCheckTable(dataCheckPO.getTableName());
        dataCheckResultVO.setCheckTableUnique(dataCheckPO.getTableUnique());
        dataCheckResultVO.setCheckField(dataCheckExtendPO.getFieldName());
        dataCheckResultVO.setCheckFieldUnique(dataCheckExtendPO.getFieldUnique());
        dataCheckResultVO.setCheckType(RuleCheckTypeEnum.getEnum(dataCheckPO.getRuleCheckType()).getName());
        dataCheckResultVO.setCheckTemplateName(templatePO.getTemplateName());
        dataCheckResultVO.setCheckTemplateDesc(templatePO.getTemplateDesc());
        return dataCheckResultVO;
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> nifiSyncCheckData(DataCheckSyncDTO dto) {
        log.info("nifi流程进入数据校验...校验参数[{}]", JSONObject.toJSON(dto));

        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
        List<DataCheckLogsPO> dataCheckLogs = new ArrayList<>();
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            // 第一步：查询数据源信息
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            if (allDataSource == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS_STOP, null);
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getDatasourceId() == Integer.parseInt(dto.getFiDataDataSourceId())).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS_STOP, null);
            }
            DataSourceTypeEnum dataSourceType = dataSourceConVO.getConType();

            // 第二步：查询数据校验模块下的模板
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda()
                    .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATA_CHECK_MODULE.getValue())
                    .eq(TemplatePO::getTemplateState, 1)
                    .eq(TemplatePO::getDelFlag, 1);
            List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
            List<Long> templateIds = null;
            if (CollectionUtils.isNotEmpty(templatePOList)) {
                templateIds = templatePOList.stream().map(TemplatePO::getId).collect(Collectors.toList());
            }

            // 第三步：查询配置的表检查规则信息
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda()
                    .eq(DataCheckPO::getDatasourceId, dataSourceConVO.getId())
                    .eq(DataCheckPO::getDelFlag, 1)
                    .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .eq(DataCheckPO::getRuleExecuteNode, RuleExecuteNodeTypeEnum.SYNCHRONIZATION.getValue())
                    .eq(DataCheckPO::getTableUnique, dto.getTableUnique())
                    .eq(DataCheckPO::getTableBusinessType, dto.getTableBusinessType().getValue())
                    .in(DataCheckPO::getTemplateId, templateIds)
                    .orderByAsc(DataCheckPO::getRuleExecuteSort);
            List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
            List<Long> ruleIds = null;
            if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
                ruleIds = dataCheckPOList.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            }

            // 第四步：查询校验规则的扩展属性
            List<DataCheckExtendPO> dataCheckExtends = null;
            if (CollectionUtils.isNotEmpty(ruleIds)) {
                QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
                dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1).in(DataCheckExtendPO::getRuleId, ruleIds);
                dataCheckExtends = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
            }

            // 第五步：根据请求参数拼接SQL语句
            DataCheckSyncParamDTO dataCheckSyncParamDTO = nifiSync_RequestParamsToSql(dto, dataSourceType);

            dataCheckSyncParamDTO.setTableNameFormat(nifiSync_GetSqlFieldFormat(dataSourceType, dto.getTableName()));

            // 第六步：如果校验规则为空则无需进行数据校验，修改表状态字段为成功
            if (CollectionUtils.isEmpty(ruleIds)) {
                resultEnum = nifiSync_UpdateTableData_NoCheck(dataSourceConVO, dataCheckSyncParamDTO);
                return ResultEntityBuild.buildData(resultEnum, null);
            }

            // 第七步：已配置校验规则，根据规则校验表数据并记录校验日志
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                TemplatePO templatePO = templatePOList.stream()
                        .filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst()
                        .orElse(null);
                DataCheckExtendPO dataCheckExtendPO = dataCheckExtends.stream()
                        .filter(item -> item.getRuleId() == dataCheckPO.getId()).findFirst()
                        .orElse(null);
                if (templatePO == null) {
                    log.info("【nifiSyncCheckData】模板为空，当前检查规则标识为：" + dataCheckPO.getId());
                    log.info("【nifiSyncCheckData】模板为空，当前检查规则名称为：" + dataCheckPO.getRuleName());
                    continue;
                }
                if (dataCheckExtendPO == null) {
                    log.info("【nifiSyncCheckData】扩展属性为空，当前检查规则标识为：" + dataCheckPO.getId());
                    log.info("【nifiSyncCheckData】扩展属性为空，当前检查规则名称为：" + dataCheckPO.getRuleName());
                    continue;
                }

                // 获取表和字段信息，将其进行转义处理
                String tableName = "";
                String tableNameFormat = "";
                if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                    tableNameFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getSchemaName()) + ".";
                    tableName = dataCheckPO.getSchemaName() + ".";
                }
                tableNameFormat += nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dto.tablePrefix + dataCheckPO.getTableName());
                tableName += dataCheckPO.getTableName();

                String fieldName = "";
                String fieldNameFormat = "";
                if (StringUtils.isNotEmpty(dataCheckExtendPO.getFieldName())) {
                    fieldNameFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckExtendPO.getFieldName());
                    fieldName = dataCheckExtendPO.getFieldName();
                }
                dataCheckSyncParamDTO.setTableName(tableName);
                dataCheckSyncParamDTO.setTableNameFormat(tableNameFormat);
                dataCheckSyncParamDTO.setFieldName(fieldName);
                dataCheckSyncParamDTO.setFieldNameFormat(fieldNameFormat);

                DataCheckResultVO dataCheckResult = null;
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case NULL_CHECK:
                        dataCheckResult = nifiSync_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case RANGE_CHECK:
                        dataCheckResult = nifiSync_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case STANDARD_CHECK:
                        dataCheckResult = nifiSync_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case DUPLICATE_DATA_CHECK:
                        dataCheckResult = nifiSync_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case FLUCTUATION_CHECK:
                        dataCheckResult = nifiSync_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case PARENTAGE_CHECK:
                        dataCheckResult = nifiSync_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case REGEX_CHECK:
                        dataCheckResult = nifiSync_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                    case SQL_SCRIPT_CHECK:
                        dataCheckResult = nifiSync_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                        break;
                }
                if (dataCheckResult != null) {
                    // 第八步：验证规则是否全部校验通过，并记录日志
                    if (dataCheckResult.getCheckResult().equals(FAIL)) {
                        resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                    }
                    // 第九步：记录校验日志
                    if (dataCheckExtendPO.getRecordErrorData() == 1) {
                        DataCheckLogsPO dataCheckLogsPO = new DataCheckLogsPO();
                        dataCheckLogsPO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                        dataCheckLogsPO.setTemplateId(Math.toIntExact(templatePO.getId()));
                        dataCheckLogsPO.setFiDatasourceId(dataSourceConVO.getDatasourceId());
                        dataCheckLogsPO.setLogType(DataCheckLogTypeEnum.NIFI_SYNCHRONIZATION_DATA_CHECK_LOG.getValue());
                        dataCheckLogsPO.setSchemaName(dataCheckResult.getCheckSchema());
                        dataCheckLogsPO.setTableName(dataCheckSyncParamDTO.getTableName());
                        dataCheckLogsPO.setFieldName(dataCheckSyncParamDTO.getFieldName());
                        dataCheckLogsPO.setCheckTemplateName(dataCheckResult.getCheckTemplateName());
                        dataCheckLogsPO.setCheckBatchNumber(dataCheckSyncParamDTO.getBatchNumber());
                        dataCheckLogsPO.setCheckSmallBatchNumber(dataCheckSyncParamDTO.getSmallBatchNumber());
                        dataCheckLogsPO.setCheckTotalCount(dataCheckResult.getCheckTotalCount());
                        dataCheckLogsPO.setCheckFailCount(dataCheckResult.getCheckFailCount());
                        dataCheckLogsPO.setCheckResult(dataCheckResult.getCheckResult().toString());
                        dataCheckLogsPO.setCheckMsg(dataCheckResult.getCheckResultMsg());
                        dataCheckLogsPO.setErrorData(dataCheckResult.getCheckErrorData());
                        dataCheckLogs.add(dataCheckLogsPO);
                        // 清空校验不通过的数据字段，减少返回的字节流
                        dataCheckResult.checkErrorData = null;
                    }
                    dataCheckResults.add(dataCheckResult);
                }
            }

            // 第十步：默认先将此批次的数据状态全部改为成功，然后执行检查结果中返回的SQL语句
            String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                    f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                    fName = dataCheckSyncParamDTO.getFieldName(),
                    f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                    f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                    sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                    sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                    sql_W = dataCheckSyncParamDTO.getWarnFieldSql();

            StringBuilder updateSql_Builder = new StringBuilder();
            // 拼接修改表状态为通过的SQL
            String updateSql_Y = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            updateSql_Builder.append(updateSql_Y);
            // 拼接修改表状态为警告的SQL
            List<String> updateSql_W_List = dataCheckResults.stream().filter(t -> t.getCheckResult().equals(WARN)).map(DataCheckResultVO::getUpdateSql).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(updateSql_W_List)) {
                updateSql_W_List.forEach(item -> {
                    updateSql_Builder.append(item);
                });
            }
            // 拼接修改表状态为不通过的SQL
            List<String> updateSql_N_List = dataCheckResults.stream().filter(t -> t.getCheckResult().equals(FAIL)).map(DataCheckResultVO::getUpdateSql).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(updateSql_N_List)) {
                updateSql_N_List.forEach(item -> {
                    updateSql_Builder.append(item);
                });
            }
            resultEnum = nifiSync_UpdateTableData_ByCheck(dataSourceConVO, updateSql_Builder.toString());
            // 第十一步：保存数据检查日志
            if (CollectionUtils.isNotEmpty(dataCheckLogs)) {
                dataCheckLogsManage.saveLog(dataCheckLogs);
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(resultEnum, dataCheckResults);
    }

    public DataCheckSyncParamDTO nifiSync_RequestParamsToSql(DataCheckSyncDTO dto, DataSourceTypeEnum dataSourceType) {
        DataCheckSyncParamDTO params = new DataCheckSyncParamDTO();

        String checkFieldWhere = "",
                updateField_Y = "",
                updateField_N = "",
                updateField_R = "",
                batchNumber = "",
                smallBatchNumber = "";

        // 校验/更新依据字段
        if (CollectionUtils.isNotEmpty(dto.getCheckByFieldMap())) {
            for (Map.Entry<String, Object> entry : dto.getCheckByFieldMap().entrySet()) {
                String sqlWhereStr = nifiSync_GetSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                checkFieldWhere += " AND " + sqlWhereStr;

                // 批次号从依据字段中获取
                if (entry.getKey().equals("fidata_batch_code")) {
                    batchNumber = entry.getValue().toString();
                } else if (entry.getKey().equals("fidata_flow_batch_code")) {
                    smallBatchNumber = entry.getValue().toString();
                }
            }
        }

        // 校验通过修改字段
        if (CollectionUtils.isNotEmpty(dto.getUpdateFieldMap_Y())) {
            for (Map.Entry<String, Object> entry : dto.getUpdateFieldMap_Y().entrySet()) {
                String sqlWhereStr = nifiSync_GetSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                updateField_Y += sqlWhereStr + ",";
            }
            if (StringUtils.isNotEmpty(updateField_Y)) {
                updateField_Y = updateField_Y.substring(0, updateField_Y.length() - 1);
            }
        }

        // 校验不通过修改字段
        if (CollectionUtils.isNotEmpty(dto.getUpdateFieldMap_N())) {
            for (Map.Entry<String, Object> entry : dto.getUpdateFieldMap_N().entrySet()) {
                String sqlWhereStr = nifiSync_GetSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                updateField_N += sqlWhereStr + ",";
            }
            if (StringUtils.isNotEmpty(updateField_N)) {
                updateField_N = updateField_N.substring(0, updateField_N.length() - 1);
            }
        }

        // 校验不通过但校验规则为弱类型修改字段
        if (CollectionUtils.isNotEmpty(dto.getUpdateFieldMap_R())) {
            for (Map.Entry<String, Object> entry : dto.getUpdateFieldMap_R().entrySet()) {
                String sqlWhereStr = nifiSync_GetSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                updateField_R += sqlWhereStr + ",";
            }
            if (StringUtils.isNotEmpty(updateField_R)) {
                updateField_R = updateField_R.substring(0, updateField_R.length() - 1);
            }
        }

        params.setRequestDto(dto);
        params.setBatchNumber(batchNumber);
        params.setSmallBatchNumber(smallBatchNumber);
        params.setWhereFieldSql(checkFieldWhere);
        params.setSuccessFieldSql(updateField_Y);
        params.setFailFieldSql(updateField_N);
        params.setWarnFieldSql(updateField_R);
        params.setMsgField(nifiSync_GetSqlFieldFormat(dataSourceType, dto.getMsgField()));
        params.setUniqueField(nifiSync_GetSqlFieldFormat(dataSourceType, dto.getUniqueField()));
        return params;
    }

    public String nifiSync_GetSqlFieldWhere(DataSourceTypeEnum dataSourceTypeEnum) {
        String sqlWhereStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" + "=" + "'%s'" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" + "=" + "'%s'" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" + "=" + "'%s'" : "";
        return sqlWhereStr;
    }

    public String nifiSync_GetSqlFieldFormat(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" :
                                "";
        if (StringUtils.isNotEmpty(sqlFieldStr)) {
            sqlFieldStr = String.format(sqlFieldStr, fieldName);
        }
        return sqlFieldStr;
    }

    public ResultEnum nifiSync_UpdateTableData_NoCheck(DataSourceConVO dataSourceConVO, DataCheckSyncParamDTO paramDTO) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
            if (StringUtils.isNotEmpty(paramDTO.getMsgField())) {
                if (StringUtils.isNotEmpty(paramDTO.getSuccessFieldSql())) {
                    paramDTO.setSuccessFieldSql(paramDTO.getSuccessFieldSql() + String.format(",%s='%s' ", paramDTO.getMsgField(), "未配置同步校验规则，默认校验通过"));
                } else {
                    paramDTO.setSuccessFieldSql(paramDTO.getSuccessFieldSql() + String.format("%s='%s' ", paramDTO.getMsgField(), "未配置同步校验规则，默认校验通过"));
                }
            }
            String updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s", paramDTO.getTableNameFormat(), paramDTO.getSuccessFieldSql(), paramDTO.getWhereFieldSql());
            log.info("【nifiSync_UpdateTableData_NoCheck】待执行SQL：" + updateSql);
            AbstractCommonDbHelper.executeSql_Close(updateSql, connection);
        } catch (Exception ex) {
            resultEnum = ResultEnum.DATA_QUALITY_UPDATEDATA_ERROR;
            log.error("【nifiSync_UpdateTableData_NoCheck】执行SQL异常：" + ex);
        }
        return resultEnum;
    }

    public ResultEnum nifiSync_UpdateTableData_ByCheck(DataSourceConVO dataSourceConVO, String sql) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
            log.info("【nifiSync_UpdateTableData_ByCheck】待执行SQL：" + sql);
            AbstractCommonDbHelper.executeSql_Close(sql, connection);
        } catch (Exception ex) {
            resultEnum = ResultEnum.DATA_QUALITY_UPDATEDATA_ERROR;
            log.error("【nifiSync_UpdateTableData_ByCheck】执行SQL异常：" + ex);
        }
        return resultEnum;
    }

    public String nifiSync_getUpdateMsgFieldSql(DataSourceTypeEnum dataSourceTypeEnum, String msgField, String msg) {
        String updateMsgFieldSql = "";
        if (StringUtils.isEmpty(msgField) || StringUtils.isEmpty(msg))
            return updateMsgFieldSql;
        if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            String caseSql = String.format("CASE \n" +
                    "\tWHEN %s='' or %s is null THEN\n" +
                    "\t\t%s\n" +
                    "\tELSE\n" +
                    "\t\t%s\n" +
                    "END", "[" + msgField + "]", "[" + msgField + "]", "'" + msg + "'", "[" + msgField + "]+" + "'；" + msg + "'");
            updateMsgFieldSql = String.format("[%s]" + "=" + "%s ", msgField, caseSql);

        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            String caseSql = String.format("CASE \n" +
                    "\tWHEN %s='' or %s is null THEN\n" +
                    "\t\t%s\n" +
                    "\tELSE\n" +
                    "\t\t%s\n" +
                    "END", "\"" + msgField + "\"", "\"" + msgField + "\"", "'" + msg + "'", "\"" + msgField + "\"" + " || " + "'；" + msg + "'");
            updateMsgFieldSql = String.format("\"%s\"" + "=" + "%s ", msgField, caseSql);
        }
        return updateMsgFieldSql;
    }

    public List<Map<String, Object>> nifiSync_CheckTableData(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【nifiSync_UpdateTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        List<Map<String, Object>> mapList = AbstractCommonDbHelper.execQueryResultMaps(sql, connection);
        return mapList;
    }

    public JSONArray nifiSync_QueryTableData(DataSourceConVO dataSourceConVO, String sql) {
        // 实时建立数据库连接实时释放，防止连接等待时间过长导致超时异常
        log.info("【nifiSync_QueryTableData】待执行SQL：" + sql);
        Connection connection = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray dataArray = AbstractCommonDbHelper.execQueryResultArrays(sql, connection);
        return dataArray;
    }

    public DataCheckResultVO nifiSync_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，获取校验结果
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = String.format("SELECT %s,%s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '')", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, f_Name),
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '')", f_uniqueIdName, t_Name, f_where, f_Name, f_Name);

        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            String checkTotalCount = null;
            for (Map.Entry<String, Object> entry : entries) {
                checkTotalCount = entry.getValue().toString();
            }
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        JSONArray errorDataList = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        }

        // 第三步：判断是否通过空值检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), "空值检查未通过");
            }
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，，但检查规则未设置强规则将继续放行数据", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
        }

        // 释放集合对象
        maps = null;
        errorDataList = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                 DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，获取校验结果
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = "", sql_UpdateErrorData = "";

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                // 序列范围
                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                String sql_InString = list.stream().map(s -> "'" + "'").collect(Collectors.joining(", "));
                sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, sql_InString);
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_Name, t_Name, f_where, f_Name, sql_InString);
                break;
            case VALUE_RANGE:
                // 取值范围
                Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                String sql_BetweenAnd = String.format("CASE(%s AS NUMERIC) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                    sql_BetweenAnd = String.format("%s::NUMERIC NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                }
                sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where, sql_BetweenAnd);
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s", f_Name, t_Name, f_where, sql_BetweenAnd);
                break;
            case DATE_RANGE:
                // 日期范围
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                String[] timeRange = timeRangeString.split("~");
                LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);
                sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND %s))",
                        f_uniqueIdName, f_Name, t_Name, f_where, f_Name, f_Name, f_Name, startTime, endTime);
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND %s))",
                        f_Name, t_Name, f_where, f_Name, f_Name, f_Name, startTime, endTime);
                break;
        }

        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            String checkTotalCount = maps.get(0).get("checkTotalCount").toString();
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        JSONArray errorDataList = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        }

        // 第三步：判断是否通过值域检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), String.format("值域检查-%s检查未通过", rangeCheckTypeEnum.getName()));
            }
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        }
        // 设置具体的校验类型
        dataCheckResultVO.setCheckTemplateName(String.format("%s-%s", TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));

        // 释放集合对象
        maps = null;
        errorDataList = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                    DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，查询待校验的数据
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where);

        // 第三步：查询待校验的数据
        JSONArray errorDataList = new JSONArray();
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        JSONArray data = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (data != null && data.size() == 0) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                String fieldKey = jsonObject.getString(f_uniqueIdName);
                Object fieldValue = jsonObject.get(fName);

                // 第四步：检查数据是否符合规范
                switch (standardCheckTypeEnum) {
                    case DATE_FORMAT:
                        // 日期格式
                        List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            boolean hasValidDate = DateTimeUtils.isValidDateFormat(fieldValue.toString(), list);
                            if (!hasValidDate) {
                                errorDataList.add(jsonObject);
                            }
                        }
                        break;
                    case URL_ADDRESS:
                        // 字符精度长度范围
                        int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                        int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            List<String> values = Arrays.asList(fieldValue.toString().split(dataCheckExtendPO.getStandardCheckTypeLengthSeparator()));
                            if (values.stream().count() >= 2) {
                                String value = values.get(Math.toIntExact(values.stream().count() - 1));
                                if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                    errorDataList.add(jsonObject);
                                }
                            }
                        }
                        break;
                    case BASE64_BYTE_STREAM:
                        // URL地址
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            boolean validURL = RegexUtils.isValidURL(fieldValue.toString(), false);
                            if (!validURL) {
                                errorDataList.add(jsonObject);
                            }
                        }
                        break;
                    case CHARACTER_PRECISION_LENGTH_RANGE:
                        // BASE64字节流
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            boolean validBase64String = RegexUtils.isBase64String(fieldValue.toString(), false);
                            if (!validBase64String) {
                                errorDataList.add(jsonObject);
                            }
                        }
                        break;
                }
                String checkTotalCount = String.valueOf(data.size());
                dataCheckResultVO.setCheckTotalCount(checkTotalCount);
            }
        }
        String checkFailCount = String.valueOf(errorDataList.size());
        dataCheckResultVO.setCheckFailCount(checkFailCount);

        // 第五步：判断字段值是否通过规范检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), String.format("规范检查-%s检查未通过", standardCheckTypeEnum.getName()));
            }
            List<String> uniqueIdList = new ArrayList<>();
            for (Object obj : errorDataList) {
                JSONObject jsonObject = (JSONObject) obj;
                String uniqueId = (String) jsonObject.get(f_uniqueIdName);
                uniqueIdList.add(uniqueId);
            }
            String sql_InString = uniqueIdList.stream().map(s -> "'" + "'").collect(Collectors.joining(", "));
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        }
        // 设置具体的校验类型
        dataCheckResultVO.setCheckTemplateName(String.format("%s-%s", TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));

        // 释放集合对象
        data = null;
        errorDataList = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                         DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，获取校验结果
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = "",
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();

        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        String updateFieldWhereStr = "";
        for (String item : fieldNames) {
            String fieldFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), item);
            f_Name += fieldFormat + ",";
            updateFieldWhereStr += String.format(" AND sy.%s = %s.%s", fieldFormat, t_Name, fieldFormat);
        }
        HashMap<String, Object> checkByFieldMap = dataCheckSyncParamDTO.getRequestDto().getCheckByFieldMap();
        if (CollectionUtils.isNotEmpty(checkByFieldMap)) {
            for (Map.Entry<String, Object> entry : checkByFieldMap.entrySet()) {
                String fieldFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), entry.getKey());
                updateFieldWhereStr += String.format(" AND %s.%s='%s'", t_Name, fieldFormat, entry.getValue());
            }
        }

        String sql_QueryTotalCount = String.format("SELECT COUNT(*) AS checkTotalCount FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = String.format("SELECT %s, COUNT(*) AS repetitionCount FROM %s WHERE 1=1 %s \n" +
                "GROUP BY %s HAVING COUNT(*) > 1;", f_Name, t_Name, f_where, f_Name);
        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            String checkTotalCount = maps.get(0).get("checkTotalCount").toString();
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        JSONArray jsonArray = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            dataCheckResultVO.setCheckFailCount(String.valueOf(maps.size()));
        }

        // 第三步：判断是否通过重复数据检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (!CollectionUtils.isNotEmpty(jsonArray)) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), "重复数据检查未通过");
            }
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s\n" +
                        "SET %s\n" +
                        "FROM (\n" +
                        " SELECT %s FROM %s WHERE 1=1 %s\n" +
                        " GROUP BY %s HAVING COUNT(*) > 1\n" +
                        ") sy\n" +
                        "WHERE 1=1 %s;", t_Name, sql_N + updateMsgFieldSql, f_Name, t_Name, f_where, f_Name, updateFieldWhereStr);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s\n" +
                        "SET %s\n" +
                        "FROM (\n" +
                        " SELECT %s FROM %s WHERE 1=1 %s\n" +
                        " GROUP BY %s HAVING COUNT(*) > 1\n" +
                        ") sy\n" +
                        "WHERE 1=1 %s;", t_Name, sql_W + updateMsgFieldSql, f_Name, t_Name, f_where, f_Name, updateFieldWhereStr);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        }

        // 释放集合对象
        maps = null;
        jsonArray = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                       DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，获取校验结果
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) AS checkTotalCount FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = "";

        boolean isValid = true;
        double thresholdValue = dataCheckExtendPO.getFluctuateCheckValue();
        double realityValue = 0.0;
        FluctuateCheckTypeEnum fluctuateCheckTypeEnum = FluctuateCheckTypeEnum.getEnum(dataCheckExtendPO.getFluctuateCheckType());
        switch (fluctuateCheckTypeEnum) {
            case AVG:
                sql_QueryCheckData = String.format("SELECT AVG(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                break;
            case MIN:
                sql_QueryCheckData = String.format("SELECT MIN(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                break;
            case MAX:
                sql_QueryCheckData = String.format("SELECT MAX(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                break;
            case SUM:
                sql_QueryCheckData = String.format("SELECT SUM(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                break;
            case COUNT:
                sql_QueryCheckData = String.format("SELECT COUNT(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                break;
        }

        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            String checkTotalCount = maps.get(0).get("checkTotalCount").toString();
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(maps)) {
            realityValue = Double.parseDouble(maps.get(0).get("realityValue").toString());
        }

        // 第四步：判断字段值是否通过波动检查
        FluctuateCheckOperatorEnum fluctuateCheckOperatorEnum = FluctuateCheckOperatorEnum.getEnumByName(dataCheckExtendPO.getFluctuateCheckOperator());
        switch (fluctuateCheckOperatorEnum) {
            case GREATER_THAN:
                if (realityValue > thresholdValue) {
                    isValid = false;
                }
                break;
            case GREATER_THAN_OR_EQUAL:
                if (realityValue >= thresholdValue) {
                    isValid = false;
                }
                break;
            case EQUAL:
                if (realityValue == thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN:
                if (realityValue < thresholdValue) {
                    isValid = false;
                }
                break;
            case LESS_THAN_OR_EQUAL:
                if (realityValue <= thresholdValue) {
                    isValid = false;
                }
                break;
        }

        // 第五步：判断字段值是否通过波动检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (!isValid) {
            // 聚合类的验证，此版本修改表状态
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_W, f_where);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                // 字段名称
                jsonObject.put("FieldName", fName);
                // 聚合值
                jsonObject.put("AggregateValue", realityValue);
                // 波动阈值
                jsonObject.put("ThresholdValue", thresholdValue);
                jsonArray.add(jsonObject);
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        }

        // 释放集合对象
        maps = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                     DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 调用元数据接口查询表上下游血缘信息
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String upTableName = "";
        String downTableName = "";

        boolean isValid = false;
        ParentageCheckTypeEnum parentageCheckTypeEnum = ParentageCheckTypeEnum.getEnum(dataCheckExtendPO.getParentageCheckType());
        switch (parentageCheckTypeEnum) {
            case CHECK_UPSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(upTableName)) {
                    isValid = true;
                }
                break;
            case CHECK_DOWNSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(downTableName)) {
                    isValid = true;
                }
                break;
            case CHECK_UPSTREAM_AND_DOWNSTREAM_BLOODLINE:
                if (StringUtils.isNotEmpty(upTableName) && StringUtils.isNotEmpty(downTableName)) {
                    isValid = true;
                }
                break;
        }

        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                 DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：组装并执行SQL语句，查询待校验的数据
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where);

        // 第三步：查询待校验的数据
        JSONArray errorDataList = new JSONArray();
        JSONArray data = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                // 第四步：判断字段值是否通过正则表达式验证
                String fieldValue = jsonObject.getString(fName);
                boolean isValid = RegexUtils.isValidPattern(fieldValue, dataCheckExtendPO.getRegexpCheckValue(), false);
                if (!isValid) {
                    errorDataList.add(jsonObject);
                }
            }
            String checkFailCount = String.valueOf(data.size());
            dataCheckResultVO.setCheckFailCount(checkFailCount);
        }
        dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));

        // 第三步：判断是否通过正则表达式检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), "正则表达式检查未通过");
            }
            List<String> uniqueIdList = new ArrayList<>();
            for (Object obj : errorDataList) {
                JSONObject jsonObject = (JSONObject) obj;
                String uniqueId = (String) jsonObject.get(f_uniqueIdName);
                uniqueIdList.add(uniqueId);
            }
            String sql_InString = uniqueIdList.stream().map(s -> "'" + "'").collect(Collectors.joining(", "));
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，正则表达式为：【%s】", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据，正则表达式为：【%s】", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过，正则表达式为：【%s】", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
        }

        // 释放集合对象
        data = null;
        errorDataList = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                     DataCheckExtendPO dataCheckExtendPO, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：建立数据库连接执行SQL查询语句
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                tName = dataCheckSyncParamDTO.getTableName(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        JSONArray jsonArray = nifiSync_QueryTableData(dataSourceConVO, dataCheckExtendPO.getSqlCheckValue());

        // 固定返回checkstate，通过为1，未通过为0，取第一行的checkstate字段判断
        boolean isValid = false;
        String checkState = "0";
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.containsKey("checkstate")) {
                checkState = jsonObject.getString("checkstate");
                if (StringUtils.isNotEmpty(checkState) && checkState.equals("1")) {
                    isValid = true;
                }
            }
        }

        // 第三步：判断是否通过SQL检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (!isValid) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), "正则表达式检查未通过");
            }
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N + updateMsgFieldSql, f_where);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_W + updateMsgFieldSql, f_where);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过，但检查规则未设置强规则将继续放行数据", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            }
            dataCheckResultVO.setUpdateSql(updateSql);
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
        } else {
            // 组装修改语句
            updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_Y, f_where);
            dataCheckResultVO.setUpdateSql(updateSql);
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】通过", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        }

        // 释放集合对象
        jsonArray = null;
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_GetCheckResultBasisInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                              DataCheckExtendPO dataCheckExtendPO) {
        DataCheckResultVO dataCheckResultVO = new DataCheckResultVO();
        dataCheckResultVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
        dataCheckResultVO.setRuleName(dataCheckPO.getRuleName());
        dataCheckResultVO.setCheckDataBase(dataSourceConVO.getConDbname());
        dataCheckResultVO.setCheckSchema(dataCheckPO.getSchemaName());
        dataCheckResultVO.setCheckTable(dataCheckPO.getTableName());
        dataCheckResultVO.setCheckTableUnique(dataCheckPO.getTableUnique());
        dataCheckResultVO.setCheckField(dataCheckExtendPO.getFieldName());
        dataCheckResultVO.setCheckFieldUnique(dataCheckExtendPO.getFieldUnique());
        dataCheckResultVO.setCheckType(RuleCheckTypeEnum.getEnum(dataCheckPO.getRuleCheckType()).getName());
        dataCheckResultVO.setCheckTemplateName(templatePO.getTemplateName());
        dataCheckResultVO.setCheckTemplateDesc(templatePO.getTemplateDesc());
        return dataCheckResultVO;
    }
}