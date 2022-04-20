package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.office.excel.ExcelUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.notice.SystemNoticeVO;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口实现类
 * @date 2022/4/12 13:47
 */
@Service
@Slf4j
public class DataQualityClientManageImpl implements IDataQualityClientManageService {

    @Resource
    UserHelper userHelper;

    @Resource
    TemplateMapper templateMapper;

    @Resource
    DataSourceConMapper dataSourceConMapper;

    @Resource
    DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    DataCheckMapper dataCheckMapper;

    @Resource
    DataCheckManageImpl dataCheckManageImpl;

    @Resource
    BusinessFilterMapper businessFilterMapper;

    @Resource
    BusinessFilterManageImpl businessFilterManageImpl;

    @Resource
    LifecycleMapper lifecycleMapper;

    @Resource
    LifecycleManageImpl lifecycleManageImpl;

    @Resource
    NoticeMapper noticeMapper;

    @Resource
    NoticeManageImpl noticeManageImpl;

    @Resource
    EmailServerMapper emailServerMapper;

    @Resource
    EmailServerManageImpl emailServerManageImpl;

    @Resource
    ComponentNotificationMapper componentNotificationMapper;

    @Resource
    DataManageClient dataManageClient;

    @Value("${dataquality.excel.path}")
    private String excelPath;

    @Override
    public ResultEntity<Object> buildFieldStrongRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.FIELD_STRONG_RULE_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段强规则模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段强规则模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段强规则模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段强规则模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.FIELD_STRONG_RULE_TEMPLATE);
            if (CollectionUtils.isEmpty(resultVOS)) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段强规则模板规则已消费，执行校验规则查询无结果");
                return result;
            }
            List<DataCheckResultVO> checkResult = resultVOS.stream().filter(t -> t.getCheckResult() == "fail").collect(Collectors.toList());
            // 检查结果无异常，返回操作结果
            if (CollectionUtils.isEmpty(checkResult)) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("字段强规则模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.FIELD_STRONG_RULE_TEMPLATE, null, null, null);
        } catch (Exception ex) {
            log.error("buildFieldStrongRule消费时触发异常，请求参数:", String.format("模板类型：字段强规则模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildFieldStrongRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【字段强规则模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildFieldAggregateRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.FIELD_AGGREGATE_THRESHOLD_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.FIELD_AGGREGATE_THRESHOLD_TEMPLATE);
            if (CollectionUtils.isEmpty(resultVOS)) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，执行校验规则查询无结果");
                return result;
            }
            List<DataCheckResultVO> checkResult = resultVOS.stream().filter(t -> t.getCheckResult() == "fail").collect(Collectors.toList());
            // 检查结果无异常，返回操作结果
            if (CollectionUtils.isEmpty(checkResult)) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("字段聚合波动阈值模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.FIELD_AGGREGATE_THRESHOLD_TEMPLATE, null, null, null);
        } catch (Exception ex) {
            log.error("buildFieldAggregateRule消费时触发异常，请求参数:", String.format("模板类型：字段聚合波动阈值模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildFieldAggregateRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【字段聚合波动阈值模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildTableRowThresholdRule(DataQualityRequestDTO requestDTO) {
        /*
         * 1、实时查询的的表行数 减去 记录的表行数 大于等于 波动阀值，发送邮件；
         * 2、更新配置表记录的表行数，赋值为实时查询的的表行数;更新配置表记录的表行数前提是 记录的表行数 大于等于 波动阀值
         * */
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.ROWCOUNT_THRESHOLD_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.ROWCOUNT_THRESHOLD_TEMPLATE);
            if (CollectionUtils.isEmpty(resultVOS)) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，执行校验规则查询无结果");
                return result;
            }
            DataCheckResultVO dataCheckResultVO = resultVOS.get(0);
            int tableRowCount = Integer.parseInt(dataCheckResultVO.getCheckResult().toString());
            boolean checkResult = tableRowCount - dataCheckPO.rowsValue < dataCheckPO.thresholdValue;
            // 检查结果无异常，返回操作结果
            if (checkResult) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("表行数波动阈值模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知并更新配置表记录的表行数
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.ROWCOUNT_THRESHOLD_TEMPLATE, null, null, null);
            if (result != null && result.getCode() == ResultEnum.SUCCESS.getCode()) {
                dataCheckPO.setRowsValue(tableRowCount);
                int i = dataCheckMapper.updateById(dataCheckPO);
                if (i <= 0) {
                    return ResultEntityBuild.buildData(ResultEnum.ERROR, "表行数波动阈值模板规则已消费，更新配置表记录的表行数失败");
                }
            }
        } catch (Exception ex) {
            log.error("buildTableRowThresholdRule消费时触发异常，请求参数:", String.format("模板类型：表行数波动阈值模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildTableRowThresholdRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【表行数波动阈值模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableCheckRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("空表校验模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("空表校验模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("空表校验模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("空表校验模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE);
            if (CollectionUtils.isEmpty(resultVOS)) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("空表校验模板规则已消费，执行校验规则查询无结果");
                return result;
            }
            List<DataCheckResultVO> checkResult = resultVOS.stream().filter(t -> t.getCheckResult() == "fail").collect(Collectors.toList());
            // 检查结果无异常，返回操作结果
            if (CollectionUtils.isEmpty(checkResult)) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("空表校验模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE, null, null, null);
        } catch (Exception ex) {
            log.error("buildEmptyTableCheckRule消费时触发异常，请求参数:", String.format("模板类型：空表校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildEmptyTableCheckRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【空表校验模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildUpdateTableRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表更新校验模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表更新校验模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表更新校验模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表更新校验模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取校验结果
            List<DataCheckResultVO> resultVOS = resultSetToJsonArray(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE);
            if (CollectionUtils.isEmpty(resultVOS)) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表更新校验模板规则已消费，执行校验规则查询无结果");
                return result;
            }
            List<DataCheckResultVO> checkResult = resultVOS.stream().filter(t -> t.getCheckResult() == "success").collect(Collectors.toList());
            // 检查结果无异常，返回操作结果
            if (CollectionUtils.isEmpty(checkResult)) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("表更新校验模板规则已消费，校验通过");
                return result;
            }
            // 检查结果发现表存在更新，发送通知&重新生成SQL检查规则并保存到配置表，因为要更新参照时间
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE, null, null, null);
            if (result != null && result.getCode() == ResultEnum.SUCCESS.getCode()) {
                DataCheckDTO dataCheckDTO = new DataCheckDTO();
                dataCheckDTO.setDatasourceId(dataCheckPO.getDatasourceId());
                dataCheckDTO.setDatasourceType(ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType()));
                dataCheckDTO.setCheckStep(CheckStepTypeEnum.getEnum(dataCheckPO.getCheckStep()));
                dataCheckDTO.setTableName(dataCheckPO.getTableName());
                dataCheckDTO.setProTableName(dataCheckPO.getProTableName());
                dataCheckDTO.setFieldName(dataCheckPO.getFieldName());
                ResultEntity<String> roleResult = dataCheckManageImpl.createRole(dataCheckDTO, TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE);
                if (roleResult != null && roleResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                    dataCheckPO.setModuleRule(roleResult.getData());
                    int i = dataCheckMapper.updateById(dataCheckPO);
                    if (i <= 0) {
                        result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                        result.setMsg("表更新校验模板规则已消费，更新配置表的校验规则失败");
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("buildUpdateTableRule消费时触发异常，请求参数:", String.format("模板类型：表更新校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildUpdateTableRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【表更新校验模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildTableBloodKinshipRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表血缘断裂校验模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表血缘断裂校验模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("表血缘断裂校验模板规则已消费，数据源不存在");
                return result;
            }
            // 调用元数据接口，查询表是否存在上下游血缘
            UpperLowerBloodParameterDTO bloodParameterDTO = new UpperLowerBloodParameterDTO();
            bloodParameterDTO.setInstanceName(dataSourceConPO.getConIp());
            bloodParameterDTO.setDbName(dataSourceConPO.getConDbname());
            String tableName = CheckStepTypeEnum.getEnum(dataCheckPO.checkStep) == CheckStepTypeEnum.TABLE_FRONT ? dataCheckPO.proTableName : dataCheckPO.tableName;
            bloodParameterDTO.setTableName(tableName);
            bloodParameterDTO.setCheckConsanguinity(dataCheckPO.getCheckConsanguinity());
            ResultEntity<Object> bloodResult = dataManageClient.existUpperLowerBlood(bloodParameterDTO);
            boolean isExistBlood = true;
            if (bloodResult != null && bloodResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                isExistBlood = Boolean.valueOf(bloodResult.getData().toString());
            }
            // 检查结果无异常，返回操作结果
            if (isExistBlood) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("表血缘断裂校验模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(),
                    TemplateTypeEnum.TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE, null, null, null);
        } catch (Exception ex) {
            log.error("buildTableBloodKinshipRule消费时触发异常，请求参数:", String.format("模板类型：表血缘断裂校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildTableBloodKinshipRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【表血缘断裂校验模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildBusinessCheckRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.BUSINESS_CHECK_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的数据校验规则
            DataCheckPO dataCheckPO = dataCheckMapper.selectById(requestDTO.getId());
            if (dataCheckPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务验证模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(dataCheckPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务验证模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(dataCheckPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dataCheckPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务验证模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = dataCheckPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务验证模板规则已消费，校验规则不存在");
                return result;
            }
            // 读取校验结果
            List<Map<String, Object>> mapList = resultSetToMap(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.BUSINESS_CHECK_TEMPLATE);
            if (mapList != null && mapList.size() > 0 && excelPath != null && !excelPath.isEmpty()) {
                // 发送校验附件保存，发送校验报告（含附件）
                String fileName = "业务验证模板执行结果.xlsx";
                String filePaht = excelPath + File.separator + "businessCheckFile";
                result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(),
                        dataCheckPO.getCreateUser(), TemplateTypeEnum.BUSINESS_CHECK_TEMPLATE, fileName, filePaht, mapList);
            }
        } catch (Exception ex) {
            log.error("buildBusinessCheckRule消费时触发异常，请求参数:", String.format("模板类型：业务验证模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildBusinessCheckRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【业务验证模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildSimilarityRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildBusinessFilterRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.BIZCHECK_MODULE, TemplateTypeEnum.BUSINESS_FILTER_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的业务清洗规则
            BusinessFilterPO businessFilterPO = businessFilterMapper.selectById(requestDTO.getId());
            if (businessFilterPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务清洗模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(businessFilterPO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务清洗模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(businessFilterPO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(businessFilterPO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务清洗模板规则已消费，数据源不存在");
                return result;
            }
            // 校验语句
            String moduleRuleSql = businessFilterPO.getModuleRule();
            if (moduleRuleSql == null || moduleRuleSql.isEmpty()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("业务清洗模板规则已消费，校验规则不存在");
                return result;
            }
            // 获取清洗结果
            int i = executeSql(dataSourceConPO, moduleRuleSql, TemplateTypeEnum.BUSINESS_FILTER_TEMPLATE);
            // 发送清洗完成通知
            result = sendNotice(businessFilterPO.getTemplateId(), businessFilterPO.getId(),
                    businessFilterPO.getCreateUser(), TemplateTypeEnum.BUSINESS_FILTER_TEMPLATE, null, null, null);
        } catch (Exception ex) {
            log.error("buildBusinessFilterRule消费时触发异常，请求参数:", String.format("模板类型：业务清洗模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildBusinessFilterRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【业务清洗模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildSpecifyTimeRecyclingRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.LIFECYCLE_MODULE, TemplateTypeEnum.SPECIFY_TIME_RECYCLING_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的生命周期规则
            LifecyclePO lifecyclePO = lifecycleMapper.selectById(requestDTO.getId());
            if (lifecyclePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("指定时间回收模板规则已消费，模板组件规则不存在");
                return result;
            }
            TemplatePO templatePO = templateMapper.selectById(lifecyclePO.getTemplateId());
            if (templatePO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("指定时间回收模板规则已消费，模板不存在");
                return result;
            }
            // 第三步:查询数据源信息，执行校验语句
            ModuleDataSourceTypeEnum dataSourceTypeEnum = ModuleDataSourceTypeEnum.getEnum(lifecyclePO.getDatasourceType());
            DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(lifecyclePO.getDatasourceId(), dataSourceTypeEnum);
            if (dataSourceConPO == null) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("指定时间回收模板规则已消费，数据源不存在");
                return result;
            }
            // 判断表是否冻结，已冻结直接返回
            if (lifecyclePO.tableState == TableStateTypeEnum.RECGCLED.getValue()) {
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("指定时间回收模板规则已消费，表已冻结");
                return result;
            }
            String nowToShortDate = DateTimeUtils.getNowToShortDate();
            int timeDifference_day = DateTimeUtils.getTimeDifference_Day(lifecyclePO.recoveryDate, nowToShortDate);
            if (timeDifference_day < 0) {
                // 回收时间-当前时间 为负数，返回结果
                result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
                result.setMsg("指定时间回收模板规则已消费，回收时间减当前时间小于0，数据异常");
                return result;
            } else if (timeDifference_day == 0) {
                // 回收时间-当前时间 等于0时，修改表状态为已冻结;冻结表;冻结该表的数据检验、业务清洗、生命周期规则;冻结相关的调度任务
                lifecyclePO.setTableState(TableStateTypeEnum.RECGCLED.getValue());
                lifecycleMapper.updateById(lifecyclePO);
                result = frozenTable(dataSourceConPO, lifecyclePO, TemplateTypeEnum.SPECIFY_TIME_RECYCLING_TEMPLATE);
            } else if (timeDifference_day <= lifecyclePO.remindDate) {
                // 回收时间-当前时间 如果小于等于提醒时间，开始发邮件提醒。
                // 发送通知
                result = sendNotice(lifecyclePO.getTemplateId(), lifecyclePO.getId(),
                        lifecyclePO.getCreateUser(), TemplateTypeEnum.SPECIFY_TIME_RECYCLING_TEMPLATE, null, null, null);
            }
        } catch (Exception ex) {
            log.error("buildBusinessFilterRule消费时触发异常，请求参数:", String.format("模板类型：指定时间回收模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType().getName()));
            log.error("buildBusinessFilterRule消费时触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.ERROR, "【指定时间回收模板】：" + ex);
        }
        return result;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableRecoveryRule(DataQualityRequestDTO requestDTO) {
        // 回收时间-持续天数如果等于提醒时间，开始发邮件。等到回收天数等于持续天数时，直接回收表并将持续天数清零
        return null;
    }

    @Override
    public ResultEntity<Object> buildNoRefreshDataRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildDataBloodKinshipRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }


    public ResultEntity<Object> frozenTable(DataSourceConPO dataSourceConPO, LifecyclePO lifecyclePO, TemplateTypeEnum templateTypeEnum) {
        ResultEntity<Object> result = new ResultEntity<>();
        // 冻结表
        String sql = String.format("DROP TABLE %s;", lifecyclePO.tableName);
        int i = executeSql(dataSourceConPO, sql, templateTypeEnum);
        // 冻结该表的数据校验、业务清洗、生命周期组件和调度任务
        QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
        dataCheckPOQueryWrapper.lambda().eq(DataCheckPO::getDelFlag, 1)
                .eq(DataCheckPO::getDatasourceId, lifecyclePO.datasourceId)
                .eq(DataCheckPO::getTableName, lifecyclePO.tableName);
        List<DataCheckPO> dataCheckPOList = dataCheckMapper.selectList(dataCheckPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataCheckPOList)) {
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                dataCheckManageImpl.deleteData(Math.toIntExact(dataCheckPO.getId()));
            }
        }

        QueryWrapper<BusinessFilterPO> businessFilterPOQueryWrapper = new QueryWrapper<>();
        businessFilterPOQueryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1)
                .eq(BusinessFilterPO::getDatasourceId, lifecyclePO.datasourceId)
                .eq(BusinessFilterPO::getTableName, lifecyclePO.tableName);
        List<BusinessFilterPO> businessFilterPOList = businessFilterMapper.selectList(businessFilterPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(businessFilterPOList)) {
            for (BusinessFilterPO businessFilterPO : businessFilterPOList) {
                businessFilterManageImpl.deleteData(Math.toIntExact(businessFilterPO.getId()));
            }
        }

        QueryWrapper<LifecyclePO> lifecyclePOQueryWrapper = new QueryWrapper<>();
        lifecyclePOQueryWrapper.lambda().eq(LifecyclePO::getDelFlag, 1)
                .eq(LifecyclePO::getDatasourceId, lifecyclePO.datasourceId)
                .eq(LifecyclePO::getTableName, lifecyclePO.tableName);
        List<LifecyclePO> lifecyclePOList = lifecycleMapper.selectList(lifecyclePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(lifecyclePOList)) {
            for (LifecyclePO lifecyclePO1 : lifecyclePOList) {
                lifecycleManageImpl.deleteData(Math.toIntExact(lifecyclePO1.getId()));
            }
        }
        result.setCode(ResultEnum.SUCCESS.getCode());
        return result;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @description 发送告警通知
     * @author dick
     * @date 2022/4/12 20:54
     * @version v1.0
     * @params templateId 模板id
     * @params moduleId 组件id
     * @params templateTypeEnum 模板类型
     * @params body 正文内容
     * @params fileName 文件名称
     * @params filePath 文件全路径，含文件名称
     * @params mapList 附件数据
     */
    public ResultEntity<Object> sendNotice(int templateId, long moduleId, String userId,
                                           TemplateTypeEnum templateTypeEnum, String fileName, String filePath,
                                           List<Map<String, Object>> mapList) {
        ResultEntity<Object> result = new ResultEntity<>();
        List<SystemNoticeVO> systemNoticeVOS = new ArrayList<>();
        // 检查结果异常，发送提示邮件
        QueryWrapper<ComponentNotificationPO> notificationPOQueryWrapper = new QueryWrapper<>();
        notificationPOQueryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1)
                .eq(ComponentNotificationPO::getTemplateId, templateId)
                .eq(ComponentNotificationPO::getModuleId, moduleId);
        List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(notificationPOQueryWrapper);
        if (CollectionUtils.isEmpty(componentNotificationPOS)) {
            result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
            result.setMsg(templateTypeEnum.getName() + "规则已消费，未配置告警通知");
            return result;
        }
        // 查询告警通知方式
        List<Integer> noticeIdList = componentNotificationPOS.stream().map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
        QueryWrapper<NoticePO> noticePOQueryWrapper = new QueryWrapper<>();
        noticePOQueryWrapper.lambda().eq(NoticePO::getDelFlag, 1)
                .in(NoticePO::getId, noticeIdList);
        List<NoticePO> noticePOS = noticeMapper.selectList(noticePOQueryWrapper);
        if (CollectionUtils.isEmpty(noticePOS)) {
            result.setCode(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL.getCode());
            result.setMsg(templateTypeEnum.getName() + "规则已消费，告警方式不存在");
            return result;
        }
        // 邮件通知
        List<NoticePO> emailNoticePOS = noticePOS.stream().filter(t -> t.getNoticeType() == 1).collect(Collectors.toList());
        // 系统通知
        List<NoticePO> systemNoticePOS = noticePOS.stream().filter(t -> t.getNoticeType() == 2).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(emailNoticePOS)) {
            boolean sendAttachment = false;
            if (TemplateTypeEnum.BUSINESS_CHECK_TEMPLATE == templateTypeEnum
                    && mapList != null && mapList.size() > 0) {
                // 验证业务模板才有附件
                ExcelUtil.createSaveExcel(filePath, null, mapList);
                sendAttachment = true;
            }
            for (NoticePO emailNoticePO : emailNoticePOS) {
                NoticeDTO noticeDTO = new NoticeDTO();
                noticeDTO.emailServerId = emailNoticePO.getEmailServerId();
                noticeDTO.emailSubject = emailNoticePO.getEmailSubject();
                noticeDTO.body = emailNoticePO.getBody();
                noticeDTO.emailConsignee = emailNoticePO.getEmailConsignee();
                noticeDTO.emailCc = emailNoticePO.getEmailCc();
                noticeDTO.sendAttachment = sendAttachment;
                noticeDTO.attachmentName = fileName;
                noticeDTO.attachmentPath = filePath;
                ResultEntity<Object> sendEmialNotice = noticeManageImpl.sendEmialNotice(noticeDTO);
                if (sendEmialNotice != null && sendEmialNotice.getCode() == ResultEnum.DATA_NOTEXISTS.getCode()) {
                    // 邮件服务器不存在不直接抛出异常
                    log.info(String.format("【%s】:邮件服务器不存在，通知id：%s，邮件服务器id：%s",
                            templateTypeEnum.getName(), emailNoticePO.getId(), emailNoticePO.getEmailServerId()));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(systemNoticePOS) && userId != null && !userId.isEmpty()) {
            // 返回系统通知消息，在task中调用站内消息提醒
            for (NoticePO t : systemNoticePOS) {
                SystemNoticeVO systemNoticeVO = new SystemNoticeVO();
                systemNoticeVO.userId = Integer.parseInt(userId);
                systemNoticeVO.msg = t.getBody();
                systemNoticeVOS.add(systemNoticeVO);
            }
            result.setData(systemNoticeVOS);
        }
        result.setCode(ResultEnum.SUCCESS.getCode());
        result.setMsg(templateTypeEnum.getName() + "规则已消费，已执行告警通知");
        return result;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>
     * @description 执行sql，返回结果
     * @author dick
     * @date 2022/4/12 18:50
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     */
    public List<DataCheckResultVO> resultSetToJsonArray(DataSourceConPO dataSourceConPO, String
            sql, TemplateTypeEnum templateTypeEnum) {
        List<DataCheckResultVO> resultVOS = new ArrayList<>();
        Statement st = null;
        Connection conn = null;
        try {
            JSONArray array = new JSONArray();
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            // JDBC 读取大量数据时的 ResultSet resultSetType 设置TYPE_FORWARD_ONLY
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
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
            rs.close();
            if (array != null && array.size() > 0) {
                resultVOS = array.toJavaList(DataCheckResultVO.class);
            }
        } catch (Exception ex) {
            log.error("resultSetToJsonArray触发异常，请求参数:", String.format("模板类型：%s，数据源id:%s，数据源类型：%s，连接字符串：%s，sql：%s",
                    templateTypeEnum.getName(), dataSourceConPO.getId(), dataSourceConPO.getConType(), dataSourceConPO.getConStr(), sql));
            log.error("resultSetToJsonArray触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】:" + ex);
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】数据库连接关闭异常:" + ex);
            }
        }
        return resultVOS;
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @description 根据SQL查询，转Map
     * @author dick
     * @date 2022/4/18 11:00
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     * @params templateTypeEnum
     */
    public List<Map<String, Object>> resultSetToMap(DataSourceConPO dataSourceConPO, String
            sql, TemplateTypeEnum templateTypeEnum) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Statement st = null;
        Connection conn = null;
        try {
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            // JDBC 读取大量数据时的 ResultSet resultSetType 设置TYPE_FORWARD_ONLY
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> objectMap = new IdentityHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(columnName);
                    objectMap.put(columnName, value);
                }
                mapList.add(objectMap);
            }
            rs.close();
        } catch (Exception ex) {
            log.error("resultSetToMap触发异常，请求参数:", String.format("模板类型：%s，数据源id:%s，数据源类型：%s，连接字符串：%s，sql：%s",
                    templateTypeEnum.getName(), dataSourceConPO.getId(), dataSourceConPO.getConType(), dataSourceConPO.getConStr(), sql));
            log.error("resultSetToMap触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】:" + ex);
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】数据库连接关闭异常:" + ex);
            }
        }
        return mapList;
    }

    /**
     * @return int
     * @description 执行sql，返回结果
     * @author dick
     * @date 2022/4/18 11:15
     * @version v1.0
     * @params dataSourceConPO
     * @params sql
     * @params templateTypeEnum
     */
    public int executeSql(DataSourceConPO dataSourceConPO, String
            sql, TemplateTypeEnum templateTypeEnum) {
        int affectedCount = 0;
        Statement st = null;
        Connection conn = null;
        try {
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象

            conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            st = conn.createStatement();
            assert st != null;
            /*
              boolean execute(String sql)
              允许执行查询语句、更新语句、DDL语句。
              返回值为true时，表示执行的是查询语句，可以通过getResultSet方法获取结果；
              返回值为false时，执行的是更新语句或DDL语句，getUpdateCount方法获取更新的记录数量。

              int executeUpdate(String sql)
              执行给定 SQL 语句，该语句可能为 INSERT、UPDATE、DELETE、DROP 语句，或者不返回任何内容的 SQL 语句（如 SQL DDL 语句）。
              返回值是更新的记录数量
             */
            affectedCount = st.executeUpdate(sql);
        } catch (Exception ex) {
            log.error("executeSql触发异常，请求参数:", String.format("模板类型：%s，数据源id:%s，数据源类型：%s，连接字符串：%s，sql：%s",
                    templateTypeEnum.getName(), dataSourceConPO.getId(), dataSourceConPO.getConType(), dataSourceConPO.getConStr(), sql));
            log.error("executeSql触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】:" + ex);
        } finally {
            try {
                if (st != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                // do nothing
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】数据库连接关闭异常:" + ex);
            }
        }
        return affectedCount;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.Object>
     * @description 验证请求参数是否合法
     * @author dick
     * @date 2022/4/12 17:00
     * @version v1.0
     * @params requestDTO
     * @params typeEnum
     */
    public ResultEntity<Object> paramterVerification(DataQualityRequestDTO requestDTO,
                                                     TemplateModulesTypeEnum typeEnum,
                                                     TemplateTypeEnum templateTypeEnum) {
        if (requestDTO == null || requestDTO.getId() == 0
                || requestDTO.getTemplateModulesType() != typeEnum) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_SCHEDULE_TASK_FAIL, templateTypeEnum.getName() + "规则消费失败，请求参数异常");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, templateTypeEnum.getName() + "规则消费成功，请求参数正常");
    }
}
