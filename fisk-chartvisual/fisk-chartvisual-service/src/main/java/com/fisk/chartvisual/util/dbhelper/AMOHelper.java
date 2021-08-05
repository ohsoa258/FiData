package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.entity.CubePO;
import com.fisk.chartvisual.entity.DimensionPO;
import com.fisk.chartvisual.entity.HierarchyPO;
import com.fisk.chartvisual.entity.MeasurePO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import io.netty.util.concurrent.ProgressivePromise;
import lombok.extern.slf4j.Slf4j;
import org.olap4j.*;
import org.olap4j.metadata.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olap4j.driver.xmla.XmlaOlap4jDriver;

/**
 * @author JinXingWang
 */
@Slf4j
public class AMOHelper {
    public final DataSourceTypeEnum type;
    private OlapConnection connection;

    public AMOHelper(DataSourceTypeEnum typeEnum) {
        this.type = typeEnum;
    }

    /**
     * 创建连接
     *
     * @param ConnectionStr
     * @param Account
     * @param Password
     * @return
     */
    public boolean connection(String ConnectionStr, String Account, String Password) {
        try {
            loadDriver();
            OlapConnection conn = (OlapConnection) DriverManager.getConnection(ConnectionStr, Account, Password);
            OlapWrapper wrapper = conn;
            connection = wrapper.unwrap(OlapConnection.class);
            return true;
        } catch (Exception ex) {
            log.error("【connection】数据库连接获取失败, ex", ex);
            return false;
//            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, ex.getLocalizedMessage());
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
     * @return
     * @throws Exception
     */
    public List<String> getCatalogs() throws Exception {
        OlapDatabaseMetaData MetaData = connection.getMetaData();
        ResultSet rs = MetaData.getCatalogs();
        List<String> catalogs = new ArrayList<>();
        while (rs.next()) {
            String value = rs.getString("TABLE_CAT");
            catalogs.add(value);
        }
        return catalogs;
    }


    /**
     * 获取Schemas
     *
     * @return
     */
    public List<String> getSchemas() {
        List<String> Schemas = new ArrayList<>();
        // TABULAR 没有Schema
        switch (type) {
            case TABULAR:
                break;
            case CUBE:
                break;
            default:
                break;
        }
        return Schemas;
    }

    /**
     * 获取Cube
     *
     * @param catalogs
     * @throws Exception
     */
    public void getCubes(String catalogs) throws Exception {
//        CATALOG_NAME
//        SCHEMA_NAME
//        CUBE_NAME
//        CUBE_TYPE
//        CUBE_GUID
//        CREATED_ON
//        LAST_SCHEMA_UPDATE
//        SCHEMA_UPDATED_BY
//        LAST_DATA_UPDATE
//        DATA_UPDATED_BY
//        IS_DRILLTHROUGH_ENABLED
//        IS_WRITE_ENABLED
//        IS_LINKABLE
//        IS_SQL_ENABLED
//        DESCRIPTION
//        CUBE_CAPTION
//        BASE_CUBE_NAME
        OlapDatabaseMetaData MetaData = connection.getMetaData();
        List<String> Schemas = new ArrayList<>();
        // TABULAR 没有Schema
        switch (type) {
            case TABULAR:
                break;
            case CUBE:
                break;
            default:
                break;
        }
    }

    public void getDimensions(String catalogs, String cube) {
//        CATALOG_NAME
//        SCHEMA_NAME
//        CUBE_NAME
//        DIMENSION_NAME
//        DIMENSION_UNIQUE_NAME
//        DIMENSION_GUID
//        DIMENSION_CAPTION
//        DIMENSION_ORDINAL
//        DIMENSION_TYPE
//        DIMENSION_CARDINALITY
//        DEFAULT_HIERARCHY
//        DESCRIPTION
//        IS_VIRTUAL
//        IS_READWRITE
//        DIMENSION_UNIQUE_SETTINGS
//        DIMENSION_MASTER_UNIQUE_NAME
//        DIMENSION_IS_VISIBLE

    }

    /**
     * 获取SSAS结构
     *
     * @param catalogs
     * @throws Exception
     */
    public CubePO getModelStructure(String catalogs, String cube) throws Exception {
        Database metadata = connection.getOlapDatabase();
        NamedList<Catalog> Catalogs = metadata.getCatalogs();
        Catalog Catalog = Catalogs.get(catalogs);
        NamedList<Schema> Schemas = Catalog.getSchemas();
        //架构
        Schema Schema = Schemas.get("");
        String SchemaName = Schema.getName();
        NamedList<Cube> Cubes = Schema.getCubes();
        Cube Cube = Cubes.get(cube);
        NamedList<Dimension> Dimensions = Cube.getDimensions();
        CubePO cubePO=new  CubePO();
        cubePO.Name=Cube.getName();
        cubePO.UniqueName=Cube.getUniqueName();
        List<MeasurePO> measurePOList=new ArrayList<>();
        //度量值
        List<Measure> Measures = Cube.getMeasures();
        for (int m = 0; m < Measures.size(); m++) {
            MeasurePO measurePO=new MeasurePO();
            Measure Measure = Measures.get(m);
            measurePO.Name=Measure.getName();
            measurePO.UniqueName=Measure.getUniqueName();
            measurePOList.add(measurePO);
        }
        //维度
        List<DimensionPO> dimensionPOList=new ArrayList<>();
        for (int d = 0; d < Dimensions.size(); d++) {
            //维度
            Dimension Dimension = Dimensions.get(d);
            // MEASURE 为度量值 , OTHER 为维度
            Dimension.Type DimensionType = Dimension.getDimensionType();
            if (DimensionType == org.olap4j.metadata.Dimension.Type.OTHER) {
                DimensionPO dimensionPO=new DimensionPO();
                dimensionPO.Name = Dimension.getName();
                dimensionPO.UniqueName= Dimension.getUniqueName();
                List<HierarchyPO> hierarchyPOList=new ArrayList<>();
                NamedList<Hierarchy> Hierarchies = Dimension.getHierarchies();
                for (int h = 0; h < Hierarchies.size(); h++) {
                    //层级
                    HierarchyPO hierarchyPO=new HierarchyPO();
                    Hierarchy Hierarchy = Hierarchies.get(h);
                    hierarchyPO.Name = Hierarchy.getName();
                    hierarchyPO.UniqueName = Hierarchy.getUniqueName();
                    hierarchyPOList.add(hierarchyPO);
                }
                dimensionPO.Hierarchies= hierarchyPOList;
                dimensionPOList.add(dimensionPO);
            }
        }
        cubePO.Measures=measurePOList;
        cubePO.Dimensions=dimensionPOList;
        return cubePO;
    }
}
