package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.Excel.ExcelDto;
import com.fisk.common.core.utils.Dto.Excel.RowDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDto;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.dbutils.dto.DataBaseInfoDTO;
import com.fisk.common.core.utils.dbutils.dto.DataSourceInfoDTO;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.core.utils.office.excel.ExcelReportUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.dto.dataquality.datasource.QueryTableRuleDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckConditionMap;
import com.fisk.datagovernance.map.dataquality.DataCheckExtendMap;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.DatacheckCodeService;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.*;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.enums.ValueRangeTypeEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private DataCheckConditionMapper dataCheckConditionMapper;

    @Resource
    private DataCheckConditionManageImpl dataCheckConditionManageImpl;

    @Resource
    DatacheckStandardsGroupServiceImpl datacheckStandardsGroupService;

    @Resource
    private DatacheckCodeService datacheckCodeService;

    @Resource
    private DataCheckLogsManageImpl dataCheckLogsManage;

    @Resource
    private DataCheckLogsMapper dataCheckLogsMapper;

    @Resource
    private QualityReportLogMapper qualityReportLogMapper;

    @Resource
    private AttachmentInfoMapper attachmentInfoMapper;

    @Resource
    private UserHelper userHelper;

    @Resource
    DataAccessClient dataAccessClient;

    @Resource
    DataModelClient dataModelClient;

    @Resource
    MdmClient mdmClient;

    @Resource
    UserClient userClient;

    @Resource
    DataManageClient dataManageClient;

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;
    @Value("${spring.datasource.username}")
    private String dataBaseUserName;
    @Value("${spring.datasource.password}")
    private String dataBasePassWord;


    @Value("${file.excelFilePath}")
    private String excelFilePath;

    @Value("${checkStandards}")
    private Boolean checkStandards;

    private static final String WARN = "warn";
    private static final String FAIL = "fail";
    private static final String SUCCESS = "success";

    @Override
    public PageDTO<DataCheckVO> getAllRule(DataCheckQueryDTO query) {
        // 第一步：参数验证
        PageDTO<DataCheckVO> page = new PageDTO<>();
        List<DataCheckVO> filterRule = new ArrayList<>();
        if (query == null) {
            return page;
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
            List<Long> templateIdList = null;
            if (query.getTemplateId() != 0) {
                templateIdList = new ArrayList<>();
                templateIdList.add(query.getTemplateId());
            }
            List<DataCheckVO> allRule = baseMapper.getAllRule(query.getCheckProcess(),
                    query.getTableUnique(), query.getRuleName(), null, templateIdList);
            if (CollectionUtils.isEmpty(allRule)) {
                return page;
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
                return page;
            }
            List<Integer> ruleIds = filterRule.stream().map(DataCheckVO::getId).distinct().collect(Collectors.toList());
            List<DataCheckExtendVO> dataCheckExtendVOList = dataCheckExtendMapper.getDataCheckExtendByRuleIdList(ruleIds);
            if (CollectionUtils.isNotEmpty(dataCheckExtendVOList)) {
                filterRule.forEach(t -> {
                    DataCheckExtendVO dataCheckExtendVO = dataCheckExtendVOList.stream().filter(k -> k.getRuleId() == t.getId()).findFirst().orElse(null);
                    t.setDataCheckExtend(dataCheckExtendVO);
                });
            }
            List<DataCheckConditionVO> dataCheckConditionVOList = dataCheckConditionMapper.getDataCheckExtendByRuleIdList(ruleIds);
            if (CollectionUtils.isNotEmpty(dataCheckConditionVOList)) {
                filterRule.forEach(t -> {
                    List<DataCheckConditionVO> dataCheckConditionVOS = dataCheckConditionVOList.stream().filter(k -> k.getRuleId() == t.getId()).collect(Collectors.toList());
                    t.setDataCheckCondition(dataCheckConditionVOS);
                });
            }
            // 第五步：排序分页设置
            query.current = query.current - 1;
            page.setTotal(Long.valueOf(filterRule.size()));
            page.setTotalPage((long) Math.ceil(1.0 * filterRule.size() / query.getSize()));
            filterRule = filterRule.stream().sorted(
                    // 1.先按照表名称排正序，并处理tableAlias为空的情况
                    Comparator.comparing(DataCheckVO::getTableAlias, Comparator.nullsFirst(Comparator.naturalOrder()))
                            // 2.再按照执行节点排正序，并处理ruleExecuteNode为空的情况
                            .thenComparing(DataCheckVO::getRuleExecuteNode, Comparator.nullsFirst(Comparator.naturalOrder()))
                            // 3.再按照创建时间排倒叙，并处理创建时间为空的情况
                            .thenComparing(DataCheckVO::getCreateTime, Comparator.nullsFirst(Comparator.reverseOrder()))
            ).skip((query.current - 1 + 1) * query.size).limit(query.size).collect(Collectors.toList());
            page.setItems(filterRule);
        } catch (Exception ex) {
            log.error("【getAllRule】查询校验规则列表异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        }
        return page;
    }

    @Override
    public DataCheckRuleSearchWhereVO getRuleSearchWhere() {
        // 数据校验搜索条件
        DataCheckRuleSearchWhereVO dataCheckRuleSearchWhereVO = new DataCheckRuleSearchWhereVO();

        // 查询数据校验搜索条件-执行环节
        List<DataCheckRuleSearchWhereMapVO> checkProcessMap = new ArrayList<>();
        List<RuleExecuteNodeTypeEnum> ruleExecuteNodeTypeEnumList = new ArrayList<>();
        ruleExecuteNodeTypeEnumList.add(RuleExecuteNodeTypeEnum.BEFORE_SYNCHRONIZATION);
        ruleExecuteNodeTypeEnumList.add(RuleExecuteNodeTypeEnum.SYNCHRONIZATION);
        ruleExecuteNodeTypeEnumList.add(RuleExecuteNodeTypeEnum.AFTER_SYNCHRONIZATION);
        ruleExecuteNodeTypeEnumList.forEach(t -> {
            DataCheckRuleSearchWhereMapVO searchWhereMap_CheckProcess = new DataCheckRuleSearchWhereMapVO();
            searchWhereMap_CheckProcess.setText(t.getName());
            searchWhereMap_CheckProcess.setValue(t.getValue());
            checkProcessMap.add(searchWhereMap_CheckProcess);
        });

        // 查询数据校验搜索条件-表名称
        List<DataCheckRuleSearchWhereMapVO> tableFullNameMap = new ArrayList<>();
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1);
        List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                // 表标识为空 或者 表名称为空则跳过
                if (StringUtils.isEmpty(dataCheckPO.getTableUnique()) || StringUtils.isEmpty(dataCheckPO.getTableName())) {
                    continue;
                }
                DataCheckRuleSearchWhereMapVO searchWhereMap_TableFullName = tableFullNameMap.stream().filter(t -> t.getValue().toString().equals(dataCheckPO.getTableUnique())).findFirst().orElse(null);
                if (searchWhereMap_TableFullName == null) {
                    String tableFullName = StringUtils.isNotEmpty(dataCheckPO.getSchemaName()) ? dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName() : dataCheckPO.getTableName();
                    searchWhereMap_TableFullName = new DataCheckRuleSearchWhereMapVO();
                    searchWhereMap_TableFullName.setText(tableFullName);
                    searchWhereMap_TableFullName.setValue(dataCheckPO.getTableUnique());
                    tableFullNameMap.add(searchWhereMap_TableFullName);
                }
            }
        }

        // 查询数据校验搜索条件-规则模板名称
        List<DataCheckRuleSearchWhereMapVO> templateMap = new ArrayList<>();
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATA_CHECK_MODULE.getValue())
                .orderByAsc(TemplatePO::getTemplateSort);
        List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(templatePOList)) {
            templatePOList.forEach(template -> {
                DataCheckRuleSearchWhereMapVO searchWhereMap_Template = new DataCheckRuleSearchWhereMapVO();
                searchWhereMap_Template.setText(template.getTemplateName());
                searchWhereMap_Template.setValue(template.getId());
                templateMap.add(searchWhereMap_Template);
            });
        }

        dataCheckRuleSearchWhereVO.setCheckProcessMap(checkProcessMap);
        dataCheckRuleSearchWhereVO.setTableFullNameMap(tableFullNameMap);
        dataCheckRuleSearchWhereVO.setTemplateMap(templateMap);

        return dataCheckRuleSearchWhereVO;
    }

    @Override
    public DataCheckRuleSearchWhereVO getDataCheckLogSearchWhere() {
        // 问题稽查知识库搜索条件
        DataCheckRuleSearchWhereVO dataCheckRuleSearchWhereVO = new DataCheckRuleSearchWhereVO();

        // 问题稽查知识库搜索条件-报告批次号
        List<DataCheckRuleSearchWhereMapVO> reportBatchNumberMap = new ArrayList<>();
        // 问题稽查知识库搜索条件-表名称
        List<DataCheckRuleSearchWhereMapVO> tableFullNameMap = new ArrayList<>();

        QueryWrapper<DataCheckLogsPO> dataCheckLogsPOQueryWrapper = new QueryWrapper<>();
        dataCheckLogsPOQueryWrapper.lambda().eq(DataCheckLogsPO::getDelFlag, 1)
                .eq(DataCheckLogsPO::getLogType, DataCheckLogTypeEnum.SUBSCRIPTION_REPORT_RULE_CHECK_LOG.getValue())
                .orderByDesc(DataCheckLogsPO::getCreateTime);
        List<DataCheckLogsPO> dataCheckLogsPOS = dataCheckLogsMapper.selectList(dataCheckLogsPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckLogsPOS)) {

            List<String> checkBatchNumberList = dataCheckLogsPOS.stream().map(DataCheckLogsPO::getCheckBatchNumber).collect(Collectors.toList());
            QueryWrapper<QualityReportLogPO> qualityReportLogPOQueryWrapper = new QueryWrapper<>();
            qualityReportLogPOQueryWrapper.lambda().eq(QualityReportLogPO::getDelFlag, 1)
                    .in(QualityReportLogPO::getReportBatchNumber, checkBatchNumberList);
            List<QualityReportLogPO> qualityReportLogPOS = qualityReportLogMapper.selectList(qualityReportLogPOQueryWrapper);

            dataCheckLogsPOS.forEach(dataCheckLog -> {
                String reportName = "";
                if (CollectionUtils.isNotEmpty(qualityReportLogPOS)) {
                    QualityReportLogPO qualityReportLogPO = qualityReportLogPOS.stream().filter(t -> t.getReportBatchNumber().equals(dataCheckLog.getCheckBatchNumber())).findFirst().orElse(null);
                    if (qualityReportLogPO != null) {
                        reportName = qualityReportLogPO.getReportName();
                    }
                }
                // 报告批次号
                DataCheckRuleSearchWhereMapVO searchWhereMap_reportBatchNumber = reportBatchNumberMap
                        .stream().filter(t -> t.getValue().toString().equals(dataCheckLog.getCheckBatchNumber()))
                        .findFirst().orElse(null);
                if (searchWhereMap_reportBatchNumber == null) {
                    searchWhereMap_reportBatchNumber = new DataCheckRuleSearchWhereMapVO();
                    String text = StringUtils.isNotEmpty(reportName) ? reportName + "(" + dataCheckLog.getCheckBatchNumber() + ")" : dataCheckLog.getCheckBatchNumber();
                    searchWhereMap_reportBatchNumber.setText(text);
                    searchWhereMap_reportBatchNumber.setValue(dataCheckLog.getCheckBatchNumber());
                    reportBatchNumberMap.add(searchWhereMap_reportBatchNumber);
                }

                // 表名称
                String tableFullName = StringUtils.isNotEmpty(dataCheckLog.getSchemaName()) ? dataCheckLog.getSchemaName() + "." + dataCheckLog.getTableName() : dataCheckLog.getTableName();
                DataCheckRuleSearchWhereMapVO searchWhereMap_TableFullName = tableFullNameMap
                        .stream().filter(t -> t.getValue().toString().equals(tableFullName))
                        .findFirst().orElse(null);
                if (searchWhereMap_TableFullName == null) {
                    searchWhereMap_TableFullName = new DataCheckRuleSearchWhereMapVO();
                    searchWhereMap_TableFullName.setText(tableFullName);
                    searchWhereMap_TableFullName.setValue(tableFullName);
                    tableFullNameMap.add(searchWhereMap_TableFullName);
                }
            });
        }

        // 问题稽查知识库搜索条件-规则模板名称
        List<DataCheckRuleSearchWhereMapVO> templateMap = new ArrayList<>();
        QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
        templatePOQueryWrapper.lambda().eq(TemplatePO::getDelFlag, 1)
                .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATA_CHECK_MODULE.getValue())
                .orderByAsc(TemplatePO::getTemplateSort);
        List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(templatePOList)) {
            templatePOList.forEach(template -> {
                DataCheckRuleSearchWhereMapVO searchWhereMap_Template = new DataCheckRuleSearchWhereMapVO();
                searchWhereMap_Template.setText(template.getTemplateName());
                searchWhereMap_Template.setValue(template.getId());
                templateMap.add(searchWhereMap_Template);
            });
        }

        dataCheckRuleSearchWhereVO.setReportBatchNumberMap(reportBatchNumberMap);
        dataCheckRuleSearchWhereVO.setTableFullNameMap(tableFullNameMap);
        dataCheckRuleSearchWhereVO.setTemplateMap(templateMap);

        return dataCheckRuleSearchWhereVO;
    }

    @Override
    public List<DataCheckVO> getRuleByIds(List<Integer> ids) {
        return baseMapper.getRuleByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataCheckDTO dto) {
        // 如果是FiData的Tree节点，需要将平台数据源ID转换为数据质量数据源ID
        if (dto.getSourceType() == SourceTypeEnum.FiData) {
            int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(dto.getSourceType(), dto.getDatasourceId());
            if (idByDataSourceId == 0) {
                return ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS;
            }
            dto.setDatasourceId(idByDataSourceId);
        }
        //第一步：验证模板是否存在以及表规则是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.getTemplateId());
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        QueryWrapper<DataCheckPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(DataCheckPO::getRuleName, dto.getRuleName())
                .eq(DataCheckPO::getDelFlag, 1);
        List<DataCheckPO> dataCheckPOList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
            return ResultEnum.DATA_QUALITY_CHECK_CODE_ALREADY_EXISTS;
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
        if (dto.getDataCheckExtend() != null) {
            DataCheckExtendPO dataCheckExtendPO = DataCheckExtendMap.INSTANCES.dtoToPo(dto.getDataCheckExtend());
            dataCheckExtendPO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            dataCheckExtendMapper.insert(dataCheckExtendPO);
        }
        //第五步：保存数据校验的检查条件
        if (CollectionUtils.isNotEmpty(dto.getDataCheckCondition())) {
            List<DataCheckConditionPO> dataCheckExtendPOs = DataCheckConditionMap.INSTANCES.dtoListToPoList(dto.getDataCheckCondition());
            if (CollectionUtils.isNotEmpty(dataCheckExtendPOs)) {
                dataCheckExtendPOs.forEach(t -> {
                    t.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                });
                dataCheckConditionManageImpl.saveBatch(dataCheckExtendPOs);
            }
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
        QueryWrapper<DataCheckPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(DataCheckPO::getRuleName, dto.getRuleName())
                .eq(DataCheckPO::getDelFlag, 1)
                .ne(DataCheckPO::getId, dto.getId());
        List<DataCheckPO> dataCheckPOList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
            return ResultEnum.DATA_QUALITY_CHECK_CODE_ALREADY_EXISTS;
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
        if (dto.getDataCheckExtend() != null) {
            DataCheckExtendPO dataCheckExtendPO = DataCheckExtendMap.INSTANCES.dtoToPo(dto.getDataCheckExtend());
            dataCheckExtendPO.setRuleId(dto.getId());
            dataCheckExtendMapper.updateById(dataCheckExtendPO);
        }
        //第五步：保存数据校验的检查条件
        dataCheckConditionMapper.updateByRuleId(dto.getId());
        if (CollectionUtils.isNotEmpty(dto.getDataCheckCondition())) {
            List<DataCheckConditionPO> dataCheckExtendPOs = DataCheckConditionMap.INSTANCES.dtoListToPoList(dto.getDataCheckCondition());
            if (CollectionUtils.isNotEmpty(dataCheckExtendPOs)) {
                dataCheckExtendPOs.forEach(t -> {
                    t.setRuleId(Math.toIntExact(dto.getId()));
                });
                dataCheckConditionManageImpl.saveBatch(dataCheckExtendPOs);
            }
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
        // 删除数据校验的检查条件
        dataCheckConditionMapper.updateByRuleId(id);
        return baseMapper.deleteByIdWithFill(dataCheckPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(DataCheckWebDTO dto) {

        log.info("api流程进入数据校验...校验参数[{}]", JSONObject.toJSON(dto));

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
                    tableName = dataCheckPO.getSchemaName() + ".";
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
                try {
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
                    dto.body.put(dataCheckPO.getTableUnique(), dataCheckResult.checkSuccessData);
                } catch (Exception ruleEx) {
                    if (dataCheckResult == null) {
                        dataCheckResult = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
                    }
                    dataCheckResult.setCheckResult(FAIL);
                    dataCheckResult.setCheckResultMsg(String.format("代号：【%s】，触发系统异常，%s未通过", dataCheckPO.getRuleName(), templateType.getName()));
                    log.error("【interfaceCheckData】执行检查规则时触发系统异常：" + ruleEx);
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
                        dataCheckLogsPO.setRuleName(dataCheckPO.getRuleName());
                        dataCheckLogsPO.setTemplateId(Math.toIntExact(templatePO.getId()));
                        dataCheckLogsPO.setFiDatasourceId(dataSourceConVO.getDatasourceId());
                        dataCheckLogsPO.setLogType(DataCheckLogTypeEnum.INTERFACE_DATA_CHECK_LOG.getValue());
                        dataCheckLogsPO.setSchemaName(dataCheckResult.getCheckSchema());
                        dataCheckLogsPO.setTableName(dataCheckResult.getCheckTable());
                        dataCheckLogsPO.setFieldName(dataCheckResult.getCheckField());
                        dataCheckLogsPO.setCheckTemplateName(dataCheckResult.getCheckTemplateName());
                        dataCheckLogsPO.setCheckBatchNumber(dataCheckSyncParamDTO.getBatchNumber());
                        dataCheckLogsPO.setCheckSmallBatchNumber(dataCheckSyncParamDTO.getSmallBatchNumber());
                        dataCheckLogsPO.setCheckTotalCount(dataCheckResult.getCheckTotalCount());
                        dataCheckLogsPO.setCheckFailCount(dataCheckResult.getCheckFailCount());
                        dataCheckLogsPO.setCheckResult(dataCheckResult.getCheckResult().toString());
                        dataCheckLogsPO.setCheckMsg(dataCheckResult.getCheckResultMsg());
                        dataCheckLogsPO.setCheckRuleIllustrate(dataCheckPO.getRuleIllustrate());
                        dataCheckLogsPO.setErrorData(dataCheckResult.getCheckErrorData());
                        dataCheckLogs.add(dataCheckLogsPO);
                        // 清空校验不通过的数据字段，减少返回的字节流
                        dataCheckResult.checkErrorData = null;
                    }
                    dataCheckResults.add(dataCheckResult);
                }
            }
            dataCheckResults = dataCheckResults.stream().map(i -> {
                JSONArray data = dto.body.get(i.getCheckTableUnique());
                i.setCheckSuccessData(data);
                return i;
            }).collect(Collectors.toList());
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
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        JSONArray errorDataList = new JSONArray();
        JSONArray successDataList = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                String value = jsonObject.getString(fName);
                if (StringUtils.isEmpty(value)) {
                    errorDataList.add(jsonObject);
                } else {
                    successDataList.add(jsonObject);
                }
            } else {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
                return dataCheckResultVO;
            }
        }

        // 第三步：判断字段值是否通过空值检查
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.NULL_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.NULL_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.NULL_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(successDataList);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.NULL_CHECK.getName()));
            dataCheckResultVO.setCheckSuccessData(successDataList);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RangeCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                  DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);
        Integer datacheckGroupId = dataCheckPO.datacheckGroupId;
        DatacheckStandardsGroupPO groupPO = datacheckStandardsGroupService.getById(datacheckGroupId);
        StandardsDTO standardsDTO = new StandardsDTO();
        if (datacheckGroupId != null){
            ResultEntity<StandardsDTO> standards = dataManageClient.getStandards(groupPO.getStandardsMenuId());
            if (standards.code == ResultEnum.SUCCESS.getCode()) {
                standardsDTO = standards.data;
            } else {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        if (standardsDTO == null){
            log.info("数据元未查询到匹配数据请检查并清理脏数据,数据元id"+datacheckGroupId);
        }
        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        JSONArray errorDataList = new JSONArray();
        JSONArray successDataList = new JSONArray();
        RangeCheckTypeEnum rangeCheckTypeEnum = null;
        ValueRangeTypeEnum valueRangeType = standardsDTO.getValueRangeType();
        if (datacheckGroupId != null){
            rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        }else {
            switch (valueRangeType){
                case DATASET:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.SEQUENCE_RANGE;
                    break;
                case VALUE:
                case VALUE_RANGE:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.VALUE_RANGE;
                    break;
                case NONE:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.NONE;
                    break;
            }
        }
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {

                String checkValue = jsonObject.getString(fName);

                // 第三步：判断字段值是否通过值域验证
                switch (rangeCheckTypeEnum) {
                    case SEQUENCE_RANGE:
                        if (datacheckGroupId != null){
                            List<CodeSetDTO> codeSetDTOList = standardsDTO.getCodeSetDTOList();
                            List<String> fieldValues = new ArrayList<>();
                            fieldValues.add(checkValue);
                            List<String> list = codeSetDTOList.stream().map(v -> v.getName()).collect(Collectors.toList());
                            List<String> valid = RegexUtils.subtractValid(fieldValues, list, true);
                            if (CollectionUtils.isNotEmpty(valid)) {
                                errorDataList.add(jsonObject);
                            } else {
                                successDataList.add(jsonObject);
                            }
                        }else {
                            // 序列范围
                            if (dataCheckExtendPO.rangeType == 2) {
                                String childrenQuery = String.format("SELECT %s FROM %s", dataCheckExtendPO.getCheckFieldName(), dataCheckExtendPO.getCheckTableName());
                                Connection conn = null;
                                Statement st = null;
                                try {
                                    Class.forName(dataSourceConVO.conType.getDriverName());
                                    conn = DriverManager.getConnection(dataSourceConVO.conStr, dataSourceConVO.conAccount, dataSourceConVO.conPassword);
                                    st = conn.createStatement();
                                    //无需判断ddl语句执行结果,因为如果执行失败会进catch
                                    log.info("开始执行脚本:{}", childrenQuery);
                                    ResultSet resultSet = st.executeQuery(childrenQuery);
                                    List<String> list = resultSetToList(resultSet);
                                    List<String> fieldValues = new ArrayList<>();
                                    fieldValues.add(checkValue);
                                    List<String> valid = RegexUtils.subtractValid(fieldValues, list, true);
                                    if (CollectionUtils.isNotEmpty(valid)) {
                                        errorDataList.add(jsonObject);
                                    } else {
                                        successDataList.add(jsonObject);
                                    }
                                } catch (Exception e) {
                                    errorDataList.add(jsonObject);
                                    dataCheckResultVO.setCheckResult(FAIL);
                                    dataCheckResultVO.setCheckResultMsg("查询关联表" + dataCheckExtendPO.getCheckTableName() + "异常,请检查连接是否正常");
                                } finally {
                                    try {
                                        assert st != null;
                                        st.close();
                                        conn.close();
                                    } catch (SQLException e) {
                                        errorDataList.add(jsonObject);
                                        dataCheckResultVO.setCheckResult(FAIL);
                                        dataCheckResultVO.setCheckResultMsg("数据库连接关闭异常");
                                        log.error("数据库连接关闭异常", e);
                                    }
                                }
                            } else {
                                List<String> fieldValues = new ArrayList<>();
                                fieldValues.add(checkValue);
                                List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                                List<String> valid = RegexUtils.subtractValid(fieldValues, list, true);
                                if (CollectionUtils.isNotEmpty(valid)) {
                                    errorDataList.add(jsonObject);
                                } else {
                                    successDataList.add(jsonObject);
                                }
                            }
                        }
                        break;
                    case VALUE_RANGE:
                        if (datacheckGroupId != null){
                            if (valueRangeType == ValueRangeTypeEnum.VALUE){
                                Double rangeCheckValue = Double.valueOf(standardsDTO.getValueRange());
                                String rangeCheckOneWayOperator = standardsDTO.getSymbols();

                                if (StringUtils.isEmpty(checkValue)) {
                                    errorDataList.add(jsonObject);
                                } else {
                                    Double value = Double.valueOf(checkValue);
                                    boolean isValid = false;

                                    switch (rangeCheckOneWayOperator) {
                                        case ">":
                                            isValid = value > rangeCheckValue;
                                            break;
                                        case ">=":
                                            isValid = value >= rangeCheckValue;
                                            break;
                                        case "=":
                                            isValid = value.equals(rangeCheckValue); // Use equals for Double comparison
                                            break;
                                        case "!=":
                                            isValid = !value.equals(rangeCheckValue);
                                            break;
                                        case "<":
                                            isValid = value < rangeCheckValue;
                                            break;
                                        case "<=":
                                            isValid = value <= rangeCheckValue;
                                            break;
                                        default:
                                            log.info("值域检查-取值范围-单向取值-未匹配到有效的运算符：" + rangeCheckOneWayOperator);
                                            break;
                                    }
                                    if (isValid) {
                                        successDataList.add(jsonObject);
                                    } else {
                                        errorDataList.add(jsonObject);
                                    }
                                }
                            }else if (valueRangeType == ValueRangeTypeEnum.VALUE_RANGE){
                                // 取值范围-区间取值
                                Double lowerBound_Int = Double.valueOf(standardsDTO.getValueRange());
                                Double upperBound_Int = Double.valueOf(standardsDTO.getValueRangeMax());
                                if (StringUtils.isNotEmpty(checkValue)) {
                                    Double value = Double.valueOf(checkValue);
                                    if (value < lowerBound_Int || value > upperBound_Int) {
                                        errorDataList.add(jsonObject);
                                    } else {
                                        successDataList.add(jsonObject);
                                    }
                                } else {
                                    errorDataList.add(jsonObject);
                                }
                            }
                        }else {
                            RangeCheckValueRangeTypeEnum rangeCheckValueRangeTypeEnum = RangeCheckValueRangeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckValueRangeType());
                            if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.INTERVAL_VALUE) {
                                // 取值范围-区间取值
                                Double lowerBound_Int = Double.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                                Double upperBound_Int = Double.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                                if (StringUtils.isNotEmpty(checkValue)) {
                                    Double value = Double.valueOf(checkValue);
                                    if (value < lowerBound_Int || value > upperBound_Int) {
                                        errorDataList.add(jsonObject);
                                    } else {
                                        successDataList.add(jsonObject);
                                    }
                                } else {
                                    errorDataList.add(jsonObject);
                                }
                            } else if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE) {
                                Double rangeCheckValue = Double.valueOf(dataCheckExtendPO.getRangeCheckValue());
                                String rangeCheckOneWayOperator = dataCheckExtendPO.getRangeCheckOneWayOperator();

                                if (StringUtils.isEmpty(checkValue)) {
                                    errorDataList.add(jsonObject);
                                } else {
                                    Double value = Double.valueOf(checkValue);
                                    boolean isValid = false;

                                    switch (rangeCheckOneWayOperator) {
                                        case ">":
                                            isValid = value > rangeCheckValue;
                                            break;
                                        case ">=":
                                            isValid = value >= rangeCheckValue;
                                            break;
                                        case "=":
                                            isValid = value.equals(rangeCheckValue); // Use equals for Double comparison
                                            break;
                                        case "!=":
                                            isValid = !value.equals(rangeCheckValue);
                                            break;
                                        case "<":
                                            isValid = value < rangeCheckValue;
                                            break;
                                        case "<=":
                                            isValid = value <= rangeCheckValue;
                                            break;
                                        default:
                                            log.info("值域检查-取值范围-单向取值-未匹配到有效的运算符：" + rangeCheckOneWayOperator);
                                            break;
                                    }
                                    if (isValid) {
                                        successDataList.add(jsonObject);
                                    } else {
                                        errorDataList.add(jsonObject);
                                    }
                                }
                            } else {
                                log.info("同步前-值域检查-取值范围-未匹配到有效的枚举：" + rangeCheckValueRangeTypeEnum.getName());
                            }
                        }
                        break;
                    case DATE_RANGE:
                        // 日期范围
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                                DateTimeFormatter.ofPattern("yyyy-M-dd"),
                                DateTimeFormatter.ofPattern("yyyy/M/dd"),
                                DateTimeFormatter.ofPattern("yyyy/MM/dd")
                        );
                        List<DateTimeFormatter> formatters1 = Arrays.asList(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                                DateTimeFormatter.ofPattern("yyyy-M-dd HH:mm:ss"),
                                DateTimeFormatter.ofPattern("yyyy/M/dd HH:mm:ss"),
                                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                        );
                        try {
                            LocalDateTime dateTime = null;
                            if (StringUtils.isNotEmpty(checkValue)) {
                                if (checkValue.length() > 10) {
                                    dateTime = DateTimeUtils.parseDateTime(checkValue, formatters1);
                                } else {
                                    LocalDate localDate = DateTimeUtils.parseDate(checkValue, formatters);
                                    if (localDate != null) {
                                        dateTime = DateTimeUtils.convertLocalDateToDateTime(localDate);
                                    }
                                }
                            }
                            if (dateTime != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                                String[] timeRange = timeRangeString.split("~");
                                LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                                LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);

                                if (dateTime.isBefore(startTime) || dateTime.isAfter(endTime)) {
                                    errorDataList.add(jsonObject);
                                } else {
                                    successDataList.add(jsonObject);
                                }
                            } else {
                                errorDataList.add(jsonObject);
                            }
                        } catch (DateTimeParseException e) {
                            errorDataList.add(jsonObject);
                        }
                        break;
                    case KEYWORDS_INCLUDE:
                        // 关键字包含
                        RangeCheckKeywordIncludeTypeEnum rangeCheckKeywordIncludeTypeEnum = RangeCheckKeywordIncludeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckKeywordIncludeType());
                        String rangeCheckValue = dataCheckExtendPO.getRangeCheckValue();

                        boolean isValid = false;
                        switch (rangeCheckKeywordIncludeTypeEnum) {
                            case CONTAINS_KEYWORDS:
                                isValid = checkValue.contains(rangeCheckValue);
                                break;
                            case INCLUDE_KEYWORDS_BEFORE:
                                isValid = checkValue.startsWith(rangeCheckValue);
                                break;
                            case INCLUDE_KEYWORDS_AFTER:
                                isValid = checkValue.endsWith(rangeCheckValue);
                                break;
                            default:
                                log.info("同步前-值域检查-关键字包含-未匹配到有效的枚举: " + rangeCheckKeywordIncludeTypeEnum.getName());
                                break;
                        }
                        if (isValid) {
                            successDataList.add(jsonObject);
                        } else {
                            errorDataList.add(jsonObject);
                        }
                        break;
                }
            } else {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
                return dataCheckResultVO;
            }
        }

        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.RANGE_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(successDataList);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            dataCheckResultVO.setCheckSuccessData(successDataList);
        }
        // 设置具体校验类型
        dataCheckResultVO.setCheckTemplateName(String.format("%s-%s", TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
        return dataCheckResultVO;
    }

    public List<String> resultSetToList(ResultSet rs) throws SQLException {
        List<String> list = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                list.add(value);
            }
        }
        return list;
    }

    public DataCheckResultVO interface_StandardCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                     DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        JSONArray errorDataList = new JSONArray();
        JSONArray successDataList = new JSONArray();
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {

                String checkValue = jsonObject.getString(fName);

                // 第三步：判断字段值是否通过规范检查
                switch (standardCheckTypeEnum) {
                    case DATE_FORMAT:
                        // 日期格式
                        List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                        boolean validDateFormat = false;
//                        if (checkValue.length() > 10) {
//                            validDateFormat = DateTimeUtils.isValidDateTimeFormat(checkValue, list);
//                        } else {
//                            validDateFormat = DateTimeUtils.isValidDateFormat(checkValue, list);
//                        }
                        validDateFormat = DateTimeUtils.isValidDateOrTimeFormat(checkValue, list);
                        if (!validDateFormat) {
                            errorDataList.add(jsonObject);
                        } else {
                            successDataList.add(jsonObject);
                        }
                        break;
                    case CHARACTER_PRECISION_LENGTH_RANGE:
                        // 字符范围
                        StandardCheckCharRangeTypeEnum standardCheckCharRangeTypeEnum = StandardCheckCharRangeTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckCharRangeType());
                        if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_PRECISION_RANGE) {
                            int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                            int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                            if (StringUtils.isNotEmpty(checkValue)) {
                                String regex = Pattern.quote(dataCheckExtendPO.getStandardCheckTypeLengthSeparator());
                                List<String> values = Arrays.asList(checkValue.split(regex));
                                if (values.stream().count() >= 2) {
                                    String value = values.get(Math.toIntExact(values.stream().count() - 1));
                                    if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                        errorDataList.add(jsonObject);
                                    } else {
                                        successDataList.add(jsonObject);
                                    }
                                }
                            } else {
                                errorDataList.add(jsonObject);
                            }
                        } else if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_LENGTH_RANGE) {
                            String standardCheckTypeLengthOperator = dataCheckExtendPO.getStandardCheckTypeLengthOperator();
                            int standardCheckTypeLengthValue = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue());
                            if (StringUtils.isEmpty(checkValue)) {
                                errorDataList.add(jsonObject);
                            } else {
                                int checkValueLength = checkValue.length();
                                boolean isValid = false;
                                switch (standardCheckTypeLengthOperator) {
                                    case ">":
                                        isValid = checkValueLength > standardCheckTypeLengthValue;
                                        break;
                                    case ">=":
                                        isValid = checkValueLength >= standardCheckTypeLengthValue;
                                        break;
                                    case "<":
                                        isValid = checkValueLength < standardCheckTypeLengthValue;
                                        break;
                                    case "<=":
                                        isValid = checkValueLength <= standardCheckTypeLengthValue;
                                        break;
                                    case "=":
                                        isValid = checkValueLength == standardCheckTypeLengthValue;
                                        break;
                                    case "!=":
                                        isValid = checkValueLength != standardCheckTypeLengthValue;
                                        break;
                                    default:
                                        log.info("同步前-规范检查-字符范围-字符长度范围-未匹配到有效的运算符：" + standardCheckTypeLengthOperator);
                                        break;
                                }

                                if (isValid) {
                                    successDataList.add(jsonObject);
                                } else {
                                    errorDataList.add(jsonObject);
                                }
                            }
                        } else {
                            log.info("同步前-规范检查-字符范围-未匹配到有效的枚举：" + standardCheckCharRangeTypeEnum.getName());
                        }
                        break;
                    case URL_ADDRESS:
                        // URL地址
                        String standardCheckTypeRegexpValue = dataCheckExtendPO.getStandardCheckTypeRegexpValue();
                        boolean validURL = RegexUtils.isValidPattern(checkValue, standardCheckTypeRegexpValue, false);
                        if (!validURL) {
                            errorDataList.add(jsonObject);
                        } else {
                            successDataList.add(jsonObject);
                        }
                        break;
                    case BASE64_BYTE_STREAM:
                        // BASE64字节流
                        boolean validBase64String = RegexUtils.isBase64String(checkValue, false);
                        if (!validBase64String) {
                            errorDataList.add(jsonObject);
                        } else {
                            successDataList.add(jsonObject);
                        }
                        break;
                }
            } else {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
                return dataCheckResultVO;
            }
        }

        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.STANDARD_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(successDataList);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            dataCheckResultVO.setCheckSuccessData(successDataList);
        }
        // 设置具体校验类型
        dataCheckResultVO.setCheckTemplateName(String.format("%s-%s", TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_DuplicateDateCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                          DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

        // 第二步：判断检查的字段是否存在，存在则获取字段值并判断是否重复
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        List<String> fieldNames = Arrays.asList(dataCheckExtendPO.getFieldName().split(","));
        List<String> fieldValues = new ArrayList<>();
        JSONArray errorDataList = new JSONArray();
        JSONArray successDataList = new JSONArray();
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
                errorDataList.add(jsonObject);
            } else {
                successDataList.add(jsonObject);
            }
            fieldValues.add(value.toLowerCase());
        }

        // 第三步：判断数据是否通过重复数据检查
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, dataCheckExtendPO.getFieldName(), dataCheckPO.ruleName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据", tName, dataCheckExtendPO.getFieldName(), dataCheckPO.ruleName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, dataCheckExtendPO.getFieldName(), dataCheckPO.ruleName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(successDataList);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s通过", tName, dataCheckExtendPO.getFieldName(), dataCheckPO.ruleName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            dataCheckResultVO.setCheckSuccessData(successDataList);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_FluctuationCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                        DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

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
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue() || dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
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
            dataCheckResultVO.setCheckFailCount(String.valueOf(data.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s-%s检查通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
            dataCheckResultVO.setCheckSuccessData(data);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_ParentageCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                      DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

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
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue() || dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
                dataCheckResultVO.setCheckSuccessData(data);
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查未通过，但检查规则未设置强规则将继续放行数据", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
                dataCheckResultVO.setCheckSuccessData(data);
            }
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，【%s】-【%s】检查通过", tableName, TemplateTypeEnum.PARENTAGE_CHECK.getName(), parentageCheckTypeEnum));
            dataCheckResultVO.setCheckSuccessData(data);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_RegexCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                  DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

        // 第二步：判断检查的字段是否存在，存在则获取字段值
        String tName = dataCheckSyncParamDTO.getTableName();
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat();
        String fName = dataCheckSyncParamDTO.getFieldName();
        String f_Name = dataCheckSyncParamDTO.getFieldNameFormat();
        JSONArray errorDataList = new JSONArray();
        JSONArray successDataList = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fName)) {
                String checkValue = jsonObject.getString(fName);
                boolean isValid = RegexUtils.isValidPattern(checkValue, dataCheckExtendPO.getRegexpCheckValue(), false);
                if (!isValid) {
                    errorDataList.add(jsonObject);
                } else {
                    successDataList.add(jsonObject);
                }
            } else {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg("待校验的JSON数据格式异常，未包含指定字段key【" + fName + "】");
                return dataCheckResultVO;
            }
        }

        // 第三步：判断字段值是否通过正则表达式验证
        if (CollectionUtils.isNotEmpty(errorDataList)) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过，正则表达式为：%s", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.REGEX_CHECK.getName(), dataCheckExtendPO.getRegexpCheckValue()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据，正则表达式为：%s", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.REGEX_CHECK.getName(), dataCheckExtendPO.getRegexpCheckValue()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s未通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.REGEX_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(successDataList);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(errorDataList));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(errorDataList.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，规则代号：【%s】，%s通过", tName, fName, dataCheckPO.ruleName, TemplateTypeEnum.REGEX_CHECK.getName()));
            dataCheckResultVO.setCheckSuccessData(data);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_SqlScriptCheck(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                      DataCheckExtendPO dataCheckExtendPO, JSONArray data, DataCheckSyncParamDTO dataCheckSyncParamDTO) {
        // 第一步：获取检查结果基础信息
        DataCheckResultVO dataCheckResultVO = interface_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, data);

        // 第二步：建立数据库连接执行SQL查询语句
        Connection conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
        JSONArray jsonArray = AbstractCommonDbHelper.execQueryResultArrays(dataCheckExtendPO.getSqlCheckValue(), conn);
        // 执行Sql脚本，查询结果是否为空，不为空则校验不通过，为空则校验通过
        boolean isValid = false;
        if (CollectionUtils.isEmpty(jsonArray)) {
            isValid = true;
        }

        String tableName = dataCheckPO.getSchemaName() + "." + dataCheckPO.getTableName();
        if (!isValid) {
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue() || dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s未通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            } else {
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
                dataCheckResultVO.setCheckSuccessData(data);
            }
            if (dataCheckExtendPO.getRecordErrorData() == 1) {
                dataCheckResultVO.setCheckErrorData(JSONArray.toJSONString(jsonArray));
            }
            dataCheckResultVO.setCheckFailCount(String.valueOf(data.size()));
        } else {
            dataCheckResultVO.setCheckResult(SUCCESS);
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s通过", tableName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            dataCheckResultVO.setCheckSuccessData(data);
        }
        return dataCheckResultVO;
    }

    public DataCheckResultVO interface_GetCheckResultBasisInfo(TemplatePO templatePO, DataSourceConVO dataSourceConVO, DataCheckPO dataCheckPO,
                                                               DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
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
        dataCheckResultVO.setCheckTotalCount(String.valueOf(data.size()));
        String tableName = dataCheckPO.getTableName().substring(dataCheckPO.getTableName().lastIndexOf("_") + 1);
        dataCheckResultVO.setCheckDisplayTableName(tableName);
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
            dataCheckSyncParamDTO.setUniqueIdNameUnFormat(dto.uniqueField);

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
                String tblName = dataCheckPO.tableName;
                //判断表名是否包含架构名，包含架构名就去掉架构名
                if (tblName.contains(".")) {
                    tblName = dto.tablePrefix + tblName.split("\\.")[1];
                }
                //判断表名是否包含ods_,如果包含就将ods_替换为stg_,因为我们操作的是临时表
                if (tblName.contains("ods_")) {
                    tblName = tblName.replace("ods_", "stg_");
                } else if (!tblName.contains("ods_")
                        && !tblName.contains("stg_")
                        && StringUtils.isNotEmpty(dto.getTablePrefix())) {
                    //判断表名，如果不包含ods_和stg_,并且参数中tablePrefix不为空则表名拼接此前缀部分。同步中执行校验的一定是临时表。出现表名不带ods_前缀是因为在数接应用配置的时候勾选了使用简称作为架构
                    tblName = dto.tablePrefix + tblName;
                }
                if (StringUtils.isNotEmpty(dataCheckPO.getSchemaName())) {
                    tableNameFormat = nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), dataCheckPO.getSchemaName()) + ".";
                    tableName = dataCheckPO.getSchemaName() + ".";
                }
                tableNameFormat += nifiSync_GetSqlFieldFormat(dataSourceConVO.getConType(), tblName);
                tableName += dataCheckPO.tableName;

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
                try {
                    switch (templateType) {
                        //空值检查
                        case NULL_CHECK:
                            dataCheckResult = nifiSync_NullCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                            break;
                        //值域检查
                        case RANGE_CHECK:
                            dataCheckResult = nifiSync_RangeCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                            break;
                        //规范检查
                        case STANDARD_CHECK:
                            dataCheckResult = nifiSync_StandardCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                            break;
                        //重复数据检查
                        case DUPLICATE_DATA_CHECK:
                            dataCheckResult = nifiSync_DuplicateDateCheck(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO, dataCheckSyncParamDTO);
                            break;
                        //波动检查
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
                } catch (Exception ruleEx) {
                    if (dataCheckResult == null) {
                        dataCheckResult = nifiSync_GetCheckResultBasisInfo(templatePO, dataSourceConVO, dataCheckPO, dataCheckExtendPO);
                    }
                    dataCheckResult.setCheckResult(FAIL);
                    dataCheckResult.setCheckResultMsg(String.format("代号：【%s】，触发系统异常，%s未通过", dataCheckPO.getRuleName(), templateType.getName()));
                    log.error("【nifiSyncCheckData】执行检查规则时触发系统异常：" + ruleEx);
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
                        dataCheckLogsPO.setRuleName(dataCheckPO.getRuleName());
                        dataCheckLogsPO.setTemplateId(Math.toIntExact(templatePO.getId()));
                        dataCheckLogsPO.setFiDatasourceId(dataSourceConVO.getDatasourceId());
                        dataCheckLogsPO.setLogType(DataCheckLogTypeEnum.NIFI_SYNCHRONIZATION_DATA_CHECK_LOG.getValue());
                        dataCheckLogsPO.setSchemaName(dataCheckResult.getCheckSchema());
                        dataCheckLogsPO.setTableName(dataCheckResult.getCheckTable());
                        dataCheckLogsPO.setFieldName(dataCheckResult.getCheckField());
                        dataCheckLogsPO.setCheckTemplateName(dataCheckResult.getCheckTemplateName());
                        dataCheckLogsPO.setCheckBatchNumber(dataCheckSyncParamDTO.getBatchNumber());
                        dataCheckLogsPO.setCheckSmallBatchNumber(dataCheckSyncParamDTO.getSmallBatchNumber());
                        dataCheckLogsPO.setCheckTotalCount(dataCheckResult.getCheckTotalCount());
                        dataCheckLogsPO.setCheckFailCount(dataCheckResult.getCheckFailCount());
                        dataCheckLogsPO.setCheckResult(dataCheckResult.getCheckResult().toString());
                        dataCheckLogsPO.setCheckMsg(dataCheckResult.getCheckResultMsg());
                        dataCheckLogsPO.setCheckRuleIllustrate(dataCheckPO.getRuleIllustrate());
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
                                ? "\"%s\"" + "=" + "'%s'" :
                                dataSourceTypeEnum == DataSourceTypeEnum.DORIS
                                        ? "`%s`" + "=" + "'%s'" : "";
        return sqlWhereStr;
    }

    /**
     * @return java.lang.String
     * @description 拼语法关键字
     * @author dick
     * @date 2024/6/19 11:04
     * @version v1.0
     * @params dataSourceTypeEnum
     * @params fieldName 可以是字段名称、表名称、架构名称
     */
    public String nifiSync_GetSqlFieldFormat(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL
                                ? "\"%s\"" :
                                dataSourceTypeEnum == DataSourceTypeEnum.DORIS
                                        ? "" : "";
        if (StringUtils.isNotEmpty(sqlFieldStr)) {
            sqlFieldStr = String.format(sqlFieldStr, fieldName);
        } else {
            sqlFieldStr = fieldName;
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
                    "END", msgField, msgField, "'" + msg + "'", msgField + "+" + "'；" + msg + "'");
            updateMsgFieldSql = String.format("%s" + "=" + "%s ", msgField, caseSql);

        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
            String caseSql = String.format("CASE \n" +
                    "\tWHEN %s='' or %s is null THEN\n" +
                    "\t\t%s\n" +
                    "\tELSE\n" +
                    "\t\t%s\n" +
                    "END", msgField, msgField, "'" + msg + "'", msgField + " || " + "'；" + msg + "'");
            updateMsgFieldSql = String.format("%s" + "=" + "%s ", msgField, caseSql);
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
                fType = dataCheckExtendPO.getFieldType(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = String.format("SELECT %s,%s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '' OR %s = 'null')", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, f_Name, f_Name);
        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
            sql_QueryCheckData = String.format("SELECT %s,%s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '' OR %s = 'null')", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, f_Name, f_Name);
        }

        String sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND (%s IS NULL OR %s = '' OR %s = 'null')", f_uniqueIdName, t_Name, f_where, f_Name, f_Name, f_Name);

        // 如果判断的字段非字符串类型，则只能判断是否为NULL
        boolean charValid = RegexUtils.isCharValid(fType);
        if (!charValid) {
            sql_QueryCheckData = String.format("SELECT %s,%s FROM %s WHERE 1=1 %s AND %s IS NULL", f_uniqueIdName, f_Name, t_Name, f_where, f_Name);
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                sql_QueryCheckData = String.format("SELECT %s,%s FROM %s WHERE 1=1 %s AND %s IS NULL", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name);
            }
            sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s IS NULL", f_uniqueIdName, t_Name, f_where, f_Name);
        }

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
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过，，但检查规则未设置强规则将继续放行数据", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.NULL_CHECK.getName()));
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
        Integer datacheckGroupId = dataCheckPO.datacheckGroupId;
        DatacheckStandardsGroupPO groupPO = datacheckStandardsGroupService.getById(datacheckGroupId);
        StandardsDTO standardsDTO = new StandardsDTO();
        if (datacheckGroupId != null){
            ResultEntity<StandardsDTO> standards = dataManageClient.getStandards(groupPO.getStandardsMenuId());
            if (standards.code == ResultEnum.SUCCESS.getCode()) {
                standardsDTO = standards.data;
            } else {
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        if (standardsDTO == null){
            log.info("数据元未查询到匹配数据请检查并清理脏数据,数据元id"+datacheckGroupId);
        }
        // 第二步：组装并执行SQL语句，获取校验结果
        String t_Name = dataCheckSyncParamDTO.getTableNameFormat(),
                f_Name = dataCheckSyncParamDTO.getFieldNameFormat(),
                fName = dataCheckSyncParamDTO.getFieldName(),
                f_where = dataCheckSyncParamDTO.getWhereFieldSql(),
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = "", sql_UpdateErrorData = "";

        DataSourceTypeEnum dataSourceTypeEnum = dataSourceConVO.getConType();
        RangeCheckTypeEnum rangeCheckTypeEnum = null;
        ValueRangeTypeEnum valueRangeType = standardsDTO.getValueRangeType();
        if (datacheckGroupId != null){
            rangeCheckTypeEnum = RangeCheckTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckType());
        }else {
            switch (valueRangeType){
                case DATASET:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.SEQUENCE_RANGE;
                    break;
                case VALUE:
                case VALUE_RANGE:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.VALUE_RANGE;
                    break;
                case NONE:
                    rangeCheckTypeEnum = RangeCheckTypeEnum.NONE;
                    break;
            }
        }
        switch (rangeCheckTypeEnum) {
            case SEQUENCE_RANGE:
                if (datacheckGroupId != null){
                    // 序列范围
                    List<CodeSetDTO> codeSetDTOList = standardsDTO.getCodeSetDTOList();
                    List<String> list = codeSetDTOList.stream().map(v -> v.getName()).collect(Collectors.toList());
                    // 将list里面的序列范围截取为'','',''格式的字符串
                    String sql_InString = list.stream()
                            .map(item -> "N'" + item + "'")
                            .collect(Collectors.joining(", "));

                    sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, sql_InString);
                    if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, sql_InString);
                    }
                    sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, t_Name, f_where, f_Name, sql_InString);
                }else {
                    if (dataCheckExtendPO.rangeType == 2) {
                        String childrenQuery = String.format("SELECT %s FROM %s", dataCheckExtendPO.getCheckFieldName(), dataCheckExtendPO.getCheckTableName());

                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, childrenQuery);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, childrenQuery);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, t_Name, f_where, f_Name, childrenQuery);
                    } else {
                        // 序列范围
                        List<String> list = Arrays.asList(dataCheckExtendPO.getRangeCheckValue().split(","));
                        // 将list里面的序列范围截取为'','',''格式的字符串
                        String sql_InString = list.stream()
                                .map(item -> "N'" + item + "'")
                                .collect(Collectors.joining(", "));

                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, sql_InString);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, sql_InString);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s NOT IN (%s)", f_uniqueIdName, t_Name, f_where, f_Name, sql_InString);
                    }
                }
                break;
            case VALUE_RANGE:
                if (datacheckGroupId != null){
                    if (valueRangeType == ValueRangeTypeEnum.VALUE){
                        Double rangeCheckValue = Double.valueOf(standardsDTO.getValueRange());
                        String rangeCheckOneWayOperator = standardsDTO.getSymbols();
                        String sql_BetweenAnd = String.format("CAST(%s AS INT) %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                            sql_BetweenAnd = String.format("%s::NUMERIC %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                            sql_BetweenAnd = String.format("%s %s '%s'", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        }
                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where, sql_BetweenAnd);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where_1, sql_BetweenAnd);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, t_Name, f_where, sql_BetweenAnd);

                    }else if (valueRangeType == ValueRangeTypeEnum.VALUE_RANGE){
                        // 取值范围-区间取值
                        Double lowerBound_Int = Double.valueOf(standardsDTO.getValueRange());
                        Double upperBound_Int = Double.valueOf(standardsDTO.getValueRangeMax());
                        String sql_BetweenAnd = String.format("CAST(%s AS INT) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                            sql_BetweenAnd = String.format("%s::NUMERIC NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                        } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                            sql_BetweenAnd = String.format("%s NOT BETWEEN '%s' AND '%s'", f_Name, lowerBound_Int, upperBound_Int);
                        }
                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where, sql_BetweenAnd);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where_1, sql_BetweenAnd);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, t_Name, f_where, sql_BetweenAnd);

                    }
                }else {
                    // 取值范围
                    RangeCheckValueRangeTypeEnum rangeCheckValueRangeTypeEnum = RangeCheckValueRangeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckValueRangeType());
                    if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.INTERVAL_VALUE) {
                        Integer lowerBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[0]);
                        Integer upperBound_Int = Integer.valueOf(dataCheckExtendPO.getRangeCheckValue().split("~")[1]);
                        String sql_BetweenAnd = String.format("CAST(%s AS INT) NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                            sql_BetweenAnd = String.format("%s::NUMERIC NOT BETWEEN %s AND %s", f_Name, lowerBound_Int, upperBound_Int);
                        } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                            sql_BetweenAnd = String.format("%s NOT BETWEEN '%s' AND '%s'", f_Name, lowerBound_Int, upperBound_Int);
                        }
                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where, sql_BetweenAnd);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where_1, sql_BetweenAnd);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, t_Name, f_where, sql_BetweenAnd);
                    } else if (rangeCheckValueRangeTypeEnum == RangeCheckValueRangeTypeEnum.UNIDIRECTIONAL_VALUE) {
                        Double rangeCheckValue = Double.valueOf(dataCheckExtendPO.getRangeCheckValue());
                        String rangeCheckOneWayOperator = dataCheckExtendPO.getRangeCheckOneWayOperator();
                        String sql_BetweenAnd = String.format("CAST(%s AS INT) %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                            sql_BetweenAnd = String.format("%s::NUMERIC %s %s", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        } else if (dataSourceTypeEnum == DataSourceTypeEnum.DORIS) {
                            sql_BetweenAnd = String.format("%s %s '%s'", f_Name, rangeCheckOneWayOperator, rangeCheckValue);
                        }
                        sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where, sql_BetweenAnd);
                        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, f_Name, t_Name, f_where_1, sql_BetweenAnd);
                        }
                        sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s", f_uniqueIdName, t_Name, f_where, sql_BetweenAnd);
                    } else {
                        log.info("同步中-值域检查-取值范围-未匹配到有效的枚举：" + rangeCheckValueRangeTypeEnum.getName());
                    }
                }
                break;
            case DATE_RANGE:
                // 日期范围
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String timeRangeString = dataCheckExtendPO.getRangeCheckValue();
                String[] timeRange = timeRangeString.split("~");
                LocalDateTime startTime = LocalDateTime.parse(timeRange[0], formatter);
                LocalDateTime endTime = LocalDateTime.parse(timeRange[1], formatter);
                sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))",
                        f_uniqueIdName, f_Name, t_Name, f_where, f_Name, f_Name, f_Name, startTime, endTime);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))",
                            f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, f_Name, f_Name, startTime, endTime);
                }
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND ((%s IS NULL OR %s = '') OR (%s NOT BETWEEN '%s' AND '%s'))",
                        f_uniqueIdName, t_Name, f_where, f_Name, f_Name, f_Name, startTime, endTime);
                break;
            case KEYWORDS_INCLUDE:
                // 关键字包含
                RangeCheckKeywordIncludeTypeEnum rangeCheckKeywordIncludeTypeEnum = RangeCheckKeywordIncludeTypeEnum.getEnum(dataCheckExtendPO.getRangeCheckKeywordIncludeType());
                String rangeCheckValue = dataCheckExtendPO.getRangeCheckValue();
                String likeValue = "";
                switch (rangeCheckKeywordIncludeTypeEnum) {
                    case CONTAINS_KEYWORDS:
                        likeValue = "'%" + rangeCheckValue + "%'";
                        break;
                    case INCLUDE_KEYWORDS_BEFORE:
                        likeValue = "'" + rangeCheckValue + "%'";
                        break;
                    case INCLUDE_KEYWORDS_AFTER:
                        likeValue = "'%" + rangeCheckValue + "'";
                        break;
                    default:
                        log.info("同步中-值域检查-关键字包含-未匹配到有效的枚举: " + rangeCheckKeywordIncludeTypeEnum.getName());
                        break;
                }
                sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s not like %s", f_uniqueIdName, f_Name, t_Name, f_where, f_Name, likeValue);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s AND %s not like %s", f_uniqueIdName, f_Name, t_Name, f_where_1, f_Name, likeValue);
                }
                sql_UpdateErrorData = String.format("SELECT %s FROM %s WHERE 1=1 %s AND %s not like %s", f_uniqueIdName, t_Name, f_where, f_Name, likeValue);
                break;
        }

        // 查询校验数据的总数
        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            String checkTotalCount = null;
            for (Map.Entry<String, Object> entry : entries) {
                checkTotalCount = entry.getValue().toString();
            }
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }

        // 查询并获取不通过校验的记录
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
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));

            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_UpdateErrorData);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查通过", dataCheckSyncParamDTO.getTableName(), dataCheckSyncParamDTO.getFieldName(), TemplateTypeEnum.RANGE_CHECK.getName(), rangeCheckTypeEnum.getName()));
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
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                f_uniqueIdNameUnFormat = dataCheckSyncParamDTO.getUniqueIdNameUnFormat(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryTotalCount = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 %s", t_Name, f_where);
        String sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where);
        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where_1);
        }

        // 第三步：查询待校验的数据
        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            String checkTotalCount = null;
            for (Map.Entry<String, Object> entry : entries) {
                checkTotalCount = entry.getValue().toString();
            }
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        JSONArray errorDataList = new JSONArray();
        StandardCheckTypeEnum standardCheckTypeEnum = StandardCheckTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckType());
        JSONArray data = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (data != null && data.size() != 0) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                String fieldKey = jsonObject.getString(f_uniqueIdNameUnFormat);
                Object fieldValue = jsonObject.get(fName);

                // 第四步：检查数据是否符合规范
                switch (standardCheckTypeEnum) {
                    case DATE_FORMAT:
                        // 日期格式
                        List<String> list = Arrays.asList(dataCheckExtendPO.getStandardCheckTypeDateValue().split(","));
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            boolean validDateFormat = false;
//                            if (fieldValue.toString().length() > 10) {
//                                validDateFormat = DateTimeUtils.isValidDateTimeFormat(fieldValue.toString(), list);
//                            } else {
//                                validDateFormat = DateTimeUtils.isValidDateFormat(fieldValue.toString(), list);
//                            }
                            validDateFormat = DateTimeUtils.isValidDateOrTimeFormat(fieldValue.toString(), list);
                            if (!validDateFormat) {
                                errorDataList.add(jsonObject);
                            }
                        }
                        break;
                    case CHARACTER_PRECISION_LENGTH_RANGE:
                        // 字符范围
                        StandardCheckCharRangeTypeEnum standardCheckCharRangeTypeEnum = StandardCheckCharRangeTypeEnum.getEnum(dataCheckExtendPO.getStandardCheckCharRangeType());
                        if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_PRECISION_RANGE) {
                            // 字符精度长度范围
                            int minFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[0]);
                            int maxFieldLength = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue().split("~")[1]);
                            if (fieldValue == null || fieldValue.toString().equals("")) {
                                errorDataList.add(jsonObject);
                            } else {
                                String regex = Pattern.quote(dataCheckExtendPO.getStandardCheckTypeLengthSeparator());
                                List<String> values = Arrays.asList(fieldValue.toString().split(regex));
                                if (values.stream().count() >= 2) {
                                    String value = values.get(Math.toIntExact(values.stream().count() - 1));
                                    if (value.length() < minFieldLength || value.length() > maxFieldLength) {
                                        errorDataList.add(jsonObject);
                                    }
                                }
                            }
                        } else if (standardCheckCharRangeTypeEnum == StandardCheckCharRangeTypeEnum.CHARACTER_LENGTH_RANGE) {
                            String standardCheckTypeLengthOperator = dataCheckExtendPO.getStandardCheckTypeLengthOperator();
                            int standardCheckTypeLengthValue = Integer.parseInt(dataCheckExtendPO.getStandardCheckTypeLengthValue());
                            int checkValueLength = fieldValue == null || fieldValue.toString().equals("") ? 0 : fieldValue.toString().length();

                            boolean isValid = false;
                            switch (standardCheckTypeLengthOperator) {
                                case ">":
                                    isValid = checkValueLength > standardCheckTypeLengthValue;
                                    break;
                                case ">=":
                                    isValid = checkValueLength >= standardCheckTypeLengthValue;
                                    break;
                                case "<":
                                    isValid = checkValueLength < standardCheckTypeLengthValue;
                                    break;
                                case "<=":
                                    isValid = checkValueLength <= standardCheckTypeLengthValue;
                                    break;
                                case "=":
                                    isValid = checkValueLength == standardCheckTypeLengthValue;
                                    break;
                                case "!=":
                                    isValid = checkValueLength != standardCheckTypeLengthValue;
                                    break;
                                default:
                                    log.info("同步中-规范检查-字符范围-字符长度范围-未匹配到有效的运算符：" + standardCheckTypeLengthOperator);
                                    break;
                            }

                            if (!isValid) {
                                errorDataList.add(jsonObject);
                            }
                        } else {
                            log.info("同步中-规范检查-字符范围-未匹配到有效的枚举：" + standardCheckCharRangeTypeEnum.getName());
                        }
                        break;
                    case URL_ADDRESS:
                        // URL地址
                        String standardCheckTypeRegexpValue = dataCheckExtendPO.getStandardCheckTypeRegexpValue();
                        if (fieldValue == null || fieldValue.toString().equals("")) {
                            errorDataList.add(jsonObject);
                        } else {
                            boolean validURL = RegexUtils.isValidPattern(fieldValue.toString(), standardCheckTypeRegexpValue, false);
                            if (!validURL) {
                                errorDataList.add(jsonObject);
                            }
                        }
                        break;
                    case BASE64_BYTE_STREAM:
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
//                String checkTotalCount = String.valueOf(data.size());
//                dataCheckResultVO.setCheckTotalCount(checkTotalCount);
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
                String uniqueId = (String) jsonObject.get(f_uniqueIdNameUnFormat);
                uniqueIdList.add(uniqueId);
            }
            String sql_InString = uniqueIdList.stream()
                    .map(item -> "'" + item + "'")
                    .collect(Collectors.joining(", "));
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查通过", tName, fName, TemplateTypeEnum.STANDARD_CHECK.getName(), standardCheckTypeEnum.getName()));
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
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
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
        String sql_QueryCheckData = String.format("SELECT %s COUNT(*) AS repetitionCount FROM %s WHERE 1=1 %s \n" +
                "GROUP BY %s HAVING COUNT(*) > 1;", f_Name, t_Name, f_where, f_Name.replaceAll(",+$", ""));

        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
            sql_QueryCheckData = String.format("SELECT %s COUNT(*) AS repetitionCount FROM %s WHERE 1=1 %s \n" +
                    "GROUP BY %s HAVING COUNT(*) > 1;", f_Name, t_Name, f_where_1, f_Name.replaceAll(",+$", ""));
        }
        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            String checkTotalCount = null;
            for (Map.Entry<String, Object> entry : entries) {
                checkTotalCount = entry.getValue().toString();
            }
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        JSONArray jsonArray = nifiSync_QueryTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            dataCheckResultVO.setCheckFailCount(String.valueOf(maps.size()));
        }

        // 第三步：判断是否通过重复数据检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (CollectionUtils.isNotEmpty(jsonArray)) {
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
                        "WHERE 1=1 %s;", t_Name, sql_N + updateMsgFieldSql, f_Name.replaceAll(",+$", ""), t_Name, f_where, f_Name.replaceAll(",+$", ""), updateFieldWhereStr);
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s\n" +
                        "SET %s\n" +
                        "FROM (\n" +
                        " SELECT %s FROM %s WHERE 1=1 %s\n" +
                        " GROUP BY %s HAVING COUNT(*) > 1\n" +
                        ") sy\n" +
                        "WHERE 1=1 %s;", t_Name, sql_N + updateMsgFieldSql, f_Name.replaceAll(",+$", ""), t_Name, f_where, f_Name.replaceAll(",+$", ""), updateFieldWhereStr);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
            } else {
                updateSql = String.format("UPDATE %s\n" +
                        "SET %s\n" +
                        "FROM (\n" +
                        " SELECT %s FROM %s WHERE 1=1 %s\n" +
                        " GROUP BY %s HAVING COUNT(*) > 1\n" +
                        ") sy\n" +
                        "WHERE 1=1 %s;", t_Name, sql_W + updateMsgFieldSql, f_Name.replaceAll(",+$", ""), t_Name, f_where, f_Name.replaceAll(",+$", ""), updateFieldWhereStr);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s通过", tName, fName, TemplateTypeEnum.DUPLICATE_DATA_CHECK.getName()));
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
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
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
                sql_QueryCheckData = String.format("SELECT AVG(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT AVG(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where_1);
                }
                break;
            case MIN:
                sql_QueryCheckData = String.format("SELECT MIN(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT MIN(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where_1);
                }
                break;
            case MAX:
                sql_QueryCheckData = String.format("SELECT MAX(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT MAX(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where_1);
                }
                break;
            case SUM:
                sql_QueryCheckData = String.format("SELECT SUM(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT SUM(CAST(%s as int)) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where_1);
                }
                break;
            case COUNT:
                sql_QueryCheckData = String.format("SELECT COUNT(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where);
                if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                    sql_QueryCheckData = String.format("SELECT COUNT(%s) AS realityValue FROM %s WHERE 1=1 %s", f_Name, t_Name, f_where_1);
                }
                break;
        }

        List<Map<String, Object>> maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryTotalCount);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            String checkTotalCount = null;
            for (Map.Entry<String, Object> entry : entries) {
                checkTotalCount = entry.getValue().toString();
            }
            dataCheckResultVO.setCheckTotalCount(checkTotalCount);
        }
        maps = nifiSync_CheckTableData(dataSourceConVO, sql_QueryCheckData);
        if (CollectionUtils.isNotEmpty(maps)) {
            Set<Map.Entry<String, Object>> entries = maps.get(0).entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                realityValue = Double.parseDouble(entry.getValue().toString());
            }
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
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_W, f_where);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查未通过，但检查规则未设置强规则将继续放行数据", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s-%s检查通过", tName, fName, TemplateTypeEnum.FLUCTUATION_CHECK.getName(), fluctuateCheckTypeEnum.getName()));
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
                f_where_1 = f_where.substring(f_where.lastIndexOf("AND")),
                f_uniqueIdName = dataCheckSyncParamDTO.getUniqueField(),
                f_uniqueIdNameUnFormat = dataCheckSyncParamDTO.getUniqueIdNameUnFormat(),
                sql_Y = dataCheckSyncParamDTO.getSuccessFieldSql(),
                sql_N = dataCheckSyncParamDTO.getFailFieldSql(),
                sql_W = dataCheckSyncParamDTO.getWarnFieldSql();
        String sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where);
        if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
            sql_QueryCheckData = String.format("SELECT %s, %s FROM %s WHERE 1=1 %s", f_uniqueIdName, f_Name, t_Name, f_where_1);
        }
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
                String uniqueId = (String) jsonObject.get(f_uniqueIdNameUnFormat);
                uniqueIdList.add(uniqueId);
            }
            String sql_InString = uniqueIdList.stream()
                    .map(item -> "'" + item + "'")
                    .collect(Collectors.joining(", "));
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                updateSql += String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N, f_where_1);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过，正则表达式为：%s", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName(), dataCheckExtendPO.getRegexpCheckValue()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_N + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过，正则表达式为：%s", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName(), dataCheckExtendPO.getRegexpCheckValue()));
            } else {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s AND %s IN (%s);", t_Name, sql_W + updateMsgFieldSql, f_where, f_uniqueIdName, sql_InString);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据，正则表达式为：%s", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName(), dataCheckExtendPO.getRegexpCheckValue()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s通过", tName, fName, TemplateTypeEnum.REGEX_CHECK.getName()));
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

        // 执行Sql脚本，查询结果是否为空，不为空则校验不通过，为空则校验通过
        boolean isValid = false;
        if (CollectionUtils.isEmpty(jsonArray)) {
            isValid = true;
        }

        // 第三步：判断是否通过SQL检查，通过或者未通过都需要更新表状态
        String updateSql = "", updateMsgFieldSql = "";
        if (!isValid) {
            // 组装修改语句
            if (StringUtils.isNotEmpty(dataCheckSyncParamDTO.getMsgField())) {
                updateMsgFieldSql = "," + nifiSync_getUpdateMsgFieldSql(dataSourceConVO.getConType(), dataCheckSyncParamDTO.getMsgField(), "正则表达式检查未通过");
            }
            if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRONG_RULE.getValue() || dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.STRICT_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_N + updateMsgFieldSql, f_where);
                dataCheckResultVO.setCheckResult(FAIL);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s未通过", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
            } else if (dataCheckPO.getRuleCheckType() == RuleCheckTypeEnum.WEAK_RULE.getValue()) {
                updateSql = String.format("UPDATE %s SET %s WHERE 1=1 %s;", t_Name, sql_W + updateMsgFieldSql, f_where);
                dataCheckResultVO.setCheckResult(WARN);
                dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s未通过，但检查规则未设置强规则将继续放行数据", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
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
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，%s通过", tName, TemplateTypeEnum.SQL_SCRIPT_CHECK.getName()));
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

    @Override
    public Page<DataCheckLogsVO> getDataCheckLogsPage(DataCheckLogsQueryDTO dto) {
        Page<DataCheckLogsVO> all = dataCheckLogsMapper.getAll(dto.page, dto);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            // 规则校验日志ID
            List<String> idUuidList = all.getRecords().stream().map(DataCheckLogsVO::getIdUuid).collect(Collectors.toList());

            // 查询校验表所属的数据库
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            // 查询校验规则的附件信息
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getCategory, AttachmentCateGoryEnum.QUALITY_VERIFICATION_RULES_VERIFICATION_DETAIL_REPORT.getValue())
                    .in(AttachmentInfoPO::getObjectId, idUuidList);
            List<AttachmentInfoPO> attachmentInfoPOList = attachmentInfoMapper.selectList(attachmentInfoPOQueryWrapper);

            for (DataCheckLogsVO dataCheckLogsVO : all.getRecords()) {
                if (CollectionUtils.isNotEmpty(allDataSource)) {
                    DataSourceConVO dataSourceConVO = allDataSource.stream().filter(source -> source.getDatasourceId() == dataCheckLogsVO.getFiDatasourceId()).findFirst().orElse(null);
                    if (dataSourceConVO != null) {
                        dataCheckLogsVO.setDataBaseIp(dataSourceConVO.getConIp());
                        dataCheckLogsVO.setDataBaseName(dataSourceConVO.getConDbname());
                    }
                }
                if (CollectionUtils.isNotEmpty(attachmentInfoPOList)) {
                    AttachmentInfoPO attachmentInfoPO = attachmentInfoPOList.stream().filter(t -> t.getObjectId().equals(dataCheckLogsVO.getIdUuid())).findFirst().orElse(null);
                    if (attachmentInfoPO != null) {
                        String filePath = "";
                        if (attachmentInfoPO.getAbsolutePath().endsWith("/")) {
                            filePath = attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName();
                        } else {
                            filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
                        }
                        File file = new File(filePath);
                        if (file.exists()) {
                            dataCheckLogsVO.setExistReport(true);
                            dataCheckLogsVO.setOriginalName(attachmentInfoPO.getOriginalName());
                        }
                    }
                }
                // 秒转分
                if (StringUtils.isNotEmpty(dataCheckLogsVO.getCheckDataDuration())) {
                    int minutes = Integer.parseInt(dataCheckLogsVO.getCheckDataDuration()) / 60;
                    int seconds = Integer.parseInt(dataCheckLogsVO.getCheckDataDuration()) % 60;
                    dataCheckLogsVO.setCheckDataDuration(minutes + "分" + seconds + "秒");
                }
            }
        }
        return all;
    }

    @Override
    public JSONArray getDataCheckLogsResult(long logId) {
        JSONArray jsonArray = null;
        Connection connection = dataSourceConManageImpl.getStatement(DataSourceTypeEnum.MYSQL, dataBaseUrl, dataBaseUserName, dataBasePassWord);
        String sql = String.format("SELECT error_data FROM tb_datacheck_rule_logs WHERE Id=%s", logId);
        List<Map<String, Object>> maps = AbstractCommonDbHelper.execQueryResultMaps(sql, connection);
        if (CollectionUtils.isNotEmpty(maps)) {
            Object error_data = maps.get(0).get("error_data");
            if (error_data != null) {
                jsonArray = JSON.parseArray(error_data.toString());
            }
        }
        return jsonArray;
    }

    @Override
    public ResultEnum deleteDataCheckLogs(long ruleId) {
        DataCheckPO dataCheckPO = baseMapper.selectById(ruleId);
        if (dataCheckPO == null) {
            return ResultEnum.DATA_QUALITY_RULE_NOTEXISTS;
        }
        QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
        dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                .eq(DataCheckExtendPO::getRuleId, ruleId);
        DataCheckExtendPO dataCheckExtendPO = dataCheckExtendMapper.selectOne(dataCheckExtendPOQueryWrapper);
        if (dataCheckExtendPO == null) {
            return ResultEnum.DATA_QUALITY_RULE_NOTEXISTS;
        }
        if (dataCheckExtendPO.getRecordErrorData() == 1 && dataCheckExtendPO.getErrorDataRetentionTime() != 0) {
            LocalDate currentDate = LocalDate.now();
            LocalDate targetDate = currentDate.minusDays(dataCheckExtendPO.getErrorDataRetentionTime());

            QueryWrapper<DataCheckLogsPO> dataCheckLogsPOQueryWrapper = new QueryWrapper<>();
            dataCheckLogsPOQueryWrapper.lambda().eq(DataCheckLogsPO::getRuleId, ruleId)
                    .gt(DataCheckLogsPO::getCreateTime, targetDate);
            dataCheckLogsMapper.delete(dataCheckLogsPOQueryWrapper);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public String createDataCheckResultExcel(String logIds) {
        if (StringUtils.isEmpty(logIds)) {
            return "";
        }
        // 第一步：根据日志id查询数据检查日志详情
        QueryWrapper<DataCheckLogsPO> dataCheckLogsPOQueryWrapper = new QueryWrapper<>();
        if (logIds.equals("all")) {
            dataCheckLogsPOQueryWrapper.lambda().eq(DataCheckLogsPO::getDelFlag, 1);
        } else {
            List<String> logIdList = Arrays.stream(logIds.split(",")).collect(Collectors.toList());
            dataCheckLogsPOQueryWrapper.lambda().eq(DataCheckLogsPO::getDelFlag, 1)
                    .in(DataCheckLogsPO::getId, logIdList);
        }
        List<DataCheckLogsPO> dataCheckLogList = dataCheckLogsMapper.selectList(dataCheckLogsPOQueryWrapper);
        if (CollectionUtils.isEmpty(dataCheckLogList)) {
            return "";
        }

        // 第二步：组装日志数据写入通用Excel对象
        List<SheetDto> sheetList = new ArrayList<>();
        int index = 0;
        for (DataCheckLogsPO dataCheckLogsPO : dataCheckLogList) {
            index++;

            SheetDto sheet = new SheetDto();
            String sheetName = dataCheckLogsPO.getRuleName() + "_Sheet" + index;
            sheet.setSheetName(sheetName);

            String errorData = dataCheckLogsPO.getErrorData();
            JSONArray jsonArray = null;
            if (StringUtils.isNotEmpty(errorData)) {
                jsonArray = JSON.parseArray(errorData);
            }
            List<RowDto> singRows = createDataCheckResultExcel_GetSingRows(dataCheckLogsPO, jsonArray);
            sheet.setSingRows(singRows);

            if (StringUtils.isNotEmpty(dataCheckLogsPO.getFieldName())) {
                sheet.setSingFields(Arrays.asList(dataCheckLogsPO.getFieldName().split(",")));
            }

            List<List<String>> dataRowList = createDataCheckResultExcel_GetDataRows(jsonArray);
            sheet.setDataRows(dataRowList);
            sheetList.add(sheet);
        }

        // 第三步：调用生成Excel的方法
        String currentFileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        String uploadUrl = excelFilePath + "dataCheckResult_excelFile/";

        ExcelDto excelDto = new ExcelDto();
        excelDto.setExcelName(currentFileName);
        excelDto.setSheets(sheetList);
        ExcelReportUtil.createExcel(excelDto, uploadUrl, currentFileName, true);

        // 第四步：数据库记录Excel附件信息并返回附件Id用于下载附件
        AttachmentInfoPO attachmentInfoPO = new AttachmentInfoPO();
        attachmentInfoPO.setCurrentFileName(currentFileName);
        attachmentInfoPO.setExtensionName(".xlsx");
        attachmentInfoPO.setAbsolutePath(uploadUrl);
        attachmentInfoPO.setOriginalName(String.format("数据检查日志%s.xlsx", DateTimeUtils.getNowToShortDate().replace("-", "")));
        attachmentInfoPO.setCategory(AttachmentCateGoryEnum.DATA_INSPECTION_LOG_REPORT.getValue());
        attachmentInfoMapper.insertOne(attachmentInfoPO);

        return attachmentInfoPO.getOriginalName() + "," + attachmentInfoPO.getId();
    }

    public List<RowDto> createDataCheckResultExcel_GetSingRows(DataCheckLogsPO dataCheckLogsPO, JSONArray jsonArray) {
        List<RowDto> singRows = new ArrayList<>();
        RowDto rowDto = new RowDto();
        rowDto.setRowIndex(0);
        List<String> Columns = new ArrayList<>();
        Columns.add("执行环节");
        Columns.add("规则类型");
        Columns.add("代号");
        Columns.add("表字段");
        Columns.add("检查结果");
        Columns.add("检查时间");
        Columns.add("检查描述");
        Columns.add("大小批次号");
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        rowDto = new RowDto();
        rowDto.setRowIndex(1);
        Columns = new ArrayList<>();
        Columns.add(dataCheckLogsPO.getLogType() == 1 ? "同步前" : dataCheckLogsPO.getLogType() == 2 ? "同步中" : "同步后");
        Columns.add(dataCheckLogsPO.getCheckTemplateName());
        Columns.add(dataCheckLogsPO.getRuleName());
        String tableName = "";
        if (StringUtils.isNotEmpty(dataCheckLogsPO.getSchemaName())) {
            tableName = dataCheckLogsPO.getSchemaName();
        }
        if (StringUtils.isNotEmpty(dataCheckLogsPO.getTableName())) {
            tableName += "." + dataCheckLogsPO.getTableName();
        }
        if (StringUtils.isNotEmpty(dataCheckLogsPO.getFieldName())) {
            tableName += "." + dataCheckLogsPO.getFieldName();
        }
        Columns.add(tableName);
        String checkResult = "";
        if (dataCheckLogsPO.getCheckResult().equals(SUCCESS)) {
            checkResult = String.format("%s,共检查%s条数据，全部通过", SUCCESS, dataCheckLogsPO.getCheckTotalCount());
        } else if (dataCheckLogsPO.getCheckResult().equals(FAIL)) {
            checkResult = String.format("%s,共检查%s条数据，%s条未通过", FAIL, dataCheckLogsPO.getCheckTotalCount(), dataCheckLogsPO.getCheckFailCount());
        } else {
            checkResult = String.format("%s,共检查%s条数据，%s条已标记警告", WARN, dataCheckLogsPO.getCheckTotalCount(), dataCheckLogsPO.getCheckFailCount());
        }
        Columns.add(checkResult);
        Columns.add(dataCheckLogsPO.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Columns.add(dataCheckLogsPO.getCheckRuleIllustrate());
        Columns.add(dataCheckLogsPO.getCheckBatchNumber() + "/" + dataCheckLogsPO.getCheckSmallBatchNumber());
        rowDto.setColumns(Columns);
        singRows.add(rowDto);

        if (CollectionUtils.isNotEmpty(jsonArray)) {
            rowDto = new RowDto();
            rowDto.setRowIndex(3);
            Columns = new ArrayList<>();
            Columns.add("检查结果明细");
            rowDto.setColumns(Columns);
            singRows.add(rowDto);

            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            List<String> fieldList = jsonObject.keySet().stream().collect(Collectors.toList());
            rowDto = new RowDto();
            rowDto.setRowIndex(4);
            Columns = new ArrayList<>();
            Columns.addAll(fieldList);
            rowDto.setColumns(Columns);
            singRows.add(rowDto);
        }
        return singRows;
    }

    public List<List<String>> createDataCheckResultExcel_GetDataRows(JSONArray jsonArray) {
        List<List<String>> dataRowList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Set<Map.Entry<String, Object>> entrySet = jsonArray.getJSONObject(i).entrySet();
                List<String> dataRow = new ArrayList<>();
                for (Map.Entry<String, Object> entry : entrySet) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    dataRow.add(value != null ? value.toString() : "");
                }
                dataRowList.add(dataRow);
            }
        }
        return dataRowList;
    }

    /**
     * @return com.fisk.common.core.response.ResultEnum
     * @description 定时清理存储在数据库的错误数据
     * @author dick
     * @date 2024/7/30 16:27
     * @version v1.0
     * @params
     */
    public ResultEnum deleteCheckResult() {
        // 第一步：设置计划任务每天凌晨12点执行
        // 第二步：检查校验规则是否设置错误数据保留时间
        List<DeleteCheckResultVO> deleteDataCheckResult = baseMapper.getDeleteDataCheckResult();
        if (CollectionUtils.isEmpty(deleteDataCheckResult)) {
            return ResultEnum.SUCCESS;
        }
        StringBuilder updateSqlBuilder = new StringBuilder();
        String toDate = DateTimeUtils.getNowToShortDate();
        String toDateTime = DateTimeUtils.getNow();
        for (DeleteCheckResultVO item : deleteDataCheckResult) {
            String dateAddReduceDay = DateTimeUtils.getDateAddReduceDay(toDate, 0 - (item.getErrorDataRetentionTime() - 1));
            String updateSql = String.format("UPDATE tb_datacheck_rule_logs SET del_flag=0 , update_time='%s' , update_user='system task'" +
                    " WHERE rule_id = %s AND create_time<'%s';", toDateTime, item.getRuleId(), dateAddReduceDay);
            updateSqlBuilder.append(updateSql);
        }
        Connection connection = dataSourceConManageImpl.getStatement(DataSourceTypeEnum.MYSQL, dataBaseUrl, dataBaseUserName, dataBasePassWord);
        AbstractCommonDbHelper.executeSql_Close(updateSqlBuilder.toString(), connection);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<DataSourceInfoDTO> getDataSourceTree(Integer dbId) {
        List<DataSourceInfoDTO> list = new ArrayList<>();
        ResultEntity<DataSourceDTO> dataSource = userClient.getFiDataDataSourceById(dbId);
        if (dataSource.getCode() != ResultEnum.SUCCESS.getCode() || dataSource.data == null) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        DataSourceDTO dataSourceDTO = dataSource.data;
        DataSourceInfoDTO data = new DataSourceInfoDTO();
        data.dbId = dataSourceDTO.id;
        data.dataSourceName = dataSourceDTO.conDbname;
        DataBaseInfoDTO dataBaseInfoDTO = new DataBaseInfoDTO();
        dataBaseInfoDTO.setDbName(dataSourceDTO.name);
        dataBaseInfoDTO.setTableNameList(getDbTable(dataSourceDTO));
        List<DataBaseInfoDTO> dataBaseInfoDTOList = new ArrayList<>();
        dataBaseInfoDTOList.add(dataBaseInfoDTO);
        data.setDataBaseInfoDTOList(dataBaseInfoDTOList);
        list.add(data);
        return list;
    }

    @Override
    public List<TableColumnDTO> getColumn(ColumnQueryDTO dto) {
        List<TableColumnDTO> tableColumnDTOS = new ArrayList<>();
        switch (dto.tableBusinessTypeEnum) {
            case NONE:
                ResultEntity<Object> fieldsDataStructure1 = dataAccessClient.getFieldsDataStructure(dto);
                if (fieldsDataStructure1.code == ResultEnum.SUCCESS.getCode()) {
                    tableColumnDTOS = (List<TableColumnDTO>) fieldsDataStructure1.data;
                }
                break;
            case DW_FACT:
            case DW_DIMENSION:
            case DORIS_DIMENSION:
            case WIDE_TABLE:
                ResultEntity<Object> fieldsDataStructure2 = dataModelClient.getFieldDataStructure(dto);
                if (fieldsDataStructure2.code == ResultEnum.SUCCESS.getCode()) {
                    tableColumnDTOS = (List<TableColumnDTO>) fieldsDataStructure2.data;
                }
                break;
            case ENTITY_TABLR:
                ResultEntity<Object> fieldsDataStructure3 = mdmClient.getFieldDataStructure(dto);
                if (fieldsDataStructure3.code == ResultEnum.SUCCESS.getCode()) {
                    tableColumnDTOS = (List<TableColumnDTO>) fieldsDataStructure3.data;
                }
                break;
        }
        return tableColumnDTOS;
    }

    @Override
    public Integer getDataCheckRoleTotal() {
        return this.baseMapper.getDataCheckRoleTotal();
    }

    /**
     * 获取数据源所有表
     *
     * @param dto
     * @return
     */
    public List<TableNameDTO> getDbTable(DataSourceDTO dto) {
        try {
            List<TableNameDTO> data = new ArrayList<>();
            FiDataMetaDataReqDTO reqDTO = new FiDataMetaDataReqDTO();
            reqDTO.setDataSourceId(String.valueOf(dto.getId()));
            reqDTO.setDataSourceName(dto.getConDbname());
            switch (dto.sourceBusinessType) {
                case NONE:
                    break;
                case DW:
                case OLAP:
                    ResultEntity<Object> tableDataStructure1 = dataModelClient.getTableDataStructure(reqDTO);
                    if (tableDataStructure1.code == ResultEnum.SUCCESS.getCode()) {
                        data = (List<TableNameDTO>) tableDataStructure1.data;
                    }
                    break;
                case ODS:
                    ResultEntity<Object> tableDataStructure2 = dataAccessClient.getTableDataStructure(reqDTO);
                    if (tableDataStructure2.code == ResultEnum.SUCCESS.getCode()) {
                        data = (List<TableNameDTO>) tableDataStructure2.data;
                    }
                    break;
                case MDM:
                    ResultEntity<Object> tableDataStructure3 = mdmClient.getTableDataStructure(reqDTO);
                    if (tableDataStructure3.code == ResultEnum.SUCCESS.getCode()) {
                        data = (List<TableNameDTO>) tableDataStructure3.data;
                    }
                    break;
            }
            return data;
        } catch (Exception e) {
            log.error("【获取表信息失败】,{}", e);
            return null;
        }
    }
}
