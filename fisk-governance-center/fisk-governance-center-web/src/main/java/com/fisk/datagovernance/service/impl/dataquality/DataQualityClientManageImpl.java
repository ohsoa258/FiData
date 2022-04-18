package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    LifecycleMapper lifecycleMapper;

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

    @Override
    public ResultEntity<Object> buildFieldStrongRule(DataQualityRequestDTO requestDTO) {
        ResultEntity<Object> result = null;
        try {
            // 第一步：验证请求参数是否合法
            result = paramterVerification(requestDTO, TemplateModulesTypeEnum.DATACHECK_MODULE, TemplateTypeEnum.FIELD_STRONG_RULE_TEMPLATE);
            if (result.code != ResultEnum.SUCCESS.getCode()) {
                return result;
            }
            // 第二步：查询配置的强类型校验规则
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
                    TemplateTypeEnum.FIELD_STRONG_RULE_TEMPLATE);
        } catch (Exception ex) {
            log.error("buildFieldStrongRule消费时触发异常，请求参数:", String.format("模板类型：字段强规则模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.FIELD_AGGREGATE_THRESHOLD_TEMPLATE);
        } catch (Exception ex) {
            log.error("buildFieldAggregateRule消费时触发异常，请求参数:", String.format("模板类型：字段聚合波动阈值模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.ROWCOUNT_THRESHOLD_TEMPLATE);
            if (result != null && result.getCode() == ResultEnum.SUCCESS.getCode()) {
                dataCheckPO.setRowsValue(tableRowCount);
                int i = dataCheckMapper.updateById(dataCheckPO);
                if (i <= 0) {
                    return ResultEntityBuild.buildData(ResultEnum.ERROR, "表行数波动阈值模板规则已消费，更新配置表记录的表行数失败");
                }
            }
        } catch (Exception ex) {
            log.error("buildTableRowThresholdRule消费时触发异常，请求参数:", String.format("模板类型：表行数波动阈值模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE);
        } catch (Exception ex) {
            log.error("buildEmptyTableCheckRule消费时触发异常，请求参数:", String.format("模板类型：空表校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE);
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
                        result.setMsg("表更新校验模板规则已消费，更新配置表的更新校验规则失败");
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("buildUpdateTableRule消费时触发异常，请求参数:", String.format("模板类型：表更新校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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
            boolean isExist = true;
            // 检查结果无异常，返回操作结果
            if (isExist) {
                result.setCode(ResultEnum.SUCCESS.getCode());
                result.setMsg("表血缘断裂校验模板规则已消费，校验通过");
                return result;
            }
            // 检查结果有异常，发送通知
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE);
        } catch (Exception ex) {
            log.error("buildTableBloodKinshipRule消费时触发异常，请求参数:", String.format("模板类型：表血缘断裂校验模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
            // 第二步：查询配置的强类型校验规则
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

            // 发送校验附件保存，发送校验报告（含附件）
            result = sendNotice(dataCheckPO.getTemplateId(), dataCheckPO.getId(), dataCheckPO.getCreateUser(), TemplateTypeEnum.BUSINESS_CHECK_TEMPLATE);
        } catch (Exception ex) {
            log.error("buildBusinessCheckRule消费时触发异常，请求参数:", String.format("模板类型：业务验证模板，id：%s，templateModulesType:%s",
                    requestDTO.getId(), requestDTO.getTemplateModulesType()));
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
        return null;
    }

    @Override
    public ResultEntity<Object> buildSpecifyTimeRecyclingRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableRecoveryRule(DataQualityRequestDTO requestDTO) {
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
     */
    public ResultEntity<Object> sendNotice(int templateId, long moduleId, String userId,
                                           TemplateTypeEnum templateTypeEnum) {
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
            for (NoticePO emailNoticePO : emailNoticePOS) {
                NoticeDTO noticeDTO = new NoticeDTO();
                noticeDTO.emailServerId = emailNoticePO.getEmailServerId();
                noticeDTO.emailSubject = emailNoticePO.getEmailSubject();
                noticeDTO.body = emailNoticePO.getBody();
                noticeDTO.emailConsignee = emailNoticePO.getEmailConsignee();
                noticeDTO.emailCc = emailNoticePO.getEmailCc();
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
        try {
            JSONArray array = new JSONArray();
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            Statement st = null;
            Connection conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
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
        try {
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            Statement st = null;
            Connection conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
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
        try {
            // 数据源类型
            DataSourceTypeEnum sourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
            // 数据库连接对象
            Statement st = null;
            Connection conn = dataSourceConManageImpl.getStatement(sourceTypeEnum.getDriverName(), dataSourceConPO.getConStr(),
                    dataSourceConPO.getConAccount(), dataSourceConPO.getConPassword());
            st = conn.createStatement();
            assert st != null;
            /*
              boolean execute(String sql)
              允许执行查询语句、更新语句、DDL语句。
              返回值为true时，表示执行的是查询语句，可以通过getResultSet方法获取结果；
              返回值为false时，执行的是更新语句或DDL语句，getUpdateCount方法获取更新的记录数量。

              int executeUpdate(String sql)
              执行给定 SQL 语句，该语句可能为 INSERT、UPDATE 或 DELETE 语句，或者不返回任何内容的 SQL 语句（如 SQL DDL 语句）。
              返回值是更新的记录数量
             */
            affectedCount = st.executeUpdate(sql);
        } catch (Exception ex) {
            log.error("executeSql触发异常，请求参数:", String.format("模板类型：%s，数据源id:%s，数据源类型：%s，连接字符串：%s，sql：%s",
                    templateTypeEnum.getName(), dataSourceConPO.getId(), dataSourceConPO.getConType(), dataSourceConPO.getConStr(), sql));
            log.error("executeSql触发异常，详细报错:", ex);
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, "【" + templateTypeEnum.getName() + "】:" + ex);
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
