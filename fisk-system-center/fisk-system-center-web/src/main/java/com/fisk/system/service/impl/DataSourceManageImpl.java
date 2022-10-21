package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.entity.DataSourcePO;
import com.fisk.system.map.DataSourceMap;
import com.fisk.system.mapper.DataSourceMapper;
import com.fisk.system.service.IDataSourceManageService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

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
    public List<DataSourceDTO> getAllDataSource() {
        List<DataSourceDTO> all = getAll(false);
        return all;
    }

    @Override
    public ResultEnum updateDataSource(DataSourceDTO dto) {
        DataSourcePO model = baseMapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourcePO::getName, dto.name)
                .ne(DataSourcePO::getId, dto.id);
        DataSourcePO data = baseMapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourceMap.INSTANCES.editDtoToPo(dto, model);
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum insertDataSource(DataSourceDTO dto) {
        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourcePO::getName, dto.name)
                .eq(DataSourcePO::getDelFlag, 1);
        DataSourcePO data = baseMapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }
        DataSourcePO model = dtoToPo(dto);
        return baseMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @SneakyThrows
    @Override
    public ResultEnum testConnection(DataSourceDTO dto) {
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
                    Class.forName(DataSourceTypeEnum.ORACLE.getName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
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
        dataSourceDTO.setServiceName(t.getServiceName());
        dataSourceDTO.setDomainName(t.getDomainName());
        dataSourceDTO.setSourceType(t.getSourceType());
        if (isShowPwd) {
            dataSourceDTO.setConPassword(t.getConPassword());
        }
        return dataSourceDTO;
    }

    private DataSourcePO dtoToPo(DataSourceDTO t) {
        DataSourcePO dataSourcePO = new DataSourcePO();
        dataSourcePO.setName(t.getName());
        dataSourcePO.setConStr(t.getConStr());
        dataSourcePO.setConIp(t.getConIp());
        dataSourcePO.setConPort(t.getConPort());
        dataSourcePO.setConDbname(t.getConDbname());
        dataSourcePO.setConType(t.getConType().getValue());
        dataSourcePO.setConAccount(t.getConAccount());
        dataSourcePO.setPlatform(t.getPlatform());
        dataSourcePO.setProtocol(t.getProtocol());
        dataSourcePO.setSourceType(t.getSourceType());
        dataSourcePO.setServiceName(t.getServiceName());
        dataSourcePO.setDomainName(t.getDomainName());
        dataSourcePO.setSourceType(t.getSourceType());
        dataSourcePO.setConPassword(t.getConPassword());
        return dataSourcePO;
    }
}
