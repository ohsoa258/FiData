package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.dataservice.BuildDataServiceHelper;
import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
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
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
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
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private DataCheckExtendManageImpl dataCheckExtendManageImpl;

    @Resource
    private UserClient userClient;

    @Resource
    private UserHelper userHelper;

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
        if (dto.getSourceType() == SourceTypeEnum.FiData) {
            int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(dto.getSourceType(), dto.getDatasourceId());
            if (idByDataSourceId == 0) {
                return ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS;
            }
            dto.setDatasourceId(idByDataSourceId);
        }
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
            List<DataCheckExtendPO> dataCheckExtends = DataCheckExtendMap.INSTANCES.dtoToPo(dto.getDataCheckExtends());
            dataCheckExtends.forEach(t -> {
                t.setRuleId(Math.toIntExact(dataCheckPO.getId()));
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
            List<DataCheckExtendPO> dataCheckExtends = DataCheckExtendMap.INSTANCES.dtoToPo(dto.getDataCheckExtends());
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

                DataCheckResultVO dataCheckResult = null;
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case NULL_CHECK:
                        dataCheckResult = interface_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case RANGE_CHECK:
                        dataCheckResult = interface_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case STANDARD_CHECK:
                        dataCheckResult = interface_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case DUPLICATE_DATA_CHECK:
                        dataCheckResult = interface_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case FLUCTUATION_CHECK:
                        dataCheckResult = interface_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case PARENTAGE_CHECK:
                        dataCheckResult = interface_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case REGEX_CHECK:
                        dataCheckResult = interface_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                    case SQL_SCRIPT_CHECK:
                        dataCheckResult = interface_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                        break;
                }
                if (dataCheckResult != null) {
                    dataCheckResults.add(dataCheckResult);
                }
            }

            // 第六步：验证规则是否全部校验通过
            if (CollectionUtils.isNotEmpty(dataCheckResults)) {
                List<DataCheckResultVO> failResults = dataCheckResults.stream().filter(t -> t.getCheckResult().equals(FAIL)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(failResults)) {
                    resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
            log.error("【interfaceCheckData】执行异常：" + ex);
        }
        return ResultEntityBuild.buildData(resultEnum, dataCheckResults);
    }

    public DataCheckResultVO interface_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过空值检查
        List<String> fieldValueFilters = fieldValues.stream().filter(item -> StringUtils.isEmpty(item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, fieldName, TemplateTypeEnum.NULL_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, fieldName, TemplateTypeEnum.NULL_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过值域验证
        boolean isValid = true;
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                // 序列范围
                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                list = RegexUtils.subtractValid(fieldValues, list, true);
                isValid = CollectionUtils.isEmpty(list);
                break;
            case VALUE_RANGE:
                // 取值范围
                Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                for (String item : fieldValues) {
                    if (StringUtils.isNotEmpty(item)) {
                        Integer value = Integer.valueOf(item);
                        if (value < lowerBound_Int || value > upperBound_Int) {
                            isValid = false;
                            break;
                        }
                    } else {
                        isValid = false;
                        break;
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
                            isValid = false;
                            break;
                        }
                    } else {
                        isValid = false;
                        break;
                    }
                }
                break;
        }
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, fieldName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, fieldName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过规范检查
        boolean isValid = false;
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        switch (standardCheckTypeEnum) {
            case DATE_FORMAT:
                // 日期格式
                List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                boolean hasValidDate = false;
                for (String item : list) {
                    hasValidDate = DateTimeUtils.isValidDate(fieldValues, item);
                    if (hasValidDate) {
                        isValid = true;
                        break;
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
                                isValid = false;
                                break;
                            }
                        }
                    } else {
                        isValid = false;
                        break;
                    }
                }
                break;
            case BASE64_BYTE_STREAM:
                // URL地址
                isValid = RegexUtils.isValidURL(fieldValues, false);
                break;
            case CHARACTER_PRECISION_LENGTH_RANGE:
                // BASE64字节流
                isValid = RegexUtils.isBase64String(fieldValues, false);
                break;
        }

        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, fieldName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, fieldName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值并判断是否重复
        boolean isValid = true;
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            String value = "";
            for (String fieldName : fieldNames) {
                if (jsonObject.containsKey(fieldName)) {
                    value += jsonObject.getString(fieldName);
                } else {
                    dataCheckResultVO.setCheckResult(FAIL);
                    dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
                    return dataCheckResultVO;
                }
            }
            if (fieldValues.contains(value.toLowerCase())) {
                isValid = false;
                break;
            }
            fieldValues.add(value.toLowerCase());
        }

        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
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
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
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
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过正则表达式验证
        boolean isValid = RegexUtils.isValidPattern(fieldValues, dataCheckExtendPO.getRegexpCheckValue(), false);
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, fieldName, TemplateTypeEnum.REGEX_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, fieldName, TemplateTypeEnum.REGEX_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：建立数据库连接执行SQL查询语句
        Connection conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        List<Map<String, Object>> maps = AbstractCommonDbHelper.execQueryResultMaps(dataCheckExtendPO.getSqlCheckValue(), conn);
        // 固定返回checkstate，通过为1，未通过为0，取第一行的checkstate字段判断
        boolean isValid = false;
        if (CollectionUtils.isNotEmpty(maps)) {
            Map<String, Object> objectMap = maps.get(0);
            if (objectMap.containsKey("checkstate")) {
                Object checkState = objectMap.get("checkstate");
                if (checkState != null && checkState.toString().equals("1")) {
                    isValid = true;
                }
            }
        }

        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_GetCheckResultBasisInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
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
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
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
            Connection connection = dataSourceConManageImpl.getStatement(dataSourceType, dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());

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
            String tableName = dto.getTableName();
            String tableNameFormat = nifiSync_GetSqlFieldFormat(dataSourceType, dto.getTableName());
            List<String> requestParamsList = nifiSync_RequestParamsToSql(dto, dataSourceType);
            String whereFieldSql = requestParamsList.get(0);
            String successFieldSql = requestParamsList.get(1);
            String failFieldSql = requestParamsList.get(2);
            String warnFieldSql = requestParamsList.get(3);
            String msgField = requestParamsList.get(4);

            // 第五步：如果校验规则为空则无需进行数据校验，修改表状态字段为成功
            if (CollectionUtils.isEmpty(ruleIds)) {
                ResultEnum resultEnum = nifiSync_UpdateTableData_NoCheck(connection, tableNameFormat, successFieldSql, whereFieldSql, msgField);
                return ResultEntityBuild.buildData(resultEnum, null);
            }

            // 第六步：已配置校验规则，根据规则校验表数据
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

                DataCheckResultVO dataCheckResult = null;
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                switch (templateType) {
                    case NULL_CHECK:
                        dataCheckResult = nifiSync_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case RANGE_CHECK:
                        dataCheckResult = nifiSync_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case STANDARD_CHECK:
                        dataCheckResult = nifiSync_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case DUPLICATE_DATA_CHECK:
                        List<String> fieldNameList = new ArrayList<>();
                        List<String> list = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
                        for (String item : list) {
                            String fieldFormat = nifiSync_GetSqlFieldFormat(dataSourceType, item);
                            fieldNameList.add(fieldFormat);
                        }
                        dataCheckResult = nifiSync_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case FLUCTUATION_CHECK:
                        dataCheckResult = nifiSync_FluctuationCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case PARENTAGE_CHECK:
                        dataCheckResult = nifiSync_ParentageCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case REGEX_CHECK:
                        dataCheckResult = nifiSync_RegexCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                    case SQL_SCRIPT_CHECK:
                        dataCheckResult = nifiSync_SqlScriptCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                        break;
                }
                if (dataCheckResult != null) {
                    dataCheckResults.add(dataCheckResult);
                }
            }

            // 第七步：根据校验结果修改表状态字段为成功、失败、警告
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                TemplatePO templatePO = templatePOList.stream().filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                List<DataCheckExtendPO> dataCheckExtendFilters = dataCheckExtends.stream().filter(item -> item.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(dataCheckExtendFilters)) {
                    continue;
                }

                List<String> fieldNames = new ArrayList<>();
                for (int k = 0; k < dataCheckExtendFilters.size(); k++) {
                    DataCheckExtendPO dataCheckExtendPO = dataCheckExtendFilters.get(k);
                    if (StringUtils.isNotEmpty(dataCheckExtendPO.getFieldUnique())) {
                        FiDataMetaDataTreeDTO fiDataMetaDataTree_Field = fiDataMetaDataTree_Table.getChildren().stream().
                                filter(f -> f.getId().equals(dataCheckExtendPO.getFieldUnique())).findFirst().orElse(null);
                        if (fiDataMetaDataTree_Field != null) {
                            fieldNames.add(fiDataMetaDataTree_Field.getLabel());
                        }
                    }
                }

                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 获取待校验的结果集合
                        if (CollectionUtils.isEmpty(fieldNames)) {
                            log.info("同步校验-" + dataCheckPO.getRuleName() + "-" + tableName + "-表字段名称为空");
                            continue;
                        }
                        ResultEntity<List<SyncCheckInfoVO>> checkResultSqls = GetCheckFieldRule_Sync(templatePO, dataSourceInfo,
                                dataSourceType, dataCheckPO, dataCheckExtendFilters.get(0), tableFullName, fieldNames.get(0));
                        if (checkResultSqls != null && checkResultSqls.getCode() == ResultEnum.SUCCESS.getCode()) {
                            checkResultSqlList.addAll(checkResultSqls.getData());
                        }
                        break;
                }
            }
            if (CollectionUtils.isNotEmpty(checkResultSqlList)) {
                result = UpdateTableData_Sync(connection, dataSourceType, dtoPramsList, checkResultSqlList);
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return result;
    }

    public List<String> nifiSync_RequestParamsToSql(DataCheckSyncDTO dto, DataSourceTypeEnum dataSourceType) {
        List<String> params = new ArrayList<>();

        String checkFieldWhere = "",
                updateField_Y = "",
                updateField_N = "",
                updateField_R = "";

        // 校验/更新依据字段
        if (CollectionUtils.isNotEmpty(dto.getCheckByFieldMap())) {
            for (Map.Entry<String, Object> entry : dto.getCheckByFieldMap().entrySet()) {
                String sqlWhereStr = nifiSync_GetSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                checkFieldWhere += " AND " + sqlWhereStr;
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

        params.add(checkFieldWhere);
        params.add(updateField_Y);
        params.add(updateField_N);
        params.add(updateField_R);
        params.add(nifiSync_GetSqlFieldFormat(dataSourceType, dto.getMsgField()));
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

    public ResultEnum nifiSync_UpdateTableData_NoCheck(Connection connection, String tableName, String fieldNames, String sqlWheres, String msgField) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            if (StringUtils.isNotEmpty(msgField)) {
                if (StringUtils.isNotEmpty(fieldNames)) {
                    fieldNames += String.format(",%s='%s' ", msgField, "未配置同步校验规则，默认校验通过");
                } else {
                    fieldNames += String.format("%s='%s' ", msgField, "未配置同步校验规则，默认校验通过");
                }
            }
            String updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s", tableName, fieldNames, sqlWheres);
            log.info("【nifiSync_UpdateTableData】待执行SQL：" + updateSql);
            AbstractCommonDbHelper.executeSql_Close(updateSql, connection);
        } catch (Exception ex) {
            resultEnum = ResultEnum.DATA_QUALITY_UPDATEDATA_ERROR;
            log.error("【nifiSync_UpdateTableData】执行SQL异常：" + ex);
        }
        return resultEnum;
    }

    public DataCheckResultVO nifiSync_NullCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过空值检查
        List<String> fieldValueFilters = fieldValues.stream().filter(item -> StringUtils.isEmpty(item)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, fieldName, TemplateTypeEnum.NULL_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, fieldName, TemplateTypeEnum.NULL_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过值域验证
        boolean isValid = true;
        RangeCheckTypeEnum rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                // 序列范围
                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                list = RegexUtils.subtractValid(fieldValues, list, true);
                isValid = CollectionUtils.isEmpty(list);
                break;
            case VALUE_RANGE:
                // 取值范围
                Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                for (String item : fieldValues) {
                    if (StringUtils.isNotEmpty(item)) {
                        Integer value = Integer.valueOf(item);
                        if (value < lowerBound_Int || value > upperBound_Int) {
                            isValid = false;
                            break;
                        }
                    } else {
                        isValid = false;
                        break;
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
                            isValid = false;
                            break;
                        }
                    } else {
                        isValid = false;
                        break;
                    }
                }
                break;
        }
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, fieldName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, fieldName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过规范检查
        boolean isValid = false;
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        switch (standardCheckTypeEnum) {
            case DATE_FORMAT:
                // 日期格式
                List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                boolean hasValidDate = false;
                for (String item : list) {
                    hasValidDate = DateTimeUtils.isValidDate(fieldValues, item);
                    if (hasValidDate) {
                        isValid = true;
                        break;
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
                                isValid = false;
                                break;
                            }
                        }
                    } else {
                        isValid = false;
                        break;
                    }
                }
                break;
            case BASE64_BYTE_STREAM:
                // URL地址
                isValid = RegexUtils.isValidURL(fieldValues, false);
                break;
            case CHARACTER_PRECISION_LENGTH_RANGE:
                // BASE64字节流
                isValid = RegexUtils.isBase64String(fieldValues, false);
                break;
        }

        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, fieldName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, fieldName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值并判断是否重复
        boolean isValid = true;
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            String value = "";
            for (String fieldName : fieldNames) {
                if (jsonObject.containsKey(fieldName)) {
                    value += jsonObject.getString(fieldName);
                } else {
                    dataCheckResultVO.setCheckResult(FAIL);
                    dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
                    return dataCheckResultVO;
                }
            }
            if (fieldValues.contains(value.toLowerCase())) {
                isValid = false;
                break;
            }
            fieldValues.add(value.toLowerCase());
        }

        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
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
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查未通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】-【%s】检查通过", tableName, dataCheckExtendPO.getFieldName(), TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckOperatorEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
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
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        String fieldName = dataCheckExtendPO.getFieldName();
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fieldName + "】");
            return dataCheckResultVO;
        }

        // 第三步：判断字段值是否通过正则表达式验证
        boolean isValid = RegexUtils.isValidPattern(fieldValues, dataCheckExtendPO.getRegexpCheckValue(), false);
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】未通过", tableName, fieldName, TemplateTypeEnum.REGEX_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，【%s】通过", tableName, fieldName, TemplateTypeEnum.REGEX_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);

        // 第二步：建立数据库连接执行SQL查询语句
        Connection conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        List<Map<String, Object>> maps = AbstractCommonDbHelper.execQueryResultMaps(dataCheckExtendPO.getSqlCheckValue(), conn);
        // 固定返回checkstate，通过为1，未通过为0，取第一行的checkstate字段判断
        boolean isValid = false;
        if (CollectionUtils.isNotEmpty(maps)) {
            Map<String, Object> objectMap = maps.get(0);
            if (objectMap.containsKey("checkstate")) {
                Object checkState = objectMap.get("checkstate");
                if (checkState != null && checkState.toString().equals("1")) {
                    isValid = true;
                }
            }
        }

        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        if (!isValid) {
            dataCheckResultVO.setCheckResult(FAIL);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】未通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO nifiSync_GetCheckResultBasisInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO) {
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


    /**
     * @return com.fisk.common.core.response.ResultEntity<java.util.List < com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>>
     * @description 同步校验，修改表数据
     * @author dick
     * @date 2022/6/2 14:48
     * @version v1.0
     * @params connection
     * @params dataSourceTypeEnum
     * @params dtoPramsList
     * @params checkResultSqlList
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultEntity<List<DataCheckResultVO>> UpdateTableData_Sync(Connection connection, DataSourceTypeEnum dataSourceTypeEnum, List<String> dtoPramsList, List<SyncCheckInfoVO> checkResultSqlList) {
        List<DataCheckResultVO> results = new ArrayList<>();
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            // @Transactional(rollbackFor = Exception.class) 开启事务，避免部分成功部分失败
            // 第一步：获取需要校验的数据结果集
            JSONArray jsonArray = GetCheckFieldRuleResult_Sync(connection, checkResultSqlList, dtoPramsList.get(0));
            if (jsonArray == null || jsonArray.size() == 0) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
            }
            // 第二步：获取校验后的结果
            CheckFieldRuleResult_Sync(checkResultSqlList, jsonArray);
            // 第三步：是否存在校验失败的数据
            List<SyncCheckInfoVO> dataCheckResults_N = checkResultSqlList.stream().filter(t -> !t.checkResult).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dataCheckResults_N)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, null);
            }
            GetUpdateSql_Sync(dataCheckResults_N, dtoPramsList, dataSourceTypeEnum);
            StringBuilder builder_UpdateSql = new StringBuilder();
            if (StringUtils.isNotEmpty(dtoPramsList.get(1))) {
                builder_UpdateSql.append(String.format("UPDATE %s SET %s WHERE 1=1 %s; \n\n", checkResultSqlList.get(0).checkTable, dtoPramsList.get(1), dtoPramsList.get(0)));
            }
            for (SyncCheckInfoVO sqlInfo : dataCheckResults_N) {
                builder_UpdateSql.append(sqlInfo.updateSql);
            }
            for (SyncCheckInfoVO syncCheckInfo : checkResultSqlList) {
                if (resultEnum == ResultEnum.SUCCESS && syncCheckInfo.getCheckRule() == RuleCheckTypeEnum.STRONG_RULE &&
                        !syncCheckInfo.checkResult) {
                    resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                }
                DataCheckResultVO dataCheckResult = new DataCheckResultVO();
                dataCheckResult.setRuleId(syncCheckInfo.ruleId);
                dataCheckResult.setRuleName(syncCheckInfo.ruleName);
                dataCheckResult.setCheckDataBase(syncCheckInfo.checkDataBase);
                dataCheckResult.setCheckTable(syncCheckInfo.tableName);
                dataCheckResult.setCheckField(syncCheckInfo.fieldName);
                dataCheckResult.setCheckType(syncCheckInfo.checkTypeName);
                dataCheckResult.setCheckDesc(syncCheckInfo.checkDesc);
                dataCheckResult.setCheckResult(syncCheckInfo.checkResult ? "success" : "fail");
                dataCheckResult.setCheckResultMsg(syncCheckInfo.checkResultMsg);
                dataCheckResult.setCheckRule(syncCheckInfo.checkRule.getValue());
                results.add(dataCheckResult);
            }
            Statement st = null;
            if (builder_UpdateSql != null && builder_UpdateSql.toString().length() > 0) {
                st = connection.createStatement();
                assert st != null;
                st.execute(builder_UpdateSql.toString());
            }
            if (st != null) {
                st.close();
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, ex);
        }
        return ResultEntityBuild.buildData(resultEnum, results);
    }

    /**
     * @return com.fisk.common.core.response.ResultEnum
     * @description 同步校验，修改表数据为成功状态
     * @author dick
     * @date 2022/6/6 16:08
     * @version v1.0
     * @params connection
     * @params dtoPramsList
     * @params tableName
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum UpdateTableDataToSuccess_Sync(Connection connection, DataSourceTypeEnum dataSourceType, List<String> dtoPramsList, String tableName) {
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            StringBuilder builder_UpdateSql = new StringBuilder();
            String updateSetSql = "";
            if (StringUtils.isNotEmpty(dtoPramsList.get(1))) {
                updateSetSql = dtoPramsList.get(1);
            }
            if (StringUtils.isNotEmpty(dtoPramsList.get(4))) {
                if (StringUtils.isNotEmpty(updateSetSql)) {
                    updateSetSql += ",";
                }
                updateSetSql += getSqlMsgField(dataSourceType, dtoPramsList.get(4), "未配置校验规则，默认校验通过");
            }
            if (StringUtils.isNotEmpty(updateSetSql)) {
                builder_UpdateSql.append(String.format("UPDATE %s SET %s WHERE 1=1 %s; \n\n", tableName, updateSetSql, dtoPramsList.get(0)));
            }
            Statement st = null;
            if (builder_UpdateSql != null && builder_UpdateSql.toString().length() > 0) {
                st = connection.createStatement();
                assert st != null;
                st.execute(builder_UpdateSql.toString());
            }
            if (st != null) {
                st.close();
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_UPDATEDATA_ERROR, ex);
        }
        return resultEnum;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.SyncCheckInfoVO>
     * @description 同步校验，查询组装校验规则
     * @author dick
     * @date 2022/6/2 14:49
     * @version v1.0
     * @params templatePO
     * @params dataSourceConPO
     * @params dataSourceTypeEnum
     * @params dataCheckPO
     * @params dataCheckExtendFilters
     */
    public ResultEntity<List<SyncCheckInfoVO>> GetCheckFieldRule_Sync(TemplatePO templatePO, DataSourceConVO dataSourceConPO,
                                                                      DataSourceTypeEnum dataSourceTypeEnum, DataCheckPO dataCheckPO,
                                                                      DataCheckExtendPO dataCheckExtend,
                                                                      String tableFullNameReq, String fieldNameReq) {
        List<SyncCheckInfoVO> checkResultSqls = new ArrayList<>();
        String tableName = tableFullNameReq;
        String fieldName = fieldNameReq;
        String checkTableName = tableFullNameReq;
        String checkFieldName = fieldNameReq;

        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(fieldName)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TABLECONFIGURATION_SENT_CHANGES, null);
        }
        if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            checkFieldName = "`" + checkFieldName + "`";
            checkTableName = "`" + checkTableName + "`";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            checkFieldName = "[" + checkFieldName + "]";
            checkTableName = "[" + checkTableName + "]";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            checkFieldName = String.format("\"%s\"", checkFieldName);
            checkFieldName = String.format("\"%s\"", checkFieldName);
        }
        String[] split = dataCheckExtend.checkType.split(",");
        DataCheckTypeEnum dataCheckTypeEnum = DataCheckTypeEnum.getEnum(dataCheckExtend.dataCheckType);
        for (String x : split) {
            CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(x));
            SyncCheckInfoVO checkResultSql = new SyncCheckInfoVO();
            checkResultSql.setCheckType(checkTypeEnum);
            checkResultSql.setCheckTypeName(checkTypeEnum.getName());
            if (checkTypeEnum == CheckTypeEnum.DATA_CHECK) {
                checkResultSql.setDataCheckType(dataCheckTypeEnum);
                checkResultSql.setCheckTypeName(dataCheckTypeEnum.getName());
            }
            checkResultSql.setCheckTable(checkTableName);
            checkResultSql.setCheckField(checkFieldName);
            checkResultSql.setTableName(tableName);
            checkResultSql.setFieldName(fieldName);
            checkResultSql.setTableUnique(dataCheckPO.getTableUnique());
            checkResultSql.setFieldUnique(dataCheckExtend.getFieldUnique());
            checkResultSql.setCheckFieldWhere(dataCheckExtend.fieldWhere);
            checkResultSql.setCheckRule(RuleCheckTypeEnum.getEnum(dataCheckPO.getCheckRule()));
            checkResultSql.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            checkResultSql.setRuleName(dataCheckPO.getRuleName());
            checkResultSql.setCheckDesc(templatePO.templateDesc);
            checkResultSql.setCheckDataBase(dataSourceConPO.conDbname);
            checkResultSqls.add(checkResultSql);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, checkResultSqls);
    }

    /**
     * @return com.alibaba.fastjson.JSONArray
     * @description 同步校验，获取校验数据
     * @author dick
     * @date 1 14:49
     * @version v1.0
     * @params conn
     * @params checkResultSqlList
     * @params checkFieldWhere
     */
    public JSONArray GetCheckFieldRuleResult_Sync(Connection conn, List<SyncCheckInfoVO> checkResultSqlList, String checkFieldWhere) {
        String tableName = checkResultSqlList.get(0).getCheckTable();
        List<String> fileds = checkResultSqlList.stream().map(SyncCheckInfoVO::getCheckField).distinct().collect(Collectors.toList());
        String filedStr = SqlParmUtils.parseListToParamStr(fileds);
        String query_SqlStr = String.format("SELECT %s FROM %s WHERE 1=1 %s;", filedStr, tableName, checkFieldWhere);
        Statement st = null;
        JSONArray array = new JSONArray();
        try {
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(query_SqlStr);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(columnName);
                    jsonObj.put(columnName, value);
                }
                array.add(jsonObj);
            }
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, ex);
        }
        return array;
    }

    /**
     * @return void
     * @description 同步校验，根据校验数据得到校验结果
     * @author dick
     * @date 2022/6/2 14:50
     * @version v1.0
     * @params checkResultSqlList
     * @params data
     */
    public void CheckFieldRuleResult_Sync(List<SyncCheckInfoVO> checkResultSqlList, JSONArray data) {
        HashMap<String, List<String>> fieldMap = new HashMap<>();
        for (SyncCheckInfoVO fieldVo : checkResultSqlList) {
            List<String> fieldValues = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                if (jsonObject.containsKey(fieldVo.fieldName)) {
                    fieldValues.add(jsonObject.getString(fieldVo.fieldName));
                }
            }
            if (CollectionUtils.isNotEmpty(fieldValues))
                fieldMap.put(fieldVo.fieldName, fieldValues);
        }
        for (SyncCheckInfoVO checkInfo : checkResultSqlList) {
            List<String> fieldValues = fieldMap.get(checkInfo.fieldName);
            List<String> fieldValueFilters = null;
            boolean isValid = true;
            String sqlInPram = "";
            switch (checkInfo.checkType) {
                case DATA_CHECK:
                    if (checkInfo.dataCheckType == DataCheckTypeEnum.TEXTLENGTH_CHECK) {
                        int fieldLength = Integer.parseInt(checkInfo.checkFieldWhere);
                        fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isNotEmpty(iter) && iter.length() > fieldLength).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                            isValid = false;
                        }
                    } else if (checkInfo.dataCheckType == DataCheckTypeEnum.DATEFORMAT_CHECK) {
                        fieldValueFilters = DateTimeUtils.ValidDate(fieldValues, checkInfo.checkFieldWhere);
                        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                            isValid = false;
                        }
                    } else if (checkInfo.dataCheckType == DataCheckTypeEnum.SEQUENCERANGE_CHECK) {
                        String[] splitArr = checkInfo.checkFieldWhere.split(",");
                        List<String> whereList = new ArrayList<>();
                        Collections.addAll(whereList, splitArr);
                        fieldValueFilters = RegexUtils.subtractValid(fieldValues, whereList, true);
                        if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                            isValid = false;
                        }
                    }
                    if (!isValid) {
                        sqlInPram = SqlParmUtils.getInParam(fieldValueFilters);
                        checkInfo.setSqlWhere(sqlInPram);
                    }
                    break;
                case NONEMPTY_CHECK:
                    fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isEmpty(iter)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                        isValid = false;
                        checkInfo.setSqlWhere("('',null)");
                    }
                    break;
                case UNIQUE_CHECK:
                    fieldValueFilters = fieldValues.stream().distinct().collect(Collectors.toList());
                    if (fieldValueFilters.size() != data.size()) {
                        isValid = false;
                        fieldValueFilters = RegexUtils.subtractValid(fieldValues, fieldValueFilters, false);
                        sqlInPram = SqlParmUtils.getInParam(fieldValueFilters);
                        checkInfo.setSqlWhere(sqlInPram);
                    }
                    break;
            }
            if (!isValid) {
                checkInfo.setCheckResult(false);
                checkInfo.setCheckResultMsg(String.format("%s字段%s未通过", checkInfo.fieldName, checkInfo.getCheckTypeName()));
            } else {
                checkInfo.setCheckResult(true);
                checkInfo.setCheckResultMsg(String.format("%s字段%s已通过", checkInfo.fieldName, checkInfo.getCheckTypeName()));
            }
        }
    }

    /**
     * @return void
     * @description 同步校验，根据校验结果组装修改SQL
     * @author dick
     * @date 2022/6/2 14:51
     * @version v1.0
     * @params dataCheckResults_N
     * @params dtoPramsList
     * @params dataSourceTypeEnum
     */
    public void GetUpdateSql_Sync(List<SyncCheckInfoVO> dataCheckResults_N, List<String> dtoPramsList, DataSourceTypeEnum dataSourceTypeEnum) {
        // 校验不通过，根据参数调整表数据
        List<SyncCheckInfoVO> checkList_Rule = new ArrayList<>();
        String tableName = dataCheckResults_N.get(0).checkTable;
        List<SyncCheckInfoVO> strong_Rule = dataCheckResults_N.stream().filter(t -> (t.getCheckRule() == RuleCheckTypeEnum.STRONG_RULE)).collect(Collectors.toList());
        List<SyncCheckInfoVO> weak_Rule = dataCheckResults_N.stream().filter(t -> (t.getCheckRule() == RuleCheckTypeEnum.WEAK_RULE)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(weak_Rule)) {
            checkList_Rule.addAll(weak_Rule);
        }
        if (CollectionUtils.isNotEmpty(strong_Rule)) {
            checkList_Rule.addAll(strong_Rule);
        }
        for (SyncCheckInfoVO t : checkList_Rule) {
            String updateFieldSql = "";
            String updateWhereSql = "";
            String updateMsgFieldSql = getSqlMsgField(dataSourceTypeEnum, dtoPramsList.get(4), t.checkResultMsg);
            // 判断是否是弱类型，弱类型则读取弱类型修改字段集合
            if (t.checkRule == RuleCheckTypeEnum.WEAK_RULE) {
                updateFieldSql = dtoPramsList.get(3);
            } else if (t.checkRule == RuleCheckTypeEnum.STRONG_RULE) {
                updateFieldSql = dtoPramsList.get(2);
            }
            if (StringUtils.isNotEmpty(updateMsgFieldSql)) {
                if (StringUtils.isNotEmpty(updateFieldSql)) {
                    updateMsgFieldSql = "," + updateMsgFieldSql;
                }
            }
            if (StringUtils.isNotEmpty(dtoPramsList.get(0))) {
                updateWhereSql = dtoPramsList.get(0);
            }
            if (StringUtils.isNotEmpty(t.sqlWhere)) {
                updateWhereSql += String.format(" AND %s IN %s", t.checkField, t.sqlWhere);
            }
            String updateSql = String.format("UPDATE %s SET %s %s WHERE 1=1 %s; \n\n", tableName, updateFieldSql, updateMsgFieldSql, updateWhereSql);
            t.setUpdateSql(updateSql);
        }
    }


    /**
     * @return java.lang.String
     * @description 同步校验，组装消息字段
     * @author dick
     * @date 2022/6/2 14:52
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params msgField
     * @params msg
     */
    public String getSqlMsgField(DataSourceTypeEnum dataSourceTypeEnum, String msgField, String msg) {
        String updateMsgFieldSql = "";
        if (StringUtils.isEmpty(msgField) || StringUtils.isEmpty(msg))
            return updateMsgFieldSql;
        if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            updateMsgFieldSql = String.format("`%s`" + "=" + "%s ", msgField, "CONCAT_WS(\"；\",`" + msgField + "`,'" + msg + "')");
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
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
}