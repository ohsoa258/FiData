package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.system.dto.GetConfigDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.DataSourcePageDTO;
import com.fisk.system.dto.datasource.DataSourceQueryDTO;
import com.fisk.system.dto.datasource.DataSourceSaveDTO;
import com.fisk.system.entity.DataSourcePO;
import com.fisk.system.map.DataSourceMap;
import com.fisk.system.mapper.DataSourceMapper;
import com.fisk.system.service.IDataSourceManageService;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源接口实现类
 *
 * @author dick
 */
@Service
public class DataSourceManageImpl extends ServiceImpl<DataSourceMapper, DataSourcePO> implements IDataSourceManageService {

    @Resource
    private GetConfigDTO getConfig;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Override
    public List<DataSourceDTO> getSystemDataSource() {
        List<DataSourceDTO> all = getAll(true);
        if (CollectionUtils.isNotEmpty(all)) {
            all = all.stream().filter(t -> t.getSourceType() == 1).collect(Collectors.toList());
        }
        return all;
    }

    @Override
    public List<DataSourceDTO> getExternalDataSource() {
        List<DataSourceDTO> all = getAll(true);
        if (CollectionUtils.isNotEmpty(all)) {
            all = all.stream().filter(t -> t.getSourceType() == 2).collect(Collectors.toList());
        }
        return all;
    }

    @Override
    public List<DataSourceDTO> getAll() {
        List<DataSourceDTO> all = getAll(true);
        return all;
    }

    @Override
    public List<FilterFieldDTO> getSearchColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_datasource_config";
        dto.filterSql = FilterSqlConstants.PLATFORM_DATASOURCE_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<DataSourceDTO> getAllDataSource(DataSourceQueryDTO queryDTO) {
        StringBuilder querySql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(queryDTO.getDto())) {
            List<FilterQueryDTO> filterQueryDTOS = queryDTO.getDto().stream().filter(t -> t.columnName.contains("con_type")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filterQueryDTOS)) {
                filterQueryDTOS.forEach(filterQueryDTO -> {
                    if (StringUtils.isNotEmpty(filterQueryDTO.getColumnValue())) {
                        if (filterQueryDTO.getColumnValue().equalsIgnoreCase("MYSQL")) {
                            filterQueryDTO.setColumnValue("0");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("SQLSERVER")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("SSMS")) {
                            filterQueryDTO.setColumnValue("1");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("POSTGRESQL")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("PG")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("PGSQL")) {
                            filterQueryDTO.setColumnValue("4");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("DORIS")) {
                            filterQueryDTO.setColumnValue("5");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("ORACLE")) {
                            filterQueryDTO.setColumnValue("6");
                        }
                    }
                });
            }
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(queryDTO.getDto()));
        DataSourcePageDTO data = new DataSourcePageDTO();
        data.page = queryDTO.getPage();
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();
        if (queryDTO.getSourceType() > 0) {
            data.where += " AND ds.source_type=" + queryDTO.getSourceType();
        }

        Page<DataSourceDTO> filter = baseMapper.filter(queryDTO.getPage(), data);
        if (filter != null && CollectionUtils.isNotEmpty(filter.getRecords())) {
            filter.getRecords().stream().forEach(t -> {
                t.setConType(DataSourceTypeEnum.getEnum(t.getConTypeValue()));
                t.setConTypeName(t.getConType().getName());
                t.setSourceBusinessType(SourceBusinessTypeEnum.getEnum(t.getSourceBusinessTypeValue()));
            });
        }
        return filter;
    }

    @Override
    public ResultEnum updateDataSource(DataSourceSaveDTO dto) {
        DataSourcePO model = baseMapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourcePO::getName, dto.name)
                .eq(DataSourcePO::getSourceType, dto.sourceType)
                .ne(DataSourcePO::getId, dto.id);
        DataSourcePO data = baseMapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourceMap.INSTANCES.dtoToPo(dto, model);
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSource(int id) {
        DataSourcePO model = baseMapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (model.getSourceType() == 1) {
            return ResultEnum.SYSTEM_DATA_SOURCE_NOT_OPERATION;
        }
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.DELETE_ERROR;
    }

    @Override
    public ResultEnum insertDataSource(DataSourceSaveDTO dto) {
        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourcePO::getName, dto.name)
                .eq(DataSourcePO::getSourceType, dto.sourceType)
                .eq(DataSourcePO::getDelFlag, 1);
        DataSourcePO model = baseMapper.selectOne(queryWrapper);
        if (model != null && model.getSourceType() == 2) {
            return ResultEnum.NAME_EXISTS;
        }
        model = new DataSourcePO();
        DataSourceMap.INSTANCES.dtoToPo(dto, model);
        return baseMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @SneakyThrows
    @Override
    public ResultEnum testConnection(DataSourceSaveDTO dto) {
        Connection conn = null;
        try {
            switch (dto.conType) {
                case MYSQL:
                    Class.forName(DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case SQLSERVER:
                    Class.forName(DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case POSTGRESQL:
                    Class.forName(DataSourceTypeEnum.POSTGRESQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case DORIS:
                    Class.forName(DataSourceTypeEnum.DORIS.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case ORACLE:
                    Class.forName(DataSourceTypeEnum.ORACLE.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                default:
                    return ResultEnum.DS_DATASOURCE_CON_WARN;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            return ResultEnum.DATASOURCE_CONNECTERROR;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DATASOURCE_CONNECTCLOSEERROR);
            }
        }
    }

    @Override
    public ResultEntity<DataSourceDTO> getById(int datasourceId) {
        DataSourcePO t = baseMapper.selectById(datasourceId);
        if (t == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, null);
        }
        DataSourceDTO dataSourceDTO = poToDto(true, t);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSourceDTO);
    }

    public List<DataSourceDTO> getAll(boolean isShowPwd) {
        List<DataSourceDTO> dataSourceList = new ArrayList<>();
        QueryWrapper<DataSourcePO> dataSourcePOQueryWrapper = new QueryWrapper<>();
        dataSourcePOQueryWrapper.lambda().eq(DataSourcePO::getDelFlag, 1);
        List<DataSourcePO> dataSourcePOS = baseMapper.selectList(dataSourcePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataSourcePOS)) {
            dataSourcePOS.forEach(t -> {
                DataSourceDTO dataSourceDTO = poToDto(isShowPwd, t);
                dataSourceList.add(dataSourceDTO);
            });
        }
        return dataSourceList;
    }

    private DataSourceDTO poToDto(boolean isShowPwd, DataSourcePO t) {
        DataSourceDTO dataSourceDTO = new DataSourceDTO();
        dataSourceDTO.setId(Math.toIntExact(t.getId()));
        dataSourceDTO.setName(t.getName());
        dataSourceDTO.setConStr(t.getConStr());
        dataSourceDTO.setConIp(t.getConIp());
        dataSourceDTO.setConPort(t.getConPort());
        dataSourceDTO.setConDbname(t.getConDbname());
        dataSourceDTO.setConType(DataSourceTypeEnum.getEnum(t.getConType()));
        dataSourceDTO.setConTypeName(DataSourceTypeEnum.getEnum(t.getConType()).getName());
        dataSourceDTO.setConAccount(t.getConAccount());
        dataSourceDTO.setPlatform(t.getPlatform());
        dataSourceDTO.setProtocol(t.getProtocol());
        dataSourceDTO.setSourceType(t.getSourceType());
        dataSourceDTO.setSourceBusinessType(SourceBusinessTypeEnum.getEnum(t.getSourceBusinessType()));
        dataSourceDTO.setSourceBusinessTypeValue(t.getSourceBusinessType());
        dataSourceDTO.setServiceName(t.getServiceName());
        dataSourceDTO.setDomainName(t.getDomainName());
        dataSourceDTO.setSourceType(t.getSourceType());
        dataSourceDTO.setPurpose(t.getPurpose());
        dataSourceDTO.setPrincipal(t.getPrincipal());
        if (isShowPwd) {
            dataSourceDTO.setConPassword(t.getConPassword());
        }
        return dataSourceDTO;
    }
}
