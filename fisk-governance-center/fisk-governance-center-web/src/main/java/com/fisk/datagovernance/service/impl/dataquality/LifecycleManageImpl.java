package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleQueryDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.dataquality.CheckStepTypeEnum;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import com.fisk.datagovernance.map.dataquality.LifecycleMap;
import com.fisk.datagovernance.mapper.dataquality.ComponentNotificationMapper;
import com.fisk.datagovernance.mapper.dataquality.LifecycleMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.ILifecycleManageService;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期实现类
 * @date 2022/3/23 12:56
 */
@Service
public class LifecycleManageImpl extends ServiceImpl<LifecycleMapper, LifecyclePO> implements ILifecycleManageService {

    @Resource
    private ComponentNotificationMapper componentNotificationMapper;

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<LifecycleVO> getAll(LifecycleQueryDTO query) {
        Page<LifecycleVO> all = baseMapper.getAll(query.page, query.conIp, query.conDbname, query.tableName, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> collect = all.getRecords().stream().map(LifecycleVO::getId).distinct().collect(Collectors.toList());
            List<Integer> collect1 = all.getRecords().stream().map(LifecycleVO::getTemplateId).distinct().collect(Collectors.toList());
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
        return all;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(LifecycleDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //根据配置的条件生成生命周期规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.moduleRule = role.data;
        //第一步：转换DTO对象为PO对象
        LifecyclePO lifecyclePO = LifecycleMap.INSTANCES.dtoToPo(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        lifecyclePO.setCreateTime(LocalDateTime.now());
        lifecyclePO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(lifecyclePO.templateId, lifecyclePO.getId(), dto.componentNotificationDTOS, false);
        //第四步：如果设置了调度任务条件，则生成调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(LifecycleEditDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //验证组件是否存在
        LifecyclePO lifecyclePO = baseMapper.selectById(dto.id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //根据配置的条件生成生命周期规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.moduleRule = role.data;
        //第一步：转换DTO对象为PO对象
        lifecyclePO = LifecycleMap.INSTANCES.dtoToPo_Edit(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        // 重置系统字段
        lifecyclePO.setCheckEmptytbDay(0);
        lifecyclePO.setCheckConsanguinityDay(0);
        lifecyclePO.setCheckRefreshtbDay(0);
        //第二步：保存数据校验信息
        int i = baseMapper.updateById(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息，先将原来的组件通知关联关系置为无效
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(lifecyclePO.templateId, lifecyclePO.getId(), dto.componentNotificationDTOS, true);
        //第四步：根据组件状态&Corn表达式，调整调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        LifecyclePO lifecyclePO = baseMapper.selectById(id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 删除关联通知
        componentNotificationMapImpl.updateDelFlag(0, lifecyclePO.getTemplateId(), lifecyclePO.getId());
        // 删除调度任务

        return baseMapper.deleteByIdWithFill(lifecyclePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 根据生命周期条件生成校验规则
     * @author dick
     * @date 2022/4/2 15:51
     * @version v1.0
     * @params dto
     * @params templateTypeEnum
     */
    public ResultEntity<String> createRole(LifecycleDTO dto, TemplateTypeEnum templateTypeEnum) {
        //查询数据源
        DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getDataSourceConPO(dto.datasourceId, dto.datasourceType);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, "");
        }
        // 待确定元数据返回的类型是几
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];

        String tableName = dto.tableName;
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
            case SPECIFY_TIME_RECYCLING_TEMPLATE:
                //指定时间回收模板
                rule = createSpecifyTime_RecyclingRule();
                break;
            case EMPTY_TABLE_RECOVERY_TEMPLATE:
                //空表回收模板
                rule = createEmptyTable_RecoveryRule(dataSourceConPO.conDbname, dataSourceTypeEnum,tableName);
                break;
            case NO_REFRESH_DATA_RECOVERY_TEMPLATE:
                //数据无刷新回收模板
                rule = createNoRefreshData_RecoveryRule(dataSourceConPO.conDbname, dataSourceTypeEnum, tableName,fieldName);
                break;
            case DATA_BLOOD_KINSHIP_RECOVERY_TEMPLATE:
                //数据血缘断裂回收模板
                rule = createDataBloodKinship_RecoveryRule();
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, "");
        }
        return rule;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 指定时间回收表生命周期规则
     * @author dick
     * @date 2022/4/2 18:36
     * @version v1.0
     */
    public ResultEntity<String> createSpecifyTime_RecyclingRule() {
        /*
         * 逻辑：
         * 无规则。调度任务实时处理
         * */
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        return result;
    }

    /**
     * @return com.fisk.common.core.response.ResultEntity<java.lang.String>
     * @description 空表回收生命周期规则
     * @author dick
     * @date 2022/4/2 15:46
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     */
    public ResultEntity<String> createEmptyTable_RecoveryRule(String dataBase, DataSourceTypeEnum dataSourceTypeEnum, String tableName) {
        if (tableName == null || tableName.isEmpty()) {
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
     * @description 数据无刷新回收生命周期规则
     * @author dick
     * @date 2022/4/2 15:46
     * @version v1.0
     * @params dataSourceTypeEnum 数据源类型
     * @params tableName 表名称
     * @params fieldName 更新依据字段
     */
    public ResultEntity<String> createNoRefreshData_RecoveryRule(String dataBase, DataSourceTypeEnum dataSourceTypeEnum,
                                                            String tableName, String fieldName) {
        if (tableName == null || tableName.isEmpty()
                || fieldName == null || fieldName.isEmpty()) {
            return ResultEntityBuild.buildData(ResultEnum.SAVE_VERIFY_ERROR, null);
        }
        /*
         * 逻辑
         * 任务调度
         * 设10天未更新则冻结表，调度每次验证后如果未更新表数据则不需要更新规则。如果验证有更新，则需要重新生成验证规则
         * 因为需要重新生成更新参照时间，同时将空表持续天数设置为0
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
     * @description 数据血缘断裂回收表生命周期规则
     * @author dick
     * @date 2022/4/2 18:36
     * @version v1.0
     */
    public ResultEntity<String> createDataBloodKinship_RecoveryRule() {
        /*
         * 逻辑：
         * 无规则。调度任务实时处理
         * */
        ResultEntity<String> result = new ResultEntity<>();
        result.setCode(0);
        return result;
    }


}