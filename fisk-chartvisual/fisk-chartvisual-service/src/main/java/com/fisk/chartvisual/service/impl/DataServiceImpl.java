package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.util.dbhelper.AbstractDbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.DbHelper;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSQLCommand;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

/**
 * @author gy
 */
@Service
public class DataServiceImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataService {

    @Resource
    private DataSourceConMapper mapper;

    @Override
    public boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(type);
        Connection connection = db.connection(con, acc, pwd);
        boolean res = connection != null;
        db.closeConnection(connection);
        return res;
    }

    @Override
    public List<Map<String, Object>> query(ChartQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);

        IBuildSQLCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        return DbHelper.execQueryResultMap(command.buildQueryData(query), model);
    }

    @Override
    public List<Map<String, Object>> querySlicer(SlicerQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);

        IBuildSQLCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        return DbHelper.execQueryResultMap(command.buildQuerySlicer(query), model);
    }


    private DataSourceConVO getDataSourceCon(int id) {
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return model;
    }

}
