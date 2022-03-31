package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.chartvisual.*;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.util.dbhelper.*;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.utils.office.excel.ExcelUtil;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.DragElemTypeEnum.COLUMN;
import static com.fisk.chartvisual.enums.DragElemTypeEnum.ROW;

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

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public DataServiceResult querySsas(ChartQueryObjectSsas query) {
        switch (query.graphicType){
            default:
                List<ColumnDetailsSsas> columnDetailsSsas = new ArrayList<>();
                List<ColumnDetailsSsas> columnList = query.columnDetails.stream().filter(e -> e.dragElemType == COLUMN)
                        .map(e -> new ColumnDetailsSsas() {{
                            name = e.name;
                            uniqueName = e.uniqueName;
                            dimensionType = e.dimensionType;
                            dragElemType = ROW;
                        }}).collect(Collectors.toList());
                columnDetailsSsas.addAll(columnList);

                List<ColumnDetailsSsas> noColumnList = query.columnDetails.stream().filter(e -> e.dragElemType != COLUMN)
                        .map(e -> new ColumnDetailsSsas() {{
                            name = e.name;
                            uniqueName = e.uniqueName;
                            dimensionType = e.dimensionType;
                            dragElemType = e.dragElemType;
                        }}).collect(Collectors.toList());
                columnDetailsSsas.addAll(noColumnList);

                query.columnDetails(columnDetailsSsas);
        }

        DataSourceConVO model = getDataSourceCon(query.id);
        cubeHelper.connection(model.conStr, model.conAccount, model.conPassword);
        return  cubeHelper.getData(query,model.conCube);
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
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
