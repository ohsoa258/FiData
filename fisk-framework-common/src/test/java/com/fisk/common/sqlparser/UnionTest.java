package com.fisk.common.sqlparser;

import com.alibaba.druid.DbType;
import com.fisk.common.service.sqlparser.ISqlParser;
import com.fisk.common.service.sqlparser.ParserVersion;
import com.fisk.common.service.sqlparser.SqlParserFactory;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description TODO
 * @date 2022/12/7 10:10
 */
@RunWith(Parameterized.class)
public class UnionTest {
    private final String sql;

    public UnionTest(String sql) {
        this.sql = sql;
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"select * from a"},
                {"select * from (select * from b union all select * from c) as a"},
                {"select * from (select * from b union all select * from (select * from d) as c) as a"},
                {"select * from (select * from (select * from e) as b union all select * from (select * from d) as c) as a"},
                {"select * from (select * from (select * from (select id, name from f union all select ID, Name from g) as e) as b union all select * from (select * from d) as c) as a"}
        });
    }

    @Test
    public void test() throws Exception {
        ISqlParser parser = SqlParserFactory.parser(ParserVersion.V1);
        List<TableMetaDataObject> res = parser.getDataTableBySql(sql, DbType.sqlserver);
        res.forEach(System.out::println);
    }
}
