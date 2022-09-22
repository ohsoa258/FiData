package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleQueryDTO;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.map.dataquality.LifecycleMap;
import com.fisk.datagovernance.mapper.dataquality.DataSourceConMapper;
import com.fisk.datagovernance.mapper.dataquality.LifecycleMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.ILifecycleManageService;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期实现类
 * @date 2022/3/23 12:56
 */
@Service
public class LifecycleManageImpl extends ServiceImpl<LifecycleMapper, LifecyclePO> implements ILifecycleManageService {

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private ExternalInterfaceImpl externalInterfaceImpl;

    @Override
    public Page<LifecycleVO> getAll(LifecycleQueryDTO query) {
        return baseMapper.getAll(query.page, query.datasourceId, query.tableUnique,
                query.tableBusinessType,query.keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(LifecycleDTO dto) {
        if (dto.sourceTypeEnum == SourceTypeEnum.FiData) {
            int idByDataSourceId = dataSourceConManageImpl.getIdByDataSourceId(dto.sourceTypeEnum, dto.datasourceId);
            if (idByDataSourceId == 0) {
                return ResultEnum.DATA_QUALITY_DATASOURCE_ONTEXISTS;
            }
            dto.datasourceId = idByDataSourceId;
        }
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：根据配置的条件生成生命周期规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.createRule = role.data;
        //第三步：转换DTO对象为PO对象
        LifecyclePO lifecyclePO = LifecycleMap.INSTANCES.dtoToPo(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第四步：保存数据校验信息
        int i = baseMapper.insert(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第五步：调用元数据接口获取最新的规则信息
        externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(LifecycleEditDTO dto) {
        //第一步：验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第二步：验证规则是否存在
        LifecyclePO lifecyclePO = baseMapper.selectById(dto.id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第三步：根据配置的条件生成生命周期规则
        TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.getEnum(templatePO.getTemplateType());
        ResultEntity<String> role = createRole(dto, templateTypeEnum);
        if (role == null || role.code != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.getEnum(role.getCode());
        }
        dto.createRule = role.data;
        //第四步：转换DTO对象为PO对象
        lifecyclePO = LifecycleMap.INSTANCES.dtoToPo_Edit(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        // 重置系统字段
        lifecyclePO.setContinuedNumber(0);
        //第五步：保存数据校验信息
        int i = baseMapper.updateById(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第六步：调用元数据接口获取最新的规则信息
        externalInterfaceImpl.synchronousTableBusinessMetaData(dto.getDatasourceId(), dto.getSourceTypeEnum(), dto.getTableBusinessType(), dto.getTableUnique());
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        LifecyclePO lifecyclePO = baseMapper.selectById(id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 调用元数据接口获取最新的规则信息
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(lifecyclePO.getDatasourceId());
        if (dataSourceConPO != null) {
            SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getEnum(dataSourceConPO.getDatasourceType());
            externalInterfaceImpl.synchronousTableBusinessMetaData(lifecyclePO.getDatasourceId(), sourceTypeEnum, lifecyclePO.getTableBusinessType(), lifecyclePO.getTableUnique());
        }
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
        DataSourceConPO dataSourceConPO = dataSourceConManageImpl.getById(dto.datasourceId);
        if (dataSourceConPO == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_DATASOURCE_EXISTS, "");
        }
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.values()[dataSourceConPO.getConType()];

        String tableName = dto.tableName;
        String fieldName = dto.fieldName;
        if (StringUtils.isNotEmpty(fieldName)) {
            if (dataSourceTypeEnum == DataSourceTypeEnum.MYSQL) {
                fieldName = "`" + fieldName + "`";
            } else if (dataSourceTypeEnum == DataSourceTypeEnum.SQLSERVER) {
                fieldName = "[" + fieldName + "]";
            } else if (dataSourceTypeEnum == DataSourceTypeEnum.POSTGRESQL) {
                fieldName = "" + fieldName + "";
            }
        }

        ResultEntity<String> rule = null;
        switch (templateTypeEnum) {
            case EMPTY_TABLE_RECOVERY_TEMPLATE:
                //空表回收模板
                rule = createEmptyTable_RecoveryRule(dataSourceConPO.conDbname, dataSourceTypeEnum, tableName);
                break;
            case NO_REFRESH_DATA_RECOVERY_TEMPLATE:
                //数据无刷新回收模板
                rule = createNoRefreshData_RecoveryRule(dataSourceConPO.conDbname, dataSourceTypeEnum, tableName, fieldName);
                break;
            default:
                return ResultEntityBuild.buildData(ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS, "");
        }
        return rule;
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
        if (StringUtils.isEmpty(dataBase) ||
                StringUtils.isEmpty(tableName) ||
                StringUtils.isEmpty(fieldName)) {
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
}