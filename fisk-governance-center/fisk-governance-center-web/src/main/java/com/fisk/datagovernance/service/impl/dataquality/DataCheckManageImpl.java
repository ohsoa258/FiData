package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckQueryDTO;
import com.fisk.datagovernance.entity.dataquality.*;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.map.dataquality.SimilarityExtendMap;
import com.fisk.datagovernance.mapper.dataquality.*;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import com.fisk.datagovernance.vo.dataquality.datacheck.SimilarityExtendVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private ComponentNotificationMapper componentNotificationMapper;

    @Resource
    private SimilarityExtendMapper similarityExtendMapper;

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    private SimilarityExtendManageImpl similarityExtendManageImpl;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<DataCheckVO> getAll(DataCheckQueryDTO query) {
        Page<DataCheckVO> all = baseMapper.getAll(query.page, query.tableName, query.keyword);

        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            // 查询告警通知
            List<Integer> collect = all.getRecords().stream().map(DataCheckVO::getId).collect(Collectors.toList());
            List<Integer> collect1 = all.getRecords().stream().map(DataCheckVO::getTemplateId).distinct().collect(Collectors.toList());
            QueryWrapper<ComponentNotificationPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1)
                    .in(ComponentNotificationPO::getTemplateId, collect1)
                    .in(ComponentNotificationPO::getModuleId, collect);
            List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(queryWrapper);
            // 查询相似度组件扩展属性
            QueryWrapper<SimilarityExtendPO> similarityExtendPOQueryWrapper = new QueryWrapper<>();
            similarityExtendPOQueryWrapper.lambda().eq(SimilarityExtendPO::getDelFlag, 1)
                    .in(SimilarityExtendPO::getDatacheckId, collect);
            List<SimilarityExtendPO> similarityExtendPOS = similarityExtendMapper.selectList(similarityExtendPOQueryWrapper);
            // 循环赋值
            all.getRecords().forEach(e -> {
                if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                    List<Integer> collect2 = componentNotificationPOS.stream()
                            .filter(item -> item.getTemplateId() == e.getTemplateId() && item.getModuleId() == e.getId())
                            .map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect2)) {
                        e.setNoticeIds(collect2);
                    }
                }
                if (CollectionUtils.isNotEmpty(similarityExtendPOS)) {
                    List<SimilarityExtendVO> similarityExtendVOS = SimilarityExtendMap.INSTANCES.poToVo(similarityExtendPOS);
                    e.setSimilarityExtendVOS(similarityExtendVOS);
                }
            });
        }
        return all;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataCheckDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第一步：根据配置的校验条件生成校验规则
        ResultEntity<String> role = createRole(dto, templatePO);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.moduleRule = role.data;
        //第二步：转换DTO对象为PO对象
        DataCheckPO dataCheckPO = DataCheckMap.INSTANCES.dtoToPo(dto);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据校验信息
        dataCheckPO.setCreateTime(LocalDateTime.now());
        dataCheckPO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(dataCheckPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(dataCheckPO.templateId, dataCheckPO.getId(), dto.componentNotificationDTOS, false);
        //第五步：保存相似度模板扩展属性
        if (templatePO.getTemplateType() == TemplateTypeEnum.SIMILARITY_TEMPLATE.getValue()) {
            similarityExtendManageImpl.saveBatchById(dto.similarityExtendDTOS, dataCheckPO.getId(), false);
        }
        //第六步：如果设置了调度任务条件，则生成调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(DataCheckEditDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        DataCheckPO dataCheckPO = baseMapper.selectById(dto.id);
        if (dataCheckPO == null) {
            return ResultEnum.SAVE_VERIFY_ERROR;
        }
        //第一步：根据配置的校验条件生成校验规则
        ResultEntity<String> role = createRole(dto, templatePO);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.values()[role.code];
        }
        dto.moduleRule = role.data;
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
        //第四步：保存数据组件通知信息，先将原来的组件通知关联关系置为无效
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(dataCheckPO.templateId, dataCheckPO.getId(), dto.componentNotificationDTOS, true);
        //第五步：保存相似度模板扩展属性
        if (templatePO.getTemplateType() == TemplateTypeEnum.SIMILARITY_TEMPLATE.getValue()) {
            similarityExtendManageImpl.saveBatchById(dto.similarityExtendDTOS, dataCheckPO.getId(), true);
        }
        //第六步：根据组件状态&Corn调度任务

        return resultEnum;
    }

    @Override
    public ResultEnum deleteData(int id) {
        DataCheckPO dataCheckPO = baseMapper.selectById(id);
        if (dataCheckPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return baseMapper.deleteByIdWithFill(dataCheckPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 根据校验条件生成校验规则
     * @author dick
     * @date 2022/4/2 15:51
     * @version v1.0
     * @params dto
     * @params templatePO
     */
    public ResultEntity<String> createRole(DataCheckDTO dto, TemplatePO templatePO) {
        //查询数据源
        DataSourceConPO dataSourceConPO = getDataSourceConPO(dto.datasourceId, dto.datasourceType);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, "");
        }
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        String tableName = dto.checkStep == CheckStepTypeEnum.TABLE_FRONT ? dto.proTableName : dto.tableName;
        String fieldName = dto.fieldName;
        if (dto.fieldName != null && !dto.fieldName.isEmpty()) {
            if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                fieldName = "`" + fieldName + "`";
            } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                fieldName = "[" + fieldName + "]";
            }
        }
        ResultEntity<String> rule = null;
        switch (templateTypeEnum) {
            case FIELD_STRONG_RULE_TEMPLATE:
                //强类型模板规则
                rule = createField_StrongRule(dataSourceTypeEnum, tableName, fieldName, dto.fieldLength, dto.checkRuleType);
                break;
            case FIELD_AGGREGATE_THRESHOLD_TEMPLATE:
                //字段聚合波动阈值模板
                rule = createField_AggregateRule(tableName, fieldName, dto.fieldAggregate, dto.thresholdValue);
                break;
            case ROWCOUNT_THRESHOLD_TEMPLATE:
                //表行数波动阈值模板
                rule = createTableRow_ThresholdRule(tableName);
                break;
            case EMPTY_TABLE_CHECK_TEMPLATE:
                //空表校验模板
                rule = createEmptyTable_CheckRule(dataSourceTypeEnum, tableName);
                break;
            case UPDATE_TABLE_CHECK_TEMPLATE:
                //表更新校验模板
                rule = createUpdateTable_CheckRule(dataSourceTypeEnum, tableName, fieldName);
                break;
            case TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE:
                //表血缘断裂校验模板
                rule = createTableBloodKinship_CheckRule(dto.datasourceId, dto.datasourceType, tableName, dto.checkConsanguinity);
                break;
            case BUSINESS_CHECK_TEMPLATE:
                rule = createBusiness_CheckRule();
                //业务验证模板
                break;
            case SIMILARITY_TEMPLATE:
                //相似度模板
                rule = createSimilarity_Rule();
                break;
        }
        return rule;
    }

    /**
     * @return com.fisk.datagovernance.entity.dataquality.DataSourceConPO
     * @description 查询数据源信息
     * @author dick
     * @date 2022/3/25 14:11
     * @version v1.0
     * @params id
     * @params dataSourceTypeEnum
     */
    public DataSourceConPO getDataSourceConPO(int id, ModuleDataSourceTypeEnum dataSourceTypeEnum) {
        DataSourceConPO dataSourceConPO = new DataSourceConPO();
        switch (dataSourceTypeEnum) {
            case DATAQUALITY:
                dataSourceConPO = dataSourceConMapper.selectById(id);
                break;
            case METADATA:
                // 调用元数据接口，获取数据源的基础信息
                break;
        }
        return dataSourceConPO;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成数据校验强规则
     * @author dick
     * @date 2022/3/25 13:59
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     * @params fieldName 字段名称
     * @params fieldLength 字段长度
     * @params checkRuleType 校验规则类型
     * 1、唯一校验
     * 2、非空校验
     * 3、长度校验
     */
    public ResultEntity<String> createField_StrongRule(DataSourceTypeEnum dataSourceTypeEnum, String tableName, String fieldName, int fieldLength, String checkRuleType) {
        StringBuilder stringBuilder = new StringBuilder();
        if (tableName == null || tableName.isEmpty() ||
                fieldName == null || fieldName.isEmpty() ||
                checkRuleType == null || checkRuleType.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, stringBuilder.toString());
        }
        String fieldNameParam = null;
        String fieldLengthFunParm = null;
        if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
            fieldNameParam = "`" + fieldName + "`";
            fieldLengthFunParm = "CHARACTER_LENGTH";
        } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
            fieldNameParam = "[" + fieldName + "]";
            fieldLengthFunParm = "LEN";
        }
        if (fieldNameParam == null || fieldNameParam.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, stringBuilder.toString());
        }
        String[] split = checkRuleType.split(",");
        for (String x : split) {
            CheckRuleTypeEnum checkRuleTypeEnum = CheckRuleTypeEnum.getEnum(Integer.parseInt(x));

            switch (checkRuleTypeEnum) {
                case UNIQUE_CHECK:
                    stringBuilder.append(String.format("SELECT\n" +
                                    "\t* \n" +
                                    "FROM\n" +
                                    "\t(\n" +
                                    "\tSELECT\n" +
                                    "\t\t'%s' AS 'checkTable',\n" +
                                    "\t\t'%s' AS 'checkField',\n" +
                                    "\t\t%s AS 'checkType',\n" +
                                    "\t\t'%s' AS 'checkDesc',\n" +
                                    "\tCASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s GROUP BY %s HAVING count( %s )> 1 ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND AS 'checkResult' \n" +
                                    "\t) T1 ",
                            tableName, fieldNameParam,
                            CheckRuleTypeEnum.UNIQUE_CHECK.getValue(),
                            CheckRuleTypeEnum.UNIQUE_CHECK.getName(),
                            fieldNameParam, tableName, fieldNameParam, fieldNameParam));
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
                                    "\t\t'%s' AS 'checkTable',\n" +
                                    "\t\t'%s' AS 'checkField',\n" +
                                    "\t\t%s AS 'checkType',\n" +
                                    "\t\t'%s' AS 'checkDesc',\n" +
                                    "\tCASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s WHERE %s IS NULL OR %s = '' ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND AS 'checkResult' \n" +
                                    "\t) T2", tableName, fieldNameParam,
                            CheckRuleTypeEnum.NONEMPTY_CHECK.getValue(),
                            CheckRuleTypeEnum.NONEMPTY_CHECK.getName(),
                            fieldNameParam, tableName, fieldNameParam, fieldNameParam));
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
                                    "\t\t'%s' AS 'checkTable',\n" +
                                    "\t\t'%s' AS 'checkField',\n" +
                                    "\t\t%s AS 'checkType',\n" +
                                    "\t\t'%s' AS 'checkDesc',\n" +
                                    "\tCASE\n" +
                                    "\t\t\t\n" +
                                    "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s FROM %s WHERE %s( %s )> %s ) temp )> 0 THEN\n" +
                                    "\t\t\t'fail' ELSE 'success' \n" +
                                    "\t\tEND AS 'checkResult' \n" +
                                    "\t) T3\n", tableName, fieldNameParam,
                            CheckRuleTypeEnum.LENGTH_CHECK.getValue(),
                            CheckRuleTypeEnum.LENGTH_CHECK.getName(),
                            fieldNameParam, tableName, fieldLengthFunParm, fieldNameParam, fieldLength));
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
    public ResultEntity<String> createField_AggregateRule(String tableName, String fieldName, String fieldAggregate, Integer thresholdValue) {
        if (tableName == null || tableName.isEmpty() ||
                fieldName == null || fieldName.isEmpty() ||
                fieldAggregate == null || fieldAggregate.isEmpty() ||
                thresholdValue == null) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = "SELECT %s FROM %s;";
        switch (fieldAggregate) {
            case "SUM":
                sql = String.format(sql, "SUM(" + fieldName + ")", tableName);
                break;
            case "COUNT":
                sql = String.format(sql, "COUNT(" + fieldName + ")", tableName);
                break;
            case "AVG":
                sql = String.format(sql, "AVG(CAST(" + fieldName + " AS decimal(10, 2))) AS " + fieldName + "", tableName);
                break;
            case "MAX":
                sql = String.format(sql, "MAX(" + fieldName + ")", tableName);
                break;
            case "MIN":
                sql = String.format(sql, "MIN(" + fieldName + ")", tableName);
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
    public ResultEntity<String> createTableRow_ThresholdRule(String tableName) {
        /*
         * 逻辑：
         * 通过调度任务实时查询处理
         * 1、实时查询的的表行数 减去 记录的表行数 大于或小于 波动阀值，发送邮件；
         * 2、更新配置表记录的表行数，赋值为实时查询的的表行数
         * */
        if (tableName == null || tableName.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
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
    public ResultEntity<String> createEmptyTable_CheckRule(DataSourceTypeEnum dataSourceTypeEnum, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\t'%s' AS 'checkTable',\n" +
                "\t\t'检查表是否存在数据，fail表示不存在，success表示存在' AS 'checkDesc',\n" +
                "\tCASE\n" +
                "\t\t\t\n" +
                "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s * FROM %s %s) temp )= 0 THEN\n" +
                "\t\t\t'fail' ELSE 'success' \n" +
                "\t\tEND AS 'checkResult' \n" +
                "\t) T1";
        switch (dataSourceTypeEnum) {
            case MYSQL:
                sql = String.format(sql, tableName, "", tableName, "LIMIT 1 ");
                break;
            case SQLSERVER:
                sql = String.format(sql, tableName, "TOP 1 ", tableName, "");
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
    public ResultEntity<String> createUpdateTable_CheckRule(DataSourceTypeEnum dataSourceTypeEnum, String tableName, String fieldName) {
        if (tableName == null || tableName.isEmpty()
                || fieldName == null || fieldName.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        /*
         * 逻辑
         * 任务调度
         * 存在更新的数据，发送邮件后。配置表重新生成SQL检查规则，因为要更新参照时间
         * */
        String sql = "SELECT\n" +
                "\t* \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\t'%s' AS 'checkTable',\n" +
                "\t\t'检查表数据是否更新，fail表示未更新，success表示有更新' AS 'checkDesc',\n" +
                "\tCASE\n" +
                "\t\t\t\n" +
                "\t\t\tWHEN ( SELECT COUNT(*) FROM ( SELECT %s * FROM %s WHERE %s>='%s' %s ) temp )= 0 THEN\n" +
                "\t\t\t'fail' ELSE 'success' \n" +
                "\t\tEND AS 'checkResult' \n" +
                "\t) T1\n" +
                "\t";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式 yyyy-MM-dd HH:mm:ss
        String format = df.format(new Date());// new Date()为获取当前系统时间
        switch (dataSourceTypeEnum) {
            case MYSQL:
                sql = String.format(sql, tableName, "", tableName, fieldName, format, "LIMIT 1 ");
                break;
            case SQLSERVER:
                sql = String.format(sql, tableName, "TOP 1 ", tableName, fieldName, format, "");
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, "");
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, sql);
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 表血缘断裂校验规则
     * @author dick
     * @date 2022/4/2 18:36
     * @version v1.0
     * @params datasourceId 数据源id
     * @params datasourceType 功能类型
     * @params tableName 表名称
     * @params checkConsanguinity  血缘检查范围：1、上游 2、下游 3、上下游
     */
    public ResultEntity<String> createTableBloodKinship_CheckRule(int datasourceId, ModuleDataSourceTypeEnum datasourceType, String tableName, Integer checkConsanguinity) {
        if (tableName == null || tableName.isEmpty()
                || checkConsanguinity == null || datasourceType != ModuleDataSourceTypeEnum.METADATA) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        /*
         * 逻辑：
         * 无规则。数据调度实时调用元数据接口检查表血缘是否存在
         * 目前只有在元数据中管理的表才能查询表的上下游血缘，自由数据源从存储过程同步记录表记录血缘关系暂时未做
         * */
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        return result;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成业务校验规则
     * @author dick
     * @date 2022/4/2 18:40
     * @version v1.0
     * @params
     */
    public ResultEntity<String> createBusiness_CheckRule() {
        /*
         * 逻辑：
         * 高级业务清洗为用户自定义sql规则，此处无需生成规则
         * */
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        return result;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 生成相似度规则
     * @author dick
     * @date 2022/4/2 18:44
     * @version v1.0
     * @params
     */
    public ResultEntity<String> createSimilarity_Rule() {
        /*
         * 逻辑：
         * 调度任务实时计算相似度比例，此处无需生成规则
         * */
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        return result;
    }
}