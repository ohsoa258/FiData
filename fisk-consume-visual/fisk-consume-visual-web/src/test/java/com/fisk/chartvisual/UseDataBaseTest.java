package com.fisk.chartvisual;

import com.fisk.chartvisual.dto.chartvisual.ChartQueryObject;
import com.fisk.chartvisual.dto.chartvisual.ColumnDetails;
import com.fisk.chartvisual.dto.datasource.DataSourceConDTO;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.service.IDataSourceConManageService;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.core.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.core.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UseDataBaseTest {

    private final DataSourceConDTO dto = new DataSourceConDTO() {{
        conType = DataSourceTypeEnum.MYSQL;
        conAccount = "root";
        conPassword = "root123";
        conStr = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
        conDbname = "dmp_chartvisual_db";
    }};

    @Resource
    IDataSourceConManageService con;
    @Resource
    IDataService use;

    /**
     * 测试数据库连接
     */
    @Test
    public void testConnection() {
        //assert con.testConnection(dto) == ResultEnum.SUCCESS;
    }

    /**
     * mysql 测试获取数据域
     */
    @Test
    public void getDataDomainMysql() {
       // ResultEntity<Object> data = con.listDataDomain(1);
       // System.out.println(data.toString());
        //assert data.data.size() > 0;
    }

    @Test
    public void getServiceDataMysql() {
        ChartQueryObject query = new ChartQueryObject() {{
            id = 1;
            tableName = "tb_test_data";
            columnDetails = new ArrayList<ColumnDetails>() {{
                add(new ColumnDetails() {{
                    columnName = "name";
                    columnType = ColumnTypeEnum.NAME;
                }});
                add(new ColumnDetails() {{
                    columnName = "value";
                    columnType = ColumnTypeEnum.VALUE;
                    aggregationType = AggregationTypeEnum.COUNT;
                }});
            }};
        }};
        DataServiceResult data = use.query(query);
        System.out.println(data.toString());
        assert data.data.size() > 0;
    }

    /**
     * sqlserver 测试获取数据域
     */
    @Test
    public void getDataDomainSqlServer() {
     //   ResultEntity<Object> data = con.listDataDomain(2);
       // System.out.println(data.toString());
       // assert data.data.size() > 0;
    }

    @Test
    public void getServiceDataSqlServer() {
        ChartQueryObject query = new ChartQueryObject() {{
            id = 2;
            tableName = "Tb_WorkOrderDetails";
            columnDetails = new ArrayList<ColumnDetails>() {{
                add(new ColumnDetails() {{
                    columnName = "SubTypeName";
                    columnType = ColumnTypeEnum.NAME;
                }});
                add(new ColumnDetails() {{
                    columnName = "ID";
                    columnType = ColumnTypeEnum.VALUE;
                    aggregationType = AggregationTypeEnum.COUNT;
                }});
            }};
        }};
        DataServiceResult data = use.query(query);
        System.out.println(data.toString());
        assert data.data.size() > 0;
    }
}
