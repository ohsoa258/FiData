package com.fisk.dataaccess.test;

import com.fisk.dataaccess.vo.TableNameVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ListContainsTest {

    @Test
    public void test() {
        TableNameVO nameVO1 = new TableNameVO();
        TableNameVO nameVO2 = new TableNameVO();
        TableNameVO nameVO3 = new TableNameVO();
        TableNameVO nameVO4 = new TableNameVO();
        List<TableNameVO> list = new ArrayList<>();

        nameVO1.id = 1;
        nameVO1.tableName = "a";
        nameVO2.id = 2;
        nameVO2.tableName = "b";
        nameVO3.id = 3;
        nameVO3.tableName = "c";
        nameVO4.id = 1;
        nameVO4.tableName = "a";

        list.add(nameVO1);
        list.add(nameVO2);
        list.add(nameVO3);

        boolean contains = list.contains(nameVO4);
        System.out.println(contains);
    }
}
