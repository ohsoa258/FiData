package com.fisk.license.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.utils.LicenseEnCryptUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.license.dto.AddLicenceDTO;
import com.fisk.license.service.ILicenseService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.ServiceRegistryDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description License测试类
 * @date 2022/11/9 13:20
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LicenseTest {

    @Resource
    private ILicenseService service;

    @Resource
    private UserClient userClient;

    /**
     * @return void
     * @description 生成Licence
     * @author dick
     * @date 2022/11/10 17:58
     * @version v1.0
     * @params
     */
    @Test
    public void createCompanyLicence() {
        AddLicenceDTO dto = new AddLicenceDTO();
        dto.setPlatform("FiData-Dev");
        //白泽本地环境mac地址：02-50-41-00-00-01
        //白泽开发环境mac地址：02:42:e4:32:0d:fd
        //白泽测试环境mac地址：02:42:7d:cc:ae:74
        //白泽演示环境mac地址：00:0c:29:c1:60:18
        dto.setAuthorizer("admin");
        dto.setMac("02:42:e4:32:0d:fd");
        dto.setMenus(getMenus());
        dto.setExpireTime("2050/01/01");
        ResultEntity<String> companyLicence = service.createCompanyLicence(dto);
        String data = companyLicence.getData();
        System.out.println("Licence：" + data);
    }

    public List<String> getMenus() {
        // 查询菜单url
        List<String> menuNameList = new ArrayList<>();
        ResultEntity<Object> clientMenuList = userClient.getMenuList();
        String json = JSONArray.toJSON(clientMenuList.getData()).toString();
        List<ServiceRegistryDTO> menuList = JSONArray.parseArray(json, ServiceRegistryDTO.class);
        menuList.forEach(t -> {
            if (CollectionUtils.isNotEmpty(t.getDtos())) {
                t.getDtos().forEach(next -> {
                    String menuAddress = String.format("/%s/%s", t.getServeUrl(), next.getServeUrl());
                    menuNameList.add(menuAddress);
                });
            }
        });
        return menuNameList;
    }
}
