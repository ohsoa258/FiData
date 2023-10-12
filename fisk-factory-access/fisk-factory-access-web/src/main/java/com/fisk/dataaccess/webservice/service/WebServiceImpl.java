package com.fisk.dataaccess.webservice.service;

import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.service.impl.ApiConfigImpl;
import com.fisk.dataaccess.service.impl.AppDataSourceImpl;
import com.fisk.dataaccess.webservice.IWebServiceServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.List;

/**
 * @author lsj
 * @date 2023-10-8 14:05:02
 * 该实现类用于提供webService客户端方式可调用的方法
 */
@Service
@WebService
@Slf4j
public class WebServiceImpl implements IWebServiceServer {

    @Resource
    private ApiConfigImpl apiConfig;
    @Resource
    private AuthClient authClient;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;

    /**
     * 简单测试
     *
     * @param id
     * @return
     */
    @Override
    @WebMethod
    public UserDTO getUser(@WebParam(name = "id") Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setAddress("上海市浦东新区");
        user.setAge(25);
        user.setName("gongj");
        return user;
    }

    /**
     * webService推送数据
     *
     * @param dataDTO
     * @return 执行结果
     */
    @Override
    @WebMethod()
    @WebResult(name = "result")
    public String webServicePushData(@WebParam(name = "dataDTO") WebServiceReceiveDataDTO dataDTO) {
        String token = dataDTO.getToken();
        if (token == null) {
            log.error("token为空，请先获取token");
            return "token为空，请先获取token";
        }
        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        receiveDataDTO.setApiCode(dataDTO.getWebServiceCode());
        receiveDataDTO.setPushData(dataDTO.getData());
        receiveDataDTO.setIfWebService(true);
        receiveDataDTO.setWebServiceToken(token);
        return apiConfig.webServicePushData(receiveDataDTO);
    }

    /**
     * 获取webService的临时token
     *
     * @param userDTO
     * @return 获取token结果
     */
    @Override
    @WebMethod
    @WebResult(name = "token")
    public String webServiceGetToken(@WebParam(name = "userDTO") WebServiceUserDTO userDTO) {

        // 根据账号名称查询对应的app_id下
        List<AppDataSourcePO> dataSourcePos =
                appDataSourceImpl.query().eq("realtime_account", userDTO.getUseraccount()).list();
        if (CollectionUtils.isEmpty(dataSourcePos)) {
            log.error("webServiceGetToken方法的账号或密码不正确或数据库中指定账号的realtime_account和realtime_pwd为空,请联系管理人员...");
            return "webServiceGetToken方法的账号或密码不正确或数据库中指定账号的realtime_account和realtime_pwd为空,请联系管理人员...";
        }
        AppDataSourcePO dataSourcePo = dataSourcePos.get(0);
        if (!dataSourcePo.realtimeAccount.equals(userDTO.getUseraccount()) || !dataSourcePo.realtimePwd.equals(userDTO.getPassword())) {
            log.error("请输入正确的账号或密码");
            return "请输入正确的账号或密码";
        }
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(userDTO.getUseraccount());
        userAuthDTO.setPassword(userDTO.getPassword());
        userAuthDTO.setTemporaryId(RedisTokenKey.DATA_ACCESS_TOKEN + dataSourcePo.id);

        ResultEntity<String> result = authClient.getToken(userAuthDTO);
        if (result.code == ResultEnum.SUCCESS.getCode()) {
            return result.data;
        } else {
            log.error("远程调用失败,方法名: 【auth-service:getToken】");
            return "远程调用失败,方法名: 【auth-service:getToken】";
        }
    }

}
