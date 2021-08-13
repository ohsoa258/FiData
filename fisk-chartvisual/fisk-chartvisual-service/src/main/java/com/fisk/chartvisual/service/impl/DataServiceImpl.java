package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ChartQueryObjectSsas;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.util.dbhelper.*;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.excel.ExcelUtil;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
@Service
public class DataServiceImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IDataService {

    @Resource
    private DataSourceConMapper mapper;
    @Resource
    RedisUtil redis;
    @Resource
    CubeHelper cubeHelper;

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_CONNECTION)
    @Override
    public boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd) {
        AbstractDbHelper db = DbHelperFactory.getDbHelper(type);
        Connection connection = db.connection(con, acc, pwd);
        boolean res = connection != null;
        db.closeConnection(connection);
        return res;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public DataServiceResult query(ChartQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);
        return DbHelper.getDataService(query, model);
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
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

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public List<Map<String, Object>> querySlicer(SlicerQueryObject query) {
        DataSourceConVO model = getDataSourceCon(query.id);

        IBuildSqlCommand command = DbHelperFactory.getSqlBuilder(model.conType);
        return DbHelper.execQueryResultMaps(command.buildQuerySlicer(query), model);
    }

    @Override
    public DataServiceResult querySsas(ChartQueryObjectSsas query) {
        DataSourceConVO model = getDataSourceCon(query.id);
        MdxHelper mdxHelper=new MdxHelper();
        cubeHelper.connection(model.conStr, model.conAccount, model.conPassword);
        String mdx=mdxHelper.GetMdxByColumnRowValue(query.columnDetails,model.conCube);
        return  cubeHelper.qeury(mdx);
    }


    private DataSourceConVO getDataSourceCon(int id) {
        DataSourceConVO model = mapper.getDataSourceConByUserId(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return model;
    }

}
