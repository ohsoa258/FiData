package com.fisk.datamodel.test;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.NifiAccessDTO;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestAtlas {

    @Resource
    DataAccessClient client;

    @Test
    public void test() {

        ResultEntity<AtlasEntityDTO> dtoResultEntity = client.getAtlasEntity(1);
        AtlasEntityDTO dto = dtoResultEntity.getData();

        System.out.println(dto);
    }

    @Test
    public void test01() {

        ResultEntity<AtlasEntityDbTableColumnDTO> dto = client.getAtlasBuildTableAndColumn(1035, 89);
        System.out.println(dto.getData());
    }

    @Test
    public void test02() {

        ResultEntity<AtlasEntityDbTableColumnDTO> dto = client.getAtlasBuildTableAndColumn(740, 6);
        System.out.println(dto);
    }

    @Test
    public void test03() {
        ResultEntity<DataAccessConfigDTO> dto = client.dataAccessConfig(1028, 89);
        System.out.println(dto);
    }

    @Test
    public void test04() {

        NifiAccessDTO dto = new NifiAccessDTO();
        dto.appid = 325;
        dto.tableId = 940;
        dto.appGroupId = "bd881358-017a-1000-d6f4-e715c2cf2641";
        dto.tableGroupId = "bd8813bf-017a-1000-104d-882f0d4aba32";
        dto.targetDbPoolComponentId = "1111-0000";
        dto.sourceDbPoolComponentId = "0000-1111";

        ResultEntity<Object> result = client.addComponentId(dto);
        System.out.println(result);
    }

}
