package com.fisk.auth.test;

import com.alibaba.fastjson.JSON;
import com.fisk.auth.dto.ssologin.SSOResultEntityDTO;
import com.fisk.auth.dto.ssologin.TicketInfoDTO;
import com.fisk.auth.utils.HttpUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserNameTest {

    @Test
    public void singleLogin() {
        try {
            TicketInfoDTO ticketInfoDTO = new TicketInfoDTO();
            ticketInfoDTO.setTICKETID("2bf33c5b9ba04ad481e0afba1c1f7ce6");
            String param = JSON.toJSONString(ticketInfoDTO);
            //根据票据获取用户信息的地址
            String url1 = "http://10.220.105.60:8494/cityoutapi/login/getUserInfoByTicket";
            //获取
            String result = HttpUtils.HttpPost(url1, param);
            SSOResultEntityDTO dto = JSON.parseObject(result, SSOResultEntityDTO.class);
            long l = System.currentTimeMillis();
            System.out.println(l);
            System.out.println(dto);

        } catch (Exception e) {
            throw new FkException(ResultEnum.ERROR,e);
        }
    }

}
