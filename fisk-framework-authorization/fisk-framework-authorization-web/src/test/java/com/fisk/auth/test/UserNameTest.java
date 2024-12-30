package com.fisk.auth.test;

import com.alibaba.fastjson.JSON;
import com.fisk.auth.dto.ssologin.SSOResultEntityDTO;
import com.fisk.auth.dto.ssologin.TicketInfoDTO;
import com.fisk.auth.service.UserAuthService;
import com.fisk.auth.utils.HttpUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
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
public class UserNameTest {
    @Resource
    private UserAuthService userAuthService;

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
    @Test
    public void Test() {
        String code = "1.AXEAFMNuzQZ11k60bmgUQ8oY441h9IQ2g-tDnvPi7IXzmAZxAJVxAA.AgABBAIAAADW6jl31mB3T7ugrWTT8pFeAwDs_wUA9P-xdL2SGTGe1ZwYUrhdytAcAxa8j2TkCxB0G67C2JXU0lrnBRISfZcnuYaBqfqpkk9k-htaiyQRHDOK1_GflWxig1xQknybtXp746stXYTcj5YaGD1iUW6B5vIlbX_jvKCa_jdmN6EESSuEXm52hLoOlJOcec2eiC2ZY2goOPcEZjb1kLKa_n06bbicKmE7dNBJTs935J6UahjEyA26PPrSu6RSMNGCmSWQuWoubXszdEogNAE7v4wqPX7Rhfo_o0DNKCGRczRq5UQ1YV9VhCCUGnwrbkWjMuADZbAZZRaWEl1elP2ukqRjQmAEqUq2_qY9kgU7mgpRDE7emKcWN6a07AgFezBqciR8sZ8xd7n84sH8FXnVTCLjof67M_TbLgbmaP6TI9sZI22mQvyf1PQzzH3lIbolvpAsdvxEDWHT3RHQdYVEAzIvIuOVDD8mAW7R9XPJ_QwuQNNr2b11xaZ6TIb1oNfzJaxOAIf18mVBBwYbNt5FurJ9ha6dDXiADMi0B9p3Y_zJPKEMXSCvRx3_gmEp-ruJbKQw3R6uarsHH5dpfpnzbUoCecMiFlPH4fiyobT1LkVnJGZj5Hcm25I7WKvbZQSQbT1HvVKshCFrArKjr8bmcqJirTQLmnb6vGzHXzegx5yfIVIKBI-vN0H7p8BYJQbpl932_CLZeH2EJDO8wCk01mmsqnXGHqJ-9sGwVIrCEBt3bddv0k8V-2FxrSrl07iqY2MoAlXApiYPRGo";
        userAuthService.azureAdLogin(code);
    }

}
