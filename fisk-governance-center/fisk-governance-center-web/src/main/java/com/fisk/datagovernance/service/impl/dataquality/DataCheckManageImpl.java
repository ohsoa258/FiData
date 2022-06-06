package com.fisk.datagovernance.service.impl.dataquality;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckExtendMap;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckTypeV0;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.SyncCheckInfoVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验实现类
 * @date 2022/3/23 12:56
 */
@Service
public class DataCheckManageImpl extends ServiceImpl<DataCheckMapper, DataCheckPO> implements IDataCheckManageService {

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private DataCheckExtendMapper dataCheckExtendMapper;

    @Resource
    private DataCheckExtendManageImpl dataCheckExtendManageImpl;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<DataCheckVO> getAll(DataCheckQueryDTO query) {
        Page<DataCheckVO> all = baseMapper.getAll(query.page, query.datasourceId, query.tableName, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> ruleIds = all.getRecords().stream().map(DataCheckVO::getId).collect(Collectors.toList());

            // 数据校验规则扩展属性
            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1)
                    .in(DataCheckExtendPO::getRuleId, ruleIds);
            List<DataCheckExtendPO> dataCheckExtends = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
            if (CollectionUtils.isNotEmpty(dataCheckExtends)) {
                all.getRecords().forEach(e -> {
                    List<DataCheckExtendPO> dataCheckExtendFilters = dataCheckExtends.stream().filter(item -> item.getRuleId() == e.getId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(dataCheckExtendFilters)) {
                        e.setDataCheckExtends(DataCheckExtendMap.INSTANCES.poToVo(dataCheckExtendFilters));
                    }
                });
            }
        }
        return all;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataCheckDTO dto) {
        //第一步：验证模板是否存在以及表规则是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：转换DTO对象为PO对象
        DataCheckPO dataCheckPO = DataCheckMap.INSTANCES.dtoToPo(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
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
        if (CollectionUtils.isNotEmpty(dto.dataCheckExtends)) {
            List<DataCheckExtendPO> dataCheckExtends = DataCheckExtendMap.INSTANCES.dtoToPo(dto.dataCheckExtends);
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
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        DataCheckPO dataCheckPO = baseMapper.selectById(dto.id);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        //第二步：转换DTO对象为PO对象
        dataCheckPO = DataCheckMap.INSTANCES.dtoToPo_Edit(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据校验信息
        int i = baseMapper.updateById(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验扩展属性
        if (CollectionUtils.isNotEmpty(dto.dataCheckExtends)) {
            dataCheckExtendMapper.updateByRuleId(dto.id);
            List<DataCheckExtendPO> dataCheckExtends = DataCheckExtendMap.INSTANCES.dtoToPo(dto.dataCheckExtends);
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
    public List<DataCheckTypeV0> getDataCheckTypeList() {
        List<DataCheckTypeV0> dataCheckTypeV0s = new ArrayList<>();
        for (CheckTypeEnum checkTypeEnum : CheckTypeEnum.values()) {
            if (checkTypeEnum == CheckTypeEnum.NONE)
                continue;
            DataCheckTypeV0 dataCheckTypeV0 = new DataCheckTypeV0();
            dataCheckTypeV0.setText(checkTypeEnum.getName());
            dataCheckTypeV0.setValue(checkTypeEnum.getValue());
            dataCheckTypeV0.setGrade(1);
            dataCheckTypeV0s.add(dataCheckTypeV0);
        }
        for (DataCheckTypeEnum dataCheckTypeEnum : DataCheckTypeEnum.values()) {
            if (dataCheckTypeEnum == DataCheckTypeEnum.NONE)
                continue;
            DataCheckTypeV0 dataCheckTypeV0 = new DataCheckTypeV0();
            dataCheckTypeV0.setText(dataCheckTypeEnum.getName());
            dataCheckTypeV0.setValue(dataCheckTypeEnum.getValue());
            dataCheckTypeV0.setParentId(dataCheckTypeEnum.getParentId());
            dataCheckTypeV0.setGrade(2);
            dataCheckTypeV0s.add(dataCheckTypeV0);
        }
        return dataCheckTypeV0s;
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(DataCheckWebDTO dto) {
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
        ResultEnum resultEnum = ResultEnum.SUCCESS;
        try {
            // 第一步：查询数据源信息
            DataSourceConPO dataSourceInfo = dataSourceConManageImpl.getDataSourceInfo(dto.getIp(), dto.getDbName());
            if (dataSourceInfo == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, dataCheckResults);
            }
            // 第二步：查询数据校验模块下页面校验场景下的模板
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda()
                    .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATACHECK_MODULE.getValue())
                    .eq(TemplatePO::getTemplateScene, TemplateSceneEnum.DATACHECK_WEBCHECK.getValue())
                    .eq(TemplatePO::getDelFlag, 1);
            List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
            if (CollectionUtils.isEmpty(templatePOList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, dataCheckResults);
            }
            // 第三步：查询配置的表规则信息
            Set<String> tableNames = dto.body.keySet();
            List<Long> templateIds = templatePOList.stream().map(TemplatePO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda()
                    .eq(DataCheckPO::getDatasourceId, dataSourceInfo.getId())
                    .eq(DataCheckPO::getDelFlag, 1)
                    .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .orderByAsc(DataCheckPO::getRuleSort)
                    .in(DataCheckPO::getUseTableName, tableNames)
                    .in(DataCheckPO::getTemplateId, templateIds);
            List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckPOList)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResults);
            }
            // 第四步：查询校验规则的扩展属性
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
                TemplatePO templatePO = templatePOList.stream().filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                List<DataCheckExtendPO> dataCheckExtendFilters = dataCheckExtends.stream().filter(item -> item.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(dataCheckExtendFilters)) {
                    continue;
                }
                // 第六步：解析需要验证的数据
                JSONArray data = dto.body.get(dataCheckPO.getUseTableName());
                if (CollectionUtils.isEmpty(data)) {
                    continue;
                }
                ResultEntity<List<DataCheckResultVO>> dataCheckResult = null;
                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 字段规则模板
                        dataCheckResult = CheckFieldRule_Interface(templatePO, dataSourceInfo, dataCheckPO, dataCheckExtendFilters.get(0), data);
                        break;
                }
                if (dataCheckResult != null) {
                    if (dataCheckResult.code != ResultEnum.SUCCESS.getCode()) {
                        DataCheckResultVO checkResultVO = new DataCheckResultVO();
                        checkResultVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                        checkResultVO.setRuleName(dataCheckPO.getRuleName());
                        checkResultVO.setCheckDataBase(dto.getDbName());
                        checkResultVO.setCheckTable(dataCheckPO.getUseTableName());
                        checkResultVO.setCheckField(dataCheckExtendFilters.get(0).fieldName);
                        checkResultVO.setCheckResult("fail");
                        checkResultVO.setCheckResultMsg(dataCheckResult.getMsg());
                        checkResultVO.setCheckDesc(templatePO.templateDesc);
                        checkResultVO.setCheckRule(dataCheckPO.getCheckRule());
                        dataCheckResults.add(checkResultVO);
                        resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                    } else {
                        for (DataCheckResultVO dataCheckResultVO : dataCheckResult.getData()) {
                            if (resultEnum == ResultEnum.SUCCESS && dataCheckResultVO.getCheckRule() == 1 &&
                                    dataCheckResultVO.getCheckResult().toString().equals("fail")) {
                                resultEnum = ResultEnum.DATA_QUALITY_DATACHECK_CHECK_NOPASS;
                            }
                            dataCheckResults.add(dataCheckResultVO);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(resultEnum, dataCheckResults);
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.util.List < com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>>
     * @description 页面校验--字段规则验证
     * @author dick
     * @date 2022/5/24 12:11
     * @version v1.0
     * @params dataSourceInfo 数据源信息
     * @params dataCheckPO 数据校验PO
     * @params dataCheckExtendPO  数据校验扩展属性PO
     * @params data 校验的数据
     */
    public ResultEntity<List<DataCheckResultVO>> CheckFieldRule_Interface(TemplatePO templatePO, DataSourceConPO dataSourceInfo, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        List<DataCheckResultVO> dataCheckResult = new ArrayList<>();
        String[] split = dataCheckExtendPO.checkType.split(",");
        if (split == null || split.length == 0) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATACHECK_RULE_ERROR, null);
        }
        String fieldName = dataCheckExtendPO.fieldName;
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            if (jsonObject.containsKey(fieldName)) {
                fieldValues.add(jsonObject.getString(fieldName));
            }
        }
        if (fieldValues.size() != data.size()) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATACHECK_REQUESTJSON_ERROR, null);
        }
        for (String x : split) {
            CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(x));
            List<String> fieldValueFilters = null;
            DataCheckResultVO dataCheckResultVO = new DataCheckResultVO();
            dataCheckResultVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            dataCheckResultVO.setRuleName(dataCheckPO.getRuleName());
            dataCheckResultVO.setCheckRule(dataCheckPO.getCheckRule());
            dataCheckResultVO.setCheckDataBase(dataSourceInfo.getConDbname());
            dataCheckResultVO.setCheckTable(dataCheckPO.getUseTableName());
            dataCheckResultVO.setCheckField(fieldName);
            dataCheckResultVO.setCheckDesc(templatePO.getTemplateDesc());
            boolean isValid = true;
            switch (checkTypeEnum) {
                case DATA_CHECK:
                    DataCheckResultVO checkResultVO = GetDataCheckResult_Interface(dataCheckPO, dataCheckExtendPO, fieldValues);
                    if (checkResultVO != null) {
                        dataCheckResultVO.setCheckType(checkResultVO.getCheckType());
                        dataCheckResultVO.setCheckResult(checkResultVO.getCheckResult());
                        dataCheckResultVO.setCheckResultMsg(checkResultVO.getCheckResultMsg());
                    }
                    break;
                case NONEMPTY_CHECK:
                    fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isEmpty(iter)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                        isValid = false;
                    }
                    break;
                case UNIQUE_CHECK:
                    fieldValueFilters = fieldValues.stream().distinct().collect(Collectors.toList());
                    if (fieldValueFilters.size() != data.size()) {
                        isValid = false;
                    }
                    break;
            }
            if (checkTypeEnum != CheckTypeEnum.DATA_CHECK) {
                dataCheckResultVO.setCheckType(checkTypeEnum.getName());
                if (!isValid) {
                    dataCheckResultVO.setCheckResult("fail");
                    dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过",
                            dataCheckPO.getUseTableName(), fieldName, checkTypeEnum.getName()));
                } else {
                    dataCheckResultVO.setCheckResult("success");
                    dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s已通过",
                            dataCheckPO.getUseTableName(), fieldName, checkTypeEnum.getName()));
                }
            }
            dataCheckResult.add(dataCheckResultVO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResult);
    }

    /**
     * @return com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO
     * @description 获取数据校验结果
     * @author dick
     * @date 2022/6/1 11:48
     * @version v1.0
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params fieldValues
     */
    public DataCheckResultVO GetDataCheckResult_Interface(DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, List<String> fieldValues) {
        DataCheckResultVO dataCheckResultVO = new DataCheckResultVO();
        String fieldWhere = dataCheckExtendPO.fieldWhere;
        DataCheckTypeEnum dataCheckTypeEnum = DataCheckTypeEnum.getEnum(dataCheckExtendPO.dataCheckType);
        boolean isValid = true;
        switch (dataCheckTypeEnum) {
            case TEXTLENGTH_CHECK:
                int fieldLength = Integer.parseInt(fieldWhere);
                List<String> fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isNotEmpty(iter) && iter.length() > fieldLength).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                    isValid = false;
                }
                break;
            case DATEFORMAT_CHECK:
                isValid = DateTimeUtils.isValidDate(fieldValues, fieldWhere);
                break;
            case SEQUENCERANGE_CHECK:
                String[] splitArr = fieldWhere.split(",");
                List<String> whereList = new ArrayList<>();
                Collections.addAll(whereList, splitArr);
                List<String> list = RegexUtils.subtractValid(fieldValues, whereList, true);
                isValid = CollectionUtils.isEmpty(list);
                break;
        }
        dataCheckResultVO.setCheckType(dataCheckTypeEnum.getName());
        if (!isValid) {
            dataCheckResultVO.setCheckResult("fail");
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过",
                    dataCheckPO.getUseTableName(), dataCheckExtendPO.getFieldName(), dataCheckTypeEnum.getName()));
        } else {
            dataCheckResultVO.setCheckResult("success");
            dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s已通过",
                    dataCheckPO.getUseTableName(), dataCheckExtendPO.getFieldName(), dataCheckTypeEnum.getName()));
        }
        return dataCheckResultVO;
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> syncCheckData(DataCheckSyncDTO dto) {
        ResultEntity<List<DataCheckResultVO>> result = new ResultEntity<>();
        try {
            if (CollectionUtils.isEmpty(dto.checkByFieldMap) &&
                    CollectionUtils.isEmpty(dto.updateFieldMap_Y) &&
                    CollectionUtils.isEmpty(dto.updateFieldMap_N) &&
                    CollectionUtils.isEmpty(dto.updateFieldMap_R) &&
                    StringUtils.isEmpty(dto.msgField)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_SYNCCHECK_NOOPERATION, null);
            }
            // 第一步：查询数据源信息
            DataSourceConPO dataSourceInfo = dataSourceConManageImpl.getDataSourceInfo(dto.getIp(), dto.getDbName());
            if (dataSourceInfo == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, null);
            }
            DataSourceTypeEnum dataSourceType = DataSourceTypeEnum.getEnum(dataSourceInfo.getConType());
            List<String> dtoPramsList = GetPrams_Sync(dto, dataSourceType);
            Connection connection = dataSourceConManageImpl.getStatement(dataSourceType.getDriverName(), dataSourceInfo.getConStr(), dataSourceInfo.getConAccount(), dataSourceInfo.getConPassword());
            // 第二步：查询数据校验模块下同步校验场景下的模板
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda()
                    .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATACHECK_MODULE.getValue())
                    .eq(TemplatePO::getTemplateScene, TemplateSceneEnum.DATACHECK_SYNCCHECK.getValue())
                    .eq(TemplatePO::getDelFlag, 1);
            List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
            List<Long> templateIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(templatePOList)) {
                templateIds = templatePOList.stream().map(TemplatePO::getId).collect(Collectors.toList());
            }
            // 第三步：查询配置的表规则信息
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda()
                    .eq(DataCheckPO::getDatasourceId, dataSourceInfo.getId())
                    .eq(DataCheckPO::getDelFlag, 1)
                    .eq(DataCheckPO::getUseTableName, dto.tableName)
                    .eq(DataCheckPO::getRuleState, RuleStateEnum.Enable.getValue())
                    .orderByAsc(DataCheckPO::getRuleSort)
                    .in(DataCheckPO::getTemplateId, templateIds);
            List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckPOList)) {
                // 未配置表校验规则，但请求参数中含成功后要修改的字段；根据参数字段修改表数据
                ResultEnum updateResult = ResultEnum.SUCCESS;
                if (CollectionUtils.isNotEmpty(dtoPramsList) && StringUtils.isNotEmpty(dtoPramsList.get(1))) {
                    String tableName = getSqlField(dataSourceType, dto.tableName);
                    updateResult = UpdateTableDataToSuccess_Sync(connection, dataSourceType, dtoPramsList, tableName);
                }
                return ResultEntityBuild.buildData(updateResult, null);
            }
            // 第四步：查询校验规则的扩展属性
            List<Long> ruleIds = dataCheckPOList.stream().map(DataCheckPO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckExtendPO> dataCheckExtendPOQueryWrapper = new QueryWrapper<>();
            dataCheckExtendPOQueryWrapper.lambda().eq(DataCheckExtendPO::getDelFlag, 1).in(DataCheckExtendPO::getRuleId, ruleIds);
            List<DataCheckExtendPO> dataCheckExtends = dataCheckExtendMapper.selectList(dataCheckExtendPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckExtends)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATACHECK_RULE_ERROR, null);
            }
            List<SyncCheckInfoVO> checkResultSqlList = new ArrayList<>();
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                TemplatePO templatePO = templatePOList.stream().filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                List<DataCheckExtendPO> dataCheckExtendFilters = dataCheckExtends.stream().filter(item -> item.getRuleId() == dataCheckPO.getId()).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(dataCheckExtendFilters)) {
                    continue;
                }
                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 获取待校验的结果集合
                        List<SyncCheckInfoVO> checkResultSqls = GetCheckFieldRule_Sync(templatePO, dataSourceInfo, dataSourceType, dataCheckPO, dataCheckExtendFilters);
                        checkResultSqlList.addAll(checkResultSqls);
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

    /**
     * @return java.util.List<java.lang.String>
     * @description 同步校验，请求参数组装
     * @author dick
     * @date 2022/6/2 14:48
     * @version v1.0
     * @params dto
     * @params dataSourceType
     */
    public List<String> GetPrams_Sync(DataCheckSyncDTO dto, DataSourceTypeEnum dataSourceType) {
        List<String> parms = new ArrayList<>();
        String checkFieldWhere = "",
                updateField_Y = "",
                updateField_N = "",
                updateField_R = "";
        // 校验/更新依据字段
        if (CollectionUtils.isNotEmpty(dto.checkByFieldMap)) {
            for (Map.Entry<String, Object> entry : dto.checkByFieldMap.entrySet()) {
                String sqlWhereStr = getSqlFieldWhere(dataSourceType);
                if (StringUtils.isEmpty(sqlWhereStr)) {
                    continue;
                }
                sqlWhereStr = String.format(sqlWhereStr, entry.getKey(), entry.getValue());
                checkFieldWhere += " AND " + sqlWhereStr;
            }
        }
        // 校验成功修改字段
        if (CollectionUtils.isNotEmpty(dto.updateFieldMap_Y)) {
            for (Map.Entry<String, Object> entry : dto.updateFieldMap_Y.entrySet()) {
                String sqlWhereStr = getSqlFieldWhere(dataSourceType);
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
        // 校验失败修改字段
        if (CollectionUtils.isNotEmpty(dto.updateFieldMap_N)) {
            for (Map.Entry<String, Object> entry : dto.updateFieldMap_N.entrySet()) {
                String sqlWhereStr = getSqlFieldWhere(dataSourceType);
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
        // 校验失败但校验规则为弱类型修改字段
        if (CollectionUtils.isNotEmpty(dto.updateFieldMap_R)) {
            for (Map.Entry<String, Object> entry : dto.updateFieldMap_R.entrySet()) {
                String sqlWhereStr = getSqlFieldWhere(dataSourceType);
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
        parms.add(checkFieldWhere);
        parms.add(updateField_Y);
        parms.add(updateField_N);
        parms.add(updateField_R);
        parms.add(dto.msgField);
        return parms;
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
                if (resultEnum == ResultEnum.SUCCESS && syncCheckInfo.getCheckRule() == CheckRuleEnum.STRONG_RULE &&
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
    public List<SyncCheckInfoVO> GetCheckFieldRule_Sync(TemplatePO templatePO, DataSourceConPO dataSourceConPO, DataSourceTypeEnum dataSourceTypeEnum, DataCheckPO dataCheckPO, List<DataCheckExtendPO> dataCheckExtendFilters) {

        List<SyncCheckInfoVO> checkResultSqls = new ArrayList<>();

        DataCheckExtendPO dataCheckExtend = dataCheckExtendFilters.get(0);
        String tableName = dataCheckPO.useTableName;
        String fieldName = dataCheckExtend.fieldName;

        if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            fieldName = "`" + fieldName + "`";
            tableName = "`" + tableName + "`";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            fieldName = "[" + fieldName + "]";
            tableName = "[" + tableName + "]";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRE) {
            fieldName = String.format("\"%s\"", fieldName);
            tableName = String.format("\"%s\"", tableName);
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
            checkResultSql.setCheckTable(tableName);
            checkResultSql.setCheckField(fieldName);
            checkResultSql.setTableName(dataCheckPO.useTableName);
            checkResultSql.setFieldName(dataCheckExtend.fieldName);
            checkResultSql.setCheckFieldWhere(dataCheckExtend.fieldWhere);
            checkResultSql.setCheckRule(CheckRuleEnum.getEnum(dataCheckPO.getCheckRule()));
            checkResultSql.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            checkResultSql.setRuleName(dataCheckPO.getRuleName());
            checkResultSql.setCheckDesc(templatePO.templateDesc);
            checkResultSql.setCheckDataBase(dataSourceConPO.conDbname);
            checkResultSqls.add(checkResultSql);
        }
        return checkResultSqls;
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
        String filedStr = SqlParmUtils.parseListToParmStr(fileds);
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
                        sqlInPram = SqlParmUtils.getInParm(fieldValueFilters);
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
                        sqlInPram = SqlParmUtils.getInParm(fieldValueFilters);
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
        List<SyncCheckInfoVO> strong_Rule = dataCheckResults_N.stream().filter(t -> (t.getCheckRule() == CheckRuleEnum.STRONG_RULE)).collect(Collectors.toList());
        List<SyncCheckInfoVO> weak_Rule = dataCheckResults_N.stream().filter(t -> (t.getCheckRule() == CheckRuleEnum.WEAK_RULE)).collect(Collectors.toList());
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
            if (t.checkRule == CheckRuleEnum.WEAK_RULE) {
                updateFieldSql = dtoPramsList.get(3);
            } else if (t.checkRule == CheckRuleEnum.STRONG_RULE) {
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
     * @description 拼接sql字段条件
     * @author dick
     * @date 2022/6/2 14:52
     * @version v1.0
     * @params dataSourceTypeEnum
     */
    public String getSqlFieldWhere(DataSourceTypeEnum dataSourceTypeEnum) {
        String sqlWhereStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" + "=" + "'%s'" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" + "=" + "'%s'" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRE
                                ? "\"%s\"" + "=" + "'%s'" :
                                "";
        return sqlWhereStr;
    }

    /**
     * @return java.lang.String
     * @description 拼接sql字段
     * @author dick
     * @date 2022/6/6 15:54
     * @version v1.0
     * @params dataSourceTypeEnum
     */
    public String getSqlField(DataSourceTypeEnum dataSourceTypeEnum, String fieldName) {
        String sqlFieldStr = dataSourceTypeEnum == DataSourceTypeEnum.MYSQL
                ? "`%s`" :
                dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER
                        ? "[%s]" :
                        dataSourceTypeEnum == DataSourceTypeEnum.POSTGRE
                                ? "\"%s\"" :
                                "";
        if (StringUtils.isNotEmpty(sqlFieldStr)) {
            sqlFieldStr = String.format(sqlFieldStr, fieldName);
        }
        return sqlFieldStr;
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

        } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRE) {
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