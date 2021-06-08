package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IUseDataBase;
import com.fisk.chartvisual.util.dscon.AbstractUseDataBase;
import com.fisk.chartvisual.util.dscon.DataSourceConFactory;
import com.fisk.chartvisual.vo.DataServiceVO;
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
public class UseDataBaseImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IUseDataBase {

    @Resource
    private DataSourceConMapper mapper;

    @Override
    public boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd) {
        AbstractUseDataBase db = DataSourceConFactory.getConnection(type);
        Connection connection = db.connection(con, acc, pwd);
        boolean res = connection != null;
        db.closeConnection(connection);
        return res;
    }

    @Override
    public List<Map<String, Object>> query(ChartQueryObject query) {
        DataSourceConVO model = mapper.getDataSourceConByUserId(query.id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        AbstractUseDataBase db = DataSourceConFactory.getConnection(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return db.execQuery(db.buildQueryData(query), connection);
    }


}
