package com.fisk.mdm;

import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description 测试补全用户信息
 * @date 2022/4/22 10:45
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ReplenishUserInfoTest {

    @Resource
    UserClient client;

    @Test
    public void test() {
        List<AttributeVO> data = new ArrayList<>();
        AttributeVO vo1 = new AttributeVO();
        vo1.createUser = "80";
        vo1.updateUser = "18";
        AttributeVO vo2 = new AttributeVO();
        vo2.createUser = "81";
        vo2.updateUser = "28";
        AttributeVO vo3 = new AttributeVO();
        vo3.createUser = null;
        vo3.updateUser = "38";
        AttributeVO vo4 = new AttributeVO();
        vo4.createUser = "80";
        vo4.updateUser = "48";
        data.add(vo1);
        data.add(vo2);
        data.add(vo3);
        data.add(vo4);
        ReplenishUserInfo.replenishUserName(data, client, UserFieldEnum.USER_NAME);
        data.stream().map(e -> e.createUser + "_" + e.updateUser).forEach(System.out::println);
    }
}
