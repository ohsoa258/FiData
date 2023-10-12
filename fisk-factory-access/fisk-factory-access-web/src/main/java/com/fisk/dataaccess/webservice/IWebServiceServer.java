package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.service.UserDTO;
import com.fisk.dataaccess.webservice.service.WebServiceReceiveDataDTO;
import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该接口用于提供webService客户端方式可调用的方法--实现类的父接口
 */
public interface IWebServiceServer {

    /**
     * 简单测试
     *
     * @param str
     * @return
     */
    UserDTO getUser(Long str);

    /**
     * webService推送数据
     *
     * @param dto dto
     * @return 执行结果
     */
    String webServicePushData(WebServiceReceiveDataDTO dto);

    /**
     * 获取webService的临时token
     *
     * @param dto dto
     * @return 获取token结果
     */
    String webServiceGetToken(WebServiceUserDTO dto);


}
