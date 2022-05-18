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
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：根据配置的校验条件生成校验规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.createRule = role.data;
        //第三步：转换DTO对象为PO对象
        DataCheckPO dataCheckPO = DataCheckMap.INSTANCES.dtoToPo(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        dataCheckPO.setCreateTime(LocalDateTime.now());
        dataCheckPO.setCreateUser(String.valueOf(loginUserInfo.getId()));
        int i = baseMapper.insertOne(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第五步：保存数据校验扩展属性
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
        //第二步：根据配置的校验条件生成校验规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.values()[role.code];
        }
        dto.createRule = role.data;
        //第三步：转换DTO对象为PO对象
        dataCheckPO = DataCheckMap.INSTANCES.dtoToPo_Edit(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验信息
        int i = baseMapper.updateById(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第五步：保存数据校验扩展属性
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
    public ResultEntity<List<DataCheckResultVO>> interfaceCheckData(DataCheckWebDTO dto) {
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
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
                List<DataCheckResultVO> dataCheckResult = null;
                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 字段规则模板
                        dataCheckResult = CheckFieldRule_Web(dataSourceInfo, dataCheckPO, dataCheckExtendFilters.get(0), data);
                        break;
                }
                if (CollectionUtils.isNotEmpty(dataCheckResult)) {
                    dataCheckResult.forEach(t -> {
                        dataCheckResults.add(t);
                    });
                }
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResults);
    }

    @Override
    public ResultEntity<List<DataCheckResultVO>> syncCheckData(DataCheckSyncDTO dto) {
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
        try {
            // 第一步：查询数据源信息
            DataSourceConPO dataSourceInfo = dataSourceConManageImpl.getDataSourceInfo(dto.getIp(), dto.getDbName());
            if (dataSourceInfo == null) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, dataCheckResults);
            }
            // 第二步：查询数据校验模块下同步校验场景下的模板
            QueryWrapper<TemplatePO> templatePOQueryWrapper = new QueryWrapper<>();
            templatePOQueryWrapper.lambda()
                    .eq(TemplatePO::getModuleType, ModuleTypeEnum.DATACHECK_MODULE.getValue())
                    .eq(TemplatePO::getTemplateScene, TemplateSceneEnum.DATACHECK_SYNCCHECK.getValue())
                    .eq(TemplatePO::getDelFlag, 1);
            List<TemplatePO> templatePOList = templateMapper.selectList(templatePOQueryWrapper);
            if (CollectionUtils.isEmpty(templatePOList)) {
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, dataCheckResults);
            }
            // 第三步：查询配置的表规则信息
            List<Long> templateIds = templatePOList.stream().map(TemplatePO::getId).collect(Collectors.toList());
            QueryWrapper<DataCheckPO> dataCheckPOQueryWrapper = new QueryWrapper<>();
            dataCheckPOQueryWrapper.lambda()
                    .eq(DataCheckPO::getDatasourceId, dataSourceInfo.getId())
                    .eq(DataCheckPO::getDelFlag, 1)
                    .in(DataCheckPO::getUseTableName, dto.tables)
                    .in(DataCheckPO::getTemplateId, templateIds);
            List<DataCheckPO> dataCheckPOList = baseMapper.selectList(dataCheckPOQueryWrapper);
            if (CollectionUtils.isEmpty(dataCheckPOList)) {
                return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResults);
            }
            // 第四步：循环规则，验证stg数据是否合规
            DataSourceTypeEnum dataSourceType = DataSourceTypeEnum.getEnum(dataSourceInfo.getConType());
            Connection connection = dataSourceConManageImpl.getStatement(dataSourceType.getDriverName(),
                    dataSourceInfo.getConStr(), dataSourceInfo.getConAccount(), dataSourceInfo.getConPassword());
            for (DataCheckPO dataCheckPO : dataCheckPOList) {
                TemplatePO templatePO = templatePOList.stream().filter(item -> item.getId() == dataCheckPO.getTemplateId()).findFirst().orElse(null);
                TemplateTypeEnum templateType = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
                List<DataCheckResultVO> dataCheckResult = null;
                switch (templateType) {
                    case FIELD_RULE_TEMPLATE:
                        // 字段规则模板
                        dataCheckResult = CheckFieldRule_Sync(connection, dataCheckPO);
                        break;
                }
                if (CollectionUtils.isNotEmpty(dataCheckResult)) {
                    dataCheckResult.forEach(t -> {
                        dataCheckResults.add(t);
                    });
                }
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR, ex);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataCheckResults);
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>
     * @description 页面校验--字段规则验证
     * @author dick
     * @date 2022/5/18 15:36
     * @version v1.0
     * @params dataSourceInfo
     * @params dataCheckPO
     * @params dataCheckExtendPO
     * @params data
     */
    public List<DataCheckResultVO> CheckFieldRule_Web(DataSourceConPO dataSourceInfo, DataCheckPO dataCheckPO, DataCheckExtendPO dataCheckExtendPO, JSONArray data) {
        List<DataCheckResultVO> dataCheckResult = new ArrayList<>();
        String[] split = dataCheckExtendPO.checkType.split(",");
        if (split == null || split.length == 0) {
            return dataCheckResult;
        }
        String fieldName = dataCheckExtendPO.fieldName;
        List<String> fieldValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            fieldValues.add(jsonObject.getString(fieldName));
        }
        for (String x : split) {
            CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(x));
            List<String> fieldValueFilters = null;
            DataCheckResultVO dataCheckResultVO = new DataCheckResultVO();
            dataCheckResultVO.setRuleId(Math.toIntExact(dataCheckPO.getId()));
            dataCheckResultVO.setRuleName(dataCheckPO.getRuleName());
            dataCheckResultVO.setCheckRule(CheckRuleEnum.getEnum(dataCheckPO.getCheckRule()));
            dataCheckResultVO.setCheckDataBase(dataSourceInfo.getConDbname());
            dataCheckResultVO.setCheckTable(dataCheckPO.getUseTableName());
            dataCheckResultVO.setCheckField(fieldName);
            dataCheckResultVO.setCheckDesc(TemplateTypeEnum.FIELD_RULE_TEMPLATE.getName());
            switch (checkTypeEnum) {
                case LENGTH_CHECK:
                    dataCheckResultVO.setCheckType(CheckTypeEnum.LENGTH_CHECK.getName());
                    int fieldLength = dataCheckExtendPO.fieldLength;
                    if (fieldLength == 0) {
                        continue;
                    }
                    fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isNotEmpty(iter) && iter.length() > fieldLength).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                        dataCheckResultVO.setCheckResult("fail");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，长度校验未通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    } else {
                        dataCheckResultVO.setCheckResult("success");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，长度校验已通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    }
                    dataCheckResult.add(dataCheckResultVO);
                    break;
                case NONEMPTY_CHECK:
                    dataCheckResultVO.setCheckType(CheckTypeEnum.NONEMPTY_CHECK.getName());
                    fieldValueFilters = fieldValues.stream().filter(iter -> StringUtils.isEmpty(iter)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(fieldValueFilters)) {
                        dataCheckResultVO.setCheckResult("fail");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，非空校验未通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    } else {
                        dataCheckResultVO.setCheckResult("success");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，非空校验已通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    }
                    dataCheckResult.add(dataCheckResultVO);
                    break;
                case UNIQUE_CHECK:
                    fieldValueFilters = fieldValues.stream().distinct().collect(Collectors.toList());
                    if (fieldValueFilters.size() != data.size()) {
                        dataCheckResultVO.setCheckResult("fail");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，唯一校验未通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    } else {
                        dataCheckResultVO.setCheckResult("success");
                        dataCheckResultVO.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，唯一校验已通过",
                                dataCheckPO.getUseTableName(), fieldName));
                    }
                    dataCheckResult.add(dataCheckResultVO);
                    break;
            }
        }
        return dataCheckResult;
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO>
     * @description 同步校验--字段规则验证
     * @author dick
     * @date 2022/5/18 15:37
     * @version v1.0
     * @params conn
     * @params dataCheckPO
     */
    public List<DataCheckResultVO> CheckFieldRule_Sync(Connection conn, DataCheckPO dataCheckPO) {
        List<DataCheckResultVO> dataCheckResults = new ArrayList<>();
        Statement st = null;
        try {
            JSONArray array = new JSONArray();
            // JDBC 读取大量数据时的 ResultSet resultSetType 设置TYPE_FORWARD_ONLY
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(dataCheckPO.createRule);
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
                dataCheckResults = array.toJavaList(DataCheckResultVO.class);
                dataCheckResults.forEach(t -> {
                    t.setRuleId(Math.toIntExact(dataCheckPO.getId()));
                    t.setRuleName(dataCheckPO.getRuleName());
                    t.setCheckRule(CheckRuleEnum.getEnum(dataCheckPO.getCheckRule()));
                    if (t.getCheckResult().toString().equals("fail")) {
                        t.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s未通过",
                                t.getCheckTable(), t.getCheckField(), t.getCheckType()));
                    } else {
                        t.setCheckResultMsg(String.format("表名：【%s】，字段名：【%s】，%s已通过",
                                t.getCheckTable(), t.getCheckField(), t.getCheckType()));
                    }
                });
            }
        } catch (Exception ex) {
            throw new FkException(ResultEnum.DATA_QUALITY_CREATESTATEMENT_ERROR, ex);
        }
        return dataCheckResults;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 根据校验条件生成校验规则
     * @author dick
     * @date 2022/4/2 15:51
     * @version v1.0
     * @params dto
     * @params templateTypeEnum
     */
    public ResultEntity<String> createRole(DataCheckDTO dto, TemplateTypeEnum templateTypeEnum) {
        if (dto == null || CollectionUtils.isEmpty(dto.dataCheckExtends)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
        }
        DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getById(dto.datasourceId);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, "");
        }
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];

        ResultEntity<String> rule = null;
        switch (templateTypeEnum) {
            case FIELD_RULE_TEMPLATE:
                //强类型模板规则
                rule = createField_StrongRule(dataSourceConPO.conDbname, dataSourceTypeEnum,
                        dto.tableName, dto.dataCheckExtends);
                break;
            case FIELD_AGGREGATE_TEMPLATE:
                //字段聚合波动阈值模板
                //rule = createField_AggregateRule(dataSourceConPO.conDbname, tableName, fieldName, dataCheckExtendDTO.fieldAggregate, dto.thresholdValue);
                break;
            case TABLECOUNT_TEMPLATE:
                //表行数波动阈值模板
                //rule = createTableRow_ThresholdRule(dataSourceConPO.conDbname, tableName);
                break;
            case EMPTY_TABLE_CHECK_TEMPLATE:
                //空表校验模板
                //rule = createEmptyTable_CheckRule(dataSourceConPO.conDbname, dataSourceTypeEnum, tableName);
                break;
            case UPDATE_TABLE_CHECK_TEMPLATE:
                //表更新校验模板
                //rule = createUpdateTable_CheckRule(dataSourceConPO.conDbname, dataSourceTypeEnum, tableName, fieldName);
                break;
            case SIMILARITY_TEMPLATE:
                //相似度模板
                //rule = createSimilarity_Rule(tableName, dto.dataCheckExtends);
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, "");
        }
        return rule;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成数据校验强规则
     * @author dick
     * @date 2022/3/25 13:59
     * @version v1.0
     * @params dataBase 数据库名称
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     * @params dataCheckExtends 数据校验扩展属性
     * 1、唯一校验
     * 2、非空校验
     * 3、长度校验
     */
    public ResultEntity<String> createField_StrongRule(String dataBase, DataSourceTypeEnum dataSourceTypeEnum,
                                                       String tableName, List<DataCheckExtendDTO> dataCheckExtends) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isEmpty(dataBase) || StringUtils.isEmpty(tableName) ||
                CollectionUtils.isEmpty(dataCheckExtends)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, stringBuilder.toString());
        }
        DataCheckExtendDTO dataCheckExtend = dataCheckExtends.get(0);
        String fieldName = dataCheckExtend.fieldName;
        String fieldLengthFunParm = null;
        if (StringUtils.isNotEmpty(fieldName)) {
            if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                fieldName = "`" + fieldName + "`";
                fieldLengthFunParm = "CHARACTER_LENGTH";
            } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                fieldName = "[" + fieldName + "]";
                fieldLengthFunParm = "LEN";
            } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRE) {
                fieldName = String.format("\"%s\"", fieldName);
                fieldLengthFunParm = "CHARACTER_LENGTH";
            }
        }
        String[] split = dataCheckExtend.checkType.split(",");
        for (String x : split) {
            CheckTypeEnum checkTypeEnum = CheckTypeEnum.getEnum(Integer.parseInt(x));

            switch (checkTypeEnum) {
                case UNIQUE_CHECK:
                    stringBuilder.append(String.format("SELECT\n" +
                                    "\t* \n" +
                                    "FROM\n" +
                                    "\t(\n" +
                                    "\tSELECT\n" +
                                    "\t\t'%s' AS checkDataBase,\n" +
                                    "\t\t'%s' AS checkTable,\n" +
                                    "\t\t'%s' AS checkField,\n" +
                                    "\t\t'%s' AS checkType,\n" +
                                    "\t\t'%s' AS checkDesc,\n" +
                                    "\t(CASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s GROUP BY %s HAVING count( %s )> 1 ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND) AS checkResult \n" +
                                    "\t) T1 ",
                            dataBase, tableName, dataCheckExtend.fieldName,
                            CheckTypeEnum.UNIQUE_CHECK.getName(),
                            TemplateTypeEnum.FIELD_RULE_TEMPLATE.getName(),
                            fieldName, tableName, fieldName, fieldName));
                    break;
                case NONEMPTY_CHECK:
                    if (stringBuilder.toString() != null && stringBuilder.toString().length() > 0) {
                        stringBuilder.append(" UNION ALL ");
                    }
                    stringBuilder.append(String.format("SELECT\n" +
                                    "\t* \n" +
                                    "FROM\n" +
                                    "\t(\n" +
                                    "\tSELECT\n" +
                                    "\t\t'%s' AS checkDataBase,\n" +
                                    "\t\t'%s' AS checkTable,\n" +
                                    "\t\t'%s' AS checkField,\n" +
                                    "\t\t'%s' AS checkType,\n" +
                                    "\t\t'%s' AS checkDesc,\n" +
                                    "\t(CASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s WHERE %s IS NULL OR %s = '' ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND) AS checkResult \n" +
                                    "\t) T2",
                            dataBase, tableName, dataCheckExtend.fieldName,
                            CheckTypeEnum.NONEMPTY_CHECK.getName(),
                            TemplateTypeEnum.FIELD_RULE_TEMPLATE.getName(),
                            fieldName, tableName, fieldName, fieldName));
                    break;
                case LENGTH_CHECK:
                    if (stringBuilder.toString() != null && stringBuilder.toString().length() > 0) {
                        stringBuilder.append(" UNION ALL ");
                    }
                    stringBuilder.append(String.format("SELECT\n" +
                                    "\t* \n" +
                                    "FROM\n" +
                                    "\t(\n" +
                                    "\tSELECT\n" +
                                    "\t\t'%s' AS checkDataBase,\n" +
                                    "\t\t'%s' AS checkTable,\n" +
                                    "\t\t'%s' AS checkField,\n" +
                                    "\t\t'%s' AS checkType,\n" +
                                    "\t\t'%s' AS checkDesc,\n" +
                                    "\t(CASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s WHERE %s( %s )> %s ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND) AS checkResult \n" +
                                    "\t) T3\n",
                            dataBase, tableName, dataCheckExtend.fieldName,
                            CheckTypeEnum.LENGTH_CHECK.getName(),
                            TemplateTypeEnum.FIELD_RULE_TEMPLATE.getName(),
                            fieldName, tableName, fieldLengthFunParm, fieldName, dataCheckExtend.fieldLength));
                    break;
                default:
                    return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, stringBuilder.toString());
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成字段聚合波动阈值规则
     * @author dick
     * @date 2022/3/25 13:59
     * @version v1.0
     * @params tableName 表名称
     * @params fieldName 字段名称
     * @params fieldAggregate 字段聚合函数
     * @params thresholdValue 波动阈值
     */
    public ResultEntity<String> createField_AggregateRule(String dataBase, String tableName, String fieldName,
                                                          String fieldAggregate, Integer thresholdValue) {
        if (StringUtils.isEmpty(tableName) ||
                StringUtils.isEmpty(fieldName) ||
                StringUtils.isEmpty(fieldAggregate) ||
                thresholdValue == null) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = "SELECT\n" +
                "\t'%s' AS checkDataBase,\n" +
                "\t'%s' AS checkTable,\n" +
                "\t'%s' AS checkField,\n" +
                "\t'%s' AS checkDesc,\n" +
                "\t'%s' AS checkType,\n" +
                "CASE\n" +
                "\t\t\n" +
                "\t\tWHEN %s >= %s THEN\n" +
                "\t\t'fail' ELSE 'success' \n" +
                "\tEND AS checkResult \n" +
                "FROM\n" +
                "\t'%s';";
        switch (fieldAggregate) {
            case "SUM":
                sql = String.format(sql, dataBase, tableName, fieldName, TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "SUM", "SUM(" + fieldName + ")", thresholdValue, tableName);
                break;
            case "COUNT":
                sql = String.format(sql, dataBase, tableName, fieldName, TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "COUNT", "COUNT(" + fieldName + ")", thresholdValue, tableName);
                break;
            case "AVG":
                sql = String.format(sql, dataBase, tableName, fieldName, TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "AVG", "AVG(CAST(" + fieldName + " AS decimal(10, 2)))", thresholdValue, tableName);
                break;
            case "MAX":
                sql = String.format(sql, dataBase, tableName, fieldName, TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "MAX", "MAX(" + fieldName + ")", thresholdValue, tableName);
                break;
            case "MIN":
                sql = String.format(sql, dataBase, tableName, fieldName, TemplateTypeEnum.FIELD_AGGREGATE_TEMPLATE.getName(),
                        "MIN", "MIN(" + fieldName + ")", thresholdValue, tableName);
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, sql);
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 表行数波动阈值模板
     * @author dick
     * @date 2022/4/2 15:47
     * @version v1.0
     * @params tableName 表名称
     */
    public ResultEntity<String> createTableRow_ThresholdRule(String dataBase, String tableName) {
        /*
         * 逻辑：
         * 通过调度任务实时查询处理
         * 1、实时查询的的表行数 减去 记录的表行数 大于 波动阀值，发送邮件；
         * 2、更新配置表记录的表行数，赋值为实时查询的的表行数
         * */
        if (StringUtils.isEmpty(dataBase) || StringUtils.isEmpty(tableName)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = String.format("SELECT\n" +
                "\t'%s' AS checkDataBase,\n" +
                "\t'%s' AS checkTable,\n" +
                "\t'%s' AS checkDesc,\n" +
                "\t'%s' AS checkType,\n" +
                "\tCOUNT( * ) AS checkResult\n" +
                "FROM\n" +
                "\t%s;", dataBase, tableName, TemplateTypeEnum.TABLECOUNT_TEMPLATE.getName(), "COUNT", tableName);
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        result.setData(sql);
        return result;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 空表校验模板
     * @author dick
     * @date 2022/4/2 15:46
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     */
    public ResultEntity<String> createEmptyTable_CheckRule(String dataBase,
                                                           DataSourceTypeEnum dataSourceTypeEnum, String tableName) {
        if (StringUtils.isEmpty(dataBase) || StringUtils.isEmpty(tableName)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\t'%s' AS 'checkDataBase',\n" +
                "\t\t'%s' AS 'checkTable',\n" +
                "\t\t'%s' AS 'checkDesc',\n" +
                "\tCASE\n" +
                "\t\t\t\n" +
                "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s * FROM %s %s) temp )= 0 THEN\n" +
                "\t\t\t'fail' ELSE 'success' \n" +
                "\t\tEND AS 'checkResult' \n" +
                "\t) T1";
        switch (dataSourceTypeEnum) {
            case MYSQL:
                sql = String.format(sql, dataBase, tableName, TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE.getName(), "", tableName, "LIMIT 1 ");
                break;
            case SQLSERVER:
                sql = String.format(sql, dataBase, tableName, TemplateTypeEnum.EMPTY_TABLE_CHECK_TEMPLATE.getName(), "TOP 1 ", tableName, "");
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, sql);
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 表更新校验模板
     * @author dick
     * @date 2022/4/2 15:46
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     * @params fieldName 更新依据字段
     */
    public ResultEntity<String> createUpdateTable_CheckRule(String dataBase, DataSourceTypeEnum dataSourceTypeEnum,
                                                            String tableName, String fieldName) {
        if (StringUtils.isEmpty(dataBase) || StringUtils.isEmpty(tableName)
                || StringUtils.isEmpty(fieldName)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        /*
         * 逻辑
         * 任务调度
         * 存在更新的数据，发送邮件后。重新生成SQL检查规则并保存到配置表，因为要更新参照时间
         * */
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\t'%s' AS 'checkDataBase',\n" +
                "\t\t'%s' AS 'checkTable',\n" +
                "\t\t'%s' AS 'checkDesc',\n" +
                "\tCASE\n" +
                "\t\t\t\n" +
                "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s * FROM %s WHERE %s>='%s' %s ) temp )= 0 THEN\n" +
                "\t\t\t'fail' ELSE 'success' \n" +
                "\t\tEND AS 'checkResult' \n" +
                "\t) T1\n" +
                "\t";
        String format = DateTimeUtils.getNowToShortDate();
        switch (dataSourceTypeEnum) {
            case MYSQL:
                sql = String.format(sql, dataBase, tableName, TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE.getName(), "", tableName, fieldName, format, "LIMIT 1 ");
                break;
            case SQLSERVER:
                sql = String.format(sql, dataBase, tableName, TemplateTypeEnum.UPDATE_TABLE_CHECK_TEMPLATE.getName(), "TOP 1 ", tableName, fieldName, format, "");
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, sql);
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成相似度规则
     * @author dick
     * @date 2022/4/2 18:44
     * @version v1.0
     * @params
     */
    public ResultEntity<String> createSimilarity_Rule(String tableName, List<DataCheckExtendDTO> extendList) {
        if (StringUtils.isEmpty(tableName) || CollectionUtils.isEmpty(extendList)) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = null;
        sql = "SELECT";
        for (DataCheckExtendDTO extendDTO : extendList) {
            sql += String.format(" %s,", extendDTO.fieldName);
        }
        if (StringUtils.isNotEmpty(sql)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        sql += String.format(" FROM %s;", tableName);
        ResultEntity<String> result = new ResultEntity<>();
        result.setData(sql);
        result.setCode(0);
        return result;
    }
}