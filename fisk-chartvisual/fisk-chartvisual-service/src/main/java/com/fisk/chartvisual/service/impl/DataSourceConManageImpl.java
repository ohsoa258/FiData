package com.fisk.chartvisual.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import com.fisk.chartvisual.dto.DataDomainDTO;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.dto.DataSourceConQuery;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataSourceConManage;
import com.fisk.chartvisual.service.IUseDataBase;
import com.fisk.chartvisual.util.dscon.AbstractUseDataBase;
import com.fisk.chartvisual.util.dscon.DataSourceConFactory;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.chartvisual.vo.DataServiceVO;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.constants.SqlConstants;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.JsonUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    IUseDataBase useDataBase;


    @Override
    public Page<DataSourceConVO> listDataSourceCons(Page<DataSourceConVO> page, DataSourceConQuery query) {
        UserDetail context = UserContext.getUser();
//        query.userId = context.getId();
        return mapper.listDataSourceConByUserId(page, query);
    }

    @Override
    public ResultEnum saveDataSourceCon(DataSourceConDTO dto) {
        UserDetail context = UserContext.getUser();
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);
//        model.createUser = context.getId().toString();
        return mapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateDataSourceCon(DataSourceConEditDTO dto) {
        DataSourceConPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        UserDetail context = UserContext.getUser();
        DataSourceConMap.INSTANCES.editDtoToPo(dto, model);
//        model.updateUser = context.getId().toString();
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceCon(int id) {
        DataSourceConPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        UserDetail context = UserContext.getUser();
        model.delFlag = Integer.parseInt(SqlConstants.DEL);
//        model.updateUser = context.getId().toString();
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum testConnection(DataSourceConDTO dto) {
        return useDataBase.testConnection(dto.conType, dto.conStr, dto.conAccount, dto.conPassword)
                ?
                ResultEnum.SUCCESS : ResultEnum.VISUAL_CONNECTION_ERROR;
    }

    @Override
    public List<DataDomainVO> listDataDomain(int id) {
        //获取连接信息
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if(model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //创建连接
        AbstractUseDataBase db = DataSourceConFactory.getConnection(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        //执行查询
        List<DataDomainDTO> data = db.execQuery(db.buildDataDomainQuery(model.conDbname), connection, DataDomainDTO.class);
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
        db.closeConnection(connection);
        return null;
    }
}
