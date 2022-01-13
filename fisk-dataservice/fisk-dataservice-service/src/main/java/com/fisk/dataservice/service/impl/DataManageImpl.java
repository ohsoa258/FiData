package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.dto.datasource.ChartQueryObject;
import com.fisk.dataservice.dto.datasource.ChartQueryObjectSsas;
import com.fisk.dataservice.dto.datasource.SlicerQueryObject;
import com.fisk.dataservice.dto.datasource.SlicerQuerySsasObject;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.mapper.DataSourceConMapper;
import com.fisk.dataservice.service.IDataManageService;
import com.fisk.dataservice.utils.*;
import com.fisk.dataservice.utils.buildsql.IBuildSqlCommand;
import com.fisk.dataservice.vo.datasource.DataServiceResult;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.excel.ExcelUtil;
import com.fisk.common.exception.FkException;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 数据接口实现类
 * @author dick
 */
@Service
public class DataManageImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataManageService {

    @Resource
    private DataSourceConMapper mapper;
    @Resource
    RedisUtil redis;
    @Resource
    CubeHelper cubeHelper;

    @Override
    public boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(type);
        Connection connection = db.connection(con, acc, pwd);
        boolean res = connection != null;
        db.closeConnection(connection);
        return res;
    }

    @Override
    public DataServiceResult query(ChartQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);
        return DbHelper.getDataService(query, model);
    }

    @Override
    public void downLoad(String key, HttpServletResponse response) {
        ChartQueryObject query = (ChartQueryObject) redis.get(key);
        if(query == null){
            return;
        }
        redis.del(key);
        DataSourceConVO model = getDataSourceCon(query.id);
        DataServiceResult res = DbHelper.getDataService(query, model);
        ExcelUtil.uploadExcelAboutUser(response, "test.xlsx", res.data);
    }

    @Override
    public List<Map<String, Object>> querySlicer(SlicerQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);

        IBuildSqlCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        return DbHelper.execQueryResultMaps(command.buildQuerySlicer(query), model);
    }

    @Override
    public DataServiceResult querySsas(ChartQueryObjectSsas query) {
        DataSourceConVO model = getDataSourceCon(query.id);
        cubeHelper.connection(model.conStr, model.conAccount, model.conPassword);
        return  cubeHelper.getData(query,model.conCube);
    }

    @Override
    public List<String> querySsasSlicer(SlicerQuerySsasObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);
        cubeHelper.connection(model.conStr, model.conAccount, model.conPassword);
        return  cubeHelper.getMembers(model.conCube,query.hierarchyName);
    }

    private DataSourceConVO getDataSourceCon(int id) {
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return model;
    }

}
