package com.fisk.chartvisual;

import com.fisk.chartvisual.dto.ChartQueryFilter;
import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ColumnDetails;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.enums.chartvisual.AggregationTypeEnum;
import com.fisk.common.enums.chartvisual.ColumnTypeEnum;
import com.fisk.common.enums.chartvisual.InteractiveTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 测试报表数据获取
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ChartDataTest {

    @Resource
    IDataService dbService;

    /**
     * 单x轴y轴，无交互操作
     */
    @Test
    public void default_SingleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.DEFAULT;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴,多y轴，无交互操作
     */
    @Test
    public void default_MultipleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.DEFAULT;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "id";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴y轴，下钻
     */
    @Test
    public void Drill_SingleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.DRILL;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubTye";
                columnName = "sub_type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        query.queryFilters = new ArrayList<ChartQueryFilter>() {{
            add(new ChartQueryFilter() {{
                columnName = "type";
                value = "智能发现";
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴，多y轴，下钻
     */
    @Test
    public void Drill_MultipleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.DRILL;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubTye";
                columnName = "sub_type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "id";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        query.queryFilters = new ArrayList<ChartQueryFilter>() {{
            add(new ChartQueryFilter() {{
                columnName = "type";
                value = "智能发现";
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴y轴，平铺
     */
    @Test
    public void Linkage_SingleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.LINKAGE;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubTye";
                columnName = "sub_type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴，多y轴，平铺
     */
    @Test
    public void Linkage_MultipleValue_Mysql() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 1;
        query.interactiveType = InteractiveTypeEnum.LINKAGE;
        query.tableName = "tb_test_data";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Type";
                columnName = "type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubTye";
                columnName = "sub_type";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "id";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "value";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /*-----------------------------------------------*/

    /**
     * 单x轴y轴，无交互操作
     */
    @Test
    public void default_SingleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.DEFAULT;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴,多y轴，无交互操作
     */
    @Test
    public void default_MultipleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.DEFAULT;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴y轴，下钻
     */
    @Test
    public void Drill_SingleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.DRILL;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubName";
                columnName = "SubTypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
        }};
        query.queryFilters = new ArrayList<ChartQueryFilter>() {{
            add(new ChartQueryFilter() {{
                columnName = "TypeName";
                value = "主动上报";
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴，多y轴，下钻
     */
    @Test
    public void Drill_MultipleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.DRILL;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubName";
                columnName = "SubTypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Values";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        query.queryFilters = new ArrayList<ChartQueryFilter>() {{
            add(new ChartQueryFilter() {{
                columnName = "TypeName";
                value = "主动上报";
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴y轴，平铺
     */
    @Test
    public void Linkage_SingleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.LINKAGE;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubName";
                columnName = "SubTypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }

    /**
     * 单x轴，多y轴，平铺
     */
    @Test
    public void Linkage_MultipleValue_SqlServer() {
        ChartQueryObject query = new ChartQueryObject();
        query.id = 2;
        query.interactiveType = InteractiveTypeEnum.LINKAGE;
        query.tableName = "Tb_WorkOrderDetails";
        query.columnDetails = new ArrayList<ColumnDetails>() {{
            add(new ColumnDetails() {{
                columnLabel = "Name";
                columnName = "TypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "SubName";
                columnName = "SubTypeName";
                columnType = ColumnTypeEnum.NAME;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.COUNT;
            }});
            add(new ColumnDetails() {{
                columnLabel = "Counts";
                columnName = "ID";
                columnType = ColumnTypeEnum.VALUE;
                aggregationType = AggregationTypeEnum.SUM;
            }});
        }};
        DataServiceResult res = dbService.query(query);
        System.out.println(res.toString());
        assert res.data.size() > 0;
    }
}
