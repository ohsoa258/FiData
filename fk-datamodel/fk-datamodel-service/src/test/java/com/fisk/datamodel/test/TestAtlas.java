package com.fisk.datamodel.test;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
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

        ResultEntity<AtlasEntityDTO> dtoResultEntity = client.getAtlasEntity(6);
        AtlasEntityDTO dto = dtoResultEntity.getData();

        System.out.println(dto);
    }

    @Test
    public void test01() {

        ResultEntity<AtlasEntityDbTableColumnDTO> dto = client.getAtlasBuildTableAndColumn(1, 6);
        System.out.println(dto.getData());
    }

    @Test
    public void test02() {

        ResultEntity<AtlasEntityDbTableColumnDTO> dto = client.getAtlasBuildTableAndColumn(740, 6);
        System.out.println(dto);

    }

}
