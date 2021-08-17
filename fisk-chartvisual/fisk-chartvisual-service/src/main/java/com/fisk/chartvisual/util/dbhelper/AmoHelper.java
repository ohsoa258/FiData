package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.entity.CubePO;
import com.fisk.chartvisual.entity.DimensionPO;
import com.fisk.chartvisual.entity.HierarchyPO;
import com.fisk.chartvisual.entity.MeasurePO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.olap4j.*;
import org.olap4j.metadata.*;
import org.springframework.util.StopWatch;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author JinXingWang
 */
@Slf4j
public class AmoHelper {
    public final DataSourceTypeEnum type;
    private OlapConnection connection;

    public AmoHelper(DataSourceTypeEnum typeEnum) {
        this.type = typeEnum;
    }

    /**
     * 创建连接
     *
     * @param connectionStr 连接字符串
     * @param account 账号
     * @param password 密码
     * @return true | false
     */
    public boolean connection(String connectionStr, String account, String password) {
        try {
            loadDriver();
            OlapConnection conn = (OlapConnection) DriverManager.getConnection(connectionStr, account, password);
            OlapWrapper wrapper = conn;
            connection = wrapper.unwrap(OlapConnection.class);
            OlapDatabaseMetaData metaData=  connection.getMetaData();
            return true;
        } catch (Exception ex) {
            log.error("【connection】数据库连接获取失败, ex", ex);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, ex.getLocalizedMessage());
        }
    }

    /**
     * 加载驱动
     *
     * @throws Exception 驱动加载异常
     */
    private void loadDriver() throws Exception {
        if (type != null) {
            try {
                Class.forName(type.getDriverName());
            } catch (ClassNotFoundException e) {
                throw new Exception("【loadDriver】" + type.getName() + "驱动加载失败, ex", e);
            }
        } else {
            throw new Exception("【loadDriver】错误的驱动类型");
        }
    }

    /**
     * 关闭连接
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                log.info("【connection】数据库连接已关闭");
            } catch (SQLException ex) {
                log.error("【closeConnection】数据库连接关闭失败, ex", ex);
            }
        }
    }

    /**
     * 获取数据库
     *
     * @return 数据库
     * @throws Exception 错误信息
     */
    public List<String> getCatalogs() throws Exception {
        OlapDatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getCatalogs();
        List<String> catalogs = new ArrayList<>();
        while (rs.next()) {
            String value = rs.getString("TABLE_CAT");
            catalogs.add(value);
        }
        return catalogs;
    }

    /**
     * 获取SSAS结构
     * @param catalogName 库名称
     * @param cubeName 模型名称
     * @return 模型结构
     * @throws Exception 错误
     */
    public CubePO getModelStructure(String catalogName, String cubeName) throws Exception {
        Database metadata = connection.getOlapDatabase();
        NamedList<Catalog> catalogList = metadata.getCatalogs();
        Catalog catalog = catalogList.get(catalogName);
        NamedList<Schema> schemas = catalog.getSchemas();
        //架构
        Schema schema = schemas.get("");
        NamedList<Cube> cubes = schema.getCubes();
        Cube cube = cubes.get(cubeName);
        NamedList<Dimension> dimensions = cube.getDimensions();
        CubePO cubePo=new  CubePO();
        cubePo.name=cube.getName();
        cubePo.uniqueName=cube.getUniqueName();
        List<MeasurePO> measurePoList=new ArrayList<>();
        //度量值
        List<Measure> measures = cube.getMeasures();
        for (Measure measure:measures) {
            MeasurePO measurePo=new MeasurePO();
            measurePo.name=measure.getName();
            measurePo.uniqueName=measure.getUniqueName();
            measurePoList.add(measurePo);
        }
        //维度
        List<DimensionPO> dimensionPoList=new ArrayList<>();
        for (Dimension dimension: dimensions ) {
            // MEASURE 为度量值 , OTHER 为维度
            Dimension.Type dimensionType = dimension.getDimensionType();
            if (dimensionType == org.olap4j.metadata.Dimension.Type.OTHER) {
                DimensionPO dimensionPo=new DimensionPO();
                dimensionPo.name = dimension.getName();
                dimensionPo.uniqueName= dimension.getUniqueName();
                List<HierarchyPO> hierarchyPoList=new ArrayList<>();
                NamedList<Hierarchy> hierarchies = dimension.getHierarchies();
                for (Hierarchy  hierarchy: hierarchies) {
                    //层级
                    HierarchyPO hierarchyPo=new HierarchyPO();
                    hierarchyPo.name = hierarchy.getName();
                    hierarchyPo.uniqueName = hierarchy.getUniqueName();
                    hierarchyPoList.add(hierarchyPo);
                }
                dimensionPo.hierarchies= hierarchyPoList;
                dimensionPoList.add(dimensionPo);
            }
        }
        cubePo.measures=measurePoList;
        cubePo.dimensions=dimensionPoList;
        return cubePo;
    }

    /**
     * 查询
     * @param mdx mdx语句
     * @return 数据集合
     */
    public DataServiceResult query(String mdx){
        StopWatch stopWatch = new StopWatch();
        String code = UUID.randomUUID().toString();
        DataServiceResult res=new DataServiceResult();
        try {
            stopWatch.start();
            log.info("【execQuery】【" + code + "】执行MDX: 【" + mdx + "】");
            OlapStatement stmt = connection.createStatement();
            CellSet cellset = stmt.executeOlapQuery(mdx);
            stmt.close();
            res.data =getDataByAnalyticalCellSet(cellset);
        } catch (Exception ex) {
            log.error("【execQuery】【" + code + "】执行MDX查询报错, ex", ex);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ex.getLocalizedMessage());
        }finally {
            stopWatch.stop();
            log.info("【execQuery】【" + code + "】执行时间: 【" + stopWatch.getTotalTimeMillis() + "毫秒】");
        }
        return res;
    }

    /**
     * 解析 cellSet
     * @param cellSet 单元格集合
     * @return map集合
     */
    public List<Map<String,Object>>  getDataByAnalyticalCellSet(CellSet cellSet){
        List<CellSetAxis> axis=cellSet.getAxes();
        switch (axis.size()){
            case 2:
                return getTwoAxisData(cellSet);
            case 1:
                return getOneAxisData(cellSet);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 解析2轴cellSet
     * @param cellSet 单元格集合
     * @return map 集合
     */
    public List<Map<String,Object>> getTwoAxisData(CellSet cellSet){
        for (Position row :cellSet.getAxes().get(1)){
            for (Position cloumn:cellSet.getAxes().get(0)){

            }
        }
        return  null;
    }

    /**
     * 解析1轴cellSet
     * @param cellSet 单元格集合
     * @return map集合
     */
    public List<Map<String,Object>> getOneAxisData(CellSet cellSet){
        List<Map<String,Object>> maps=new ArrayList<>();
        for (Position column : cellSet.getAxes().get(0)) {
            Map<String,Object> map=new HashMap<>();
            for (Member member : column.getMembers()) {
                map.put("name",member.getName());
            }
            final Cell cell = cellSet.getCell(column);
            map.put("value",cell.getValue());
            maps.add(map);
        }
        return maps;
    }
}
