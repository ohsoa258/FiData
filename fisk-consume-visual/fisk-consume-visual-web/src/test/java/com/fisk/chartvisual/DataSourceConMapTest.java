package com.fisk.chartvisual;

import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataSourceConMapTest {

    @Test
    public void dtoToPoTest(){
        DataSourceConDTO dto = new DataSourceConDTO(){{
            conStr = "test";
            conAccount = "admin";
            conPassword = "admin";
            conType = DataSourceTypeEnum.SQLSERVER;
        }};
        DataSourceConPO model = DataSourceConMap.INSTANCES.dtoToPo(dto);

        System.out.println(model.toString());

        assert model.conPassword.equals("admin") && model.conAccount.equals("admin");
    }

}
