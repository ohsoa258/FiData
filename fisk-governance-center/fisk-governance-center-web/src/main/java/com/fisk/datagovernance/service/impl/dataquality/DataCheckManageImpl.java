package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckEditDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckQueryDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import com.fisk.datagovernance.entity.dataquality.DataCheckPO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.DataCheckMap;
import com.fisk.datagovernance.mapper.dataquality.ComponentNotificationMapper;
import com.fisk.datagovernance.mapper.dataquality.DataCheckMapper;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.IDataCheckManageService;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<DataCheckVO> getAll(DataCheckQueryDTO query) {
        Page<DataCheckVO> all = baseMapper.getAll(query.page, query.tableName, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> collect = all.getRecords().stream().map(DataCheckVO::getId).distinct().collect(Collectors.toList());
            List<Integer> collect1 = all.getRecords().stream().map(DataCheckVO::getTemplateId).distinct().collect(Collectors.toList());
            QueryWrapper<ComponentNotificationPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1)
                    .in(ComponentNotificationPO::getTemplateId, collect1)
                    .in(ComponentNotificationPO::getModuleId, collect);
            List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                all.getRecords().forEach(e -> {
                    List<Integer> collect2 = componentNotificationPOS.stream()
                            .filter(item -> item.getTemplateId() == e.getTemplateId() && item.getModuleId() == e.getId())
                            .map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect2)) {
                        e.setNoticeIds(collect2);
                    }
                });
            }
        }
        return  all;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataCheckDTO dto) {
        //第一步：根据配置的校验条件生成校验规则
        ResultEntity<String> role = createRole(dto);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.values()[role.code];
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
        //第五步：如果设置了调度任务条件，则生成调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(DataCheckEditDTO dto) {
        DataCheckPO dataCheckPO = baseMapper.selectById(dto.id);
        if (dataCheckPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：根据配置的校验条件生成校验规则
        ResultEntity<String> role = createRole(dto);
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
        //第五步：根据组件状态&Corn表达式，调整调度任务
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
     * @return 校验规则
     * @description 根据校验条件生成校验规则
     * @author dick
     * @date 2022/3/24 17:59
     * @version v1.0
     * @params 数据校验DTO
     */
    public ResultEntity<String> createRole(DataCheckDTO dto) {
        //selectById 默认仅查询有效的
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, "");
        }
        //查询数据源
        DataSourceConPO dataSourceConPO = getDataSourceConPO(dto.datasourceId, dto.datasourceType);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, "");
        }
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        String tableName = dto.checkStep == CheckStepTypeEnum.TABLE_FRONT ? dto.proTableName : dto.tableName;
        ResultEntity<String> rule = null;
        switch (templateTypeEnum) {
            case FIELD_STRONG_RULE_TEMPLATE:
                //强类型模板规则
                rule = createField_StrongRule(dataSourceTypeEnum, tableName, dto.fieldName, dto.fieldLength, dto.checkRuleType);
                break;
            case FIELD_AGGREGETION_TEMPLATE:
                break;
            case TABLEROW_THRESHOLD_TEMPLATE:
                break;
            case EMPTY_TABLE_CHECK_TEMPLATE:
                break;
            case UPDATE_TABLE_CHECK_TEMPLATE:
                break;
            case TABLE_BLOOD_KINSHIP_CHECK_TEMPLATE:
                break;
            case BUSINESS_CHECK_TEMPLATE:
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
     * @return com.fisk.common.response.ResultEntity<java.lang.String>
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
    public ResultEntity<String> createField_StrongRule(DataSourceTypeEnum dataSourceTypeEnum, String tableName, String fieldName,
                                                       int fieldLength, String checkRuleType) {
        StringBuilder stringBuilder = new StringBuilder();
        if (tableName == null || tableName.isEmpty() ||
                fieldName == null || fieldName.isEmpty() ||
                checkRuleType == null || checkRuleType.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_STRONGRULE_ERROR, stringBuilder.toString());
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
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_STRONGRULE_ERROR, stringBuilder.toString());
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
            }
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, stringBuilder.toString());
    }
}