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
public class JoinTest {
    private final String sql;

    public JoinTest(String sql) {
        this.sql = sql;
    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"select a.*, b.* from a left join b on a.id = b.id"},
                {"select a.id, b.name, a.age, b.sex from a left join b on a.id = b.id"},
                {"select a.id, b.name, a.age, b.sex from (select id, age from c) as a left join b on a.id = b.id"},
                {"select a.id, b.name, a.age, b.sex from (select id, age from c) as a left join (select name, sex from d) as b on a.id = b.id"},
                {"select a.id, b.name, a.age, b.sex from (select c.id, c.age from c union all select e.id, e.age from e) as a left join (select d.name, f.sex from d left join f on d.id = f.id) as b on a.id = b.id"},
                {"select a.id, b.name, c.age, d.sex from  a left join b on a.id = b.id left join c on a.id2 = c.id left join d on a.id3 = d.id"}
        });
    }

    @Test
    public void test() throws Exception {
        ISqlParser parser = SqlParserFactory.parser(ParserVersion.V1);
        List<TableMetaDataObject> res = parser.getDataTableBySql(sql, DbType.sqlserver);
        res.forEach(System.out::println);
    }
}
