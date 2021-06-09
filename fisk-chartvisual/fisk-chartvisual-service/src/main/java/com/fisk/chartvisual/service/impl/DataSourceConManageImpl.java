package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataSourceConManage;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.util.dbhelper.DbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSQLCommand;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.enums.TraceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源管理实现类
 *
 * @author gy
 */
@Service
public class DataSourceConManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataSourceConManage {

    //TODO: 未获取登录人信息

    @Resource
    DataSourceConMapper mapper;
    @Resource
    IDataService useDataBase;


    @Override
    public Page<DataSourceConVO> listDataSourceCons(Page<DataSourceConVO> page, DataSourceConQuery query) {
        return mapper.listDataSourceConByUserId(page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourceConPO::getName, dto.name);
        //queryWrapper.eq(DataSourceConPO::getCreateUser,"用户id");
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }

        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        DataSourceConPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<DataSourceConPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourceConPO::getName, dto.name)
                .ne(DataSourceConPO::getId, dto.id);
        //queryWrapper.eq(DataSourceConPO::getCreateUser,"用户id");
        DataSourceConPO data = mapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.NAME_EXISTS;
        }

        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_CONNECTION)
    @Override
    public ResultEnum testConnection(TestConnectionDTO dto) {
        return useDataBase.testConnection(dto.conType, dto.conStr, dto.conAccount, dto.conPassword)
                ?
                ResultEnum.SUCCESS : ResultEnum.VISUAL_CONNECTION_ERROR;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public List<DataDomainVO> listDataDomain(int id) {
        //获取连接信息
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //创建连接
        IBuildSQLCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        List<DataDomainDTO> data = DbHelper.execQueryResultList(command.buildDataDomainQuery(model.conDbname), model, DataDomainDTO.class);
        if (data != null) {
            //格式化结果。根据表名称/描述字段分组，获取每个表的字段信息
            return data.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(
                                    () -> new TreeSet<>(
                                            Comparator.comparing(
                                                    o -> o.getTableName() + "#" + o.getTableDetails()))),
                            ArrayList::new)
                    )
                    .stream()
                    .map(e ->
                            new DataDomainVO() {{
                                name = e.tableName;
                                details = e.tableDetails;
                                children = data.stream()
                                        .filter(c -> c.tableName.equals(e.tableName))
                                        .map(item -> new DataDomainVO(item.columnName, item.columnDetails))
                                        .collect(Collectors.toList());
                            }})
                    .collect(Collectors.toList());
        }
        return null;
    }
}
