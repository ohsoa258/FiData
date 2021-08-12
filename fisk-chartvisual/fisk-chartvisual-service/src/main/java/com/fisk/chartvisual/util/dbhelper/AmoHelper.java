package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.entity.CubePO;
import com.fisk.chartvisual.entity.DimensionPO;
import com.fisk.chartvisual.entity.HierarchyPO;
import com.fisk.chartvisual.entity.MeasurePO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.olap4j.*;
import org.olap4j.metadata.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        cubePo.Name=cube.getName();
        cubePo.UniqueName=cube.getUniqueName();
        List<MeasurePO> measurePoList=new ArrayList<>();
        //度量值
        List<Measure> measures = cube.getMeasures();
        for (Measure measure:measures) {
            MeasurePO measurePo=new MeasurePO();
            measurePo.Name=measure.getName();
            measurePo.UniqueName=measure.getUniqueName();
            measurePoList.add(measurePo);
        }
        //维度
        List<DimensionPO> dimensionPoList=new ArrayList<>();
        for (Dimension dimension: dimensions ) {
            // MEASURE 为度量值 , OTHER 为维度
            Dimension.Type dimensionType = dimension.getDimensionType();
            if (dimensionType == org.olap4j.metadata.Dimension.Type.OTHER) {
                DimensionPO dimensionPo=new DimensionPO();
                dimensionPo.Name = dimension.getName();
                dimensionPo.UniqueName= dimension.getUniqueName();
                List<HierarchyPO> hierarchyPoList=new ArrayList<>();
                NamedList<Hierarchy> hierarchies = dimension.getHierarchies();
                for (Hierarchy  hierarchy: hierarchies) {
                    //层级
                    HierarchyPO hierarchyPo=new HierarchyPO();
                    hierarchyPo.Name = hierarchy.getName();
                    hierarchyPo.UniqueName = hierarchy.getUniqueName();
                    hierarchyPoList.add(hierarchyPo);
                }
                dimensionPo.Hierarchies= hierarchyPoList;
                dimensionPoList.add(dimensionPo);
            }
        }
        cubePo.Measures=measurePoList;
        cubePo.Dimensions=dimensionPoList;
        return cubePo;
    }
}
