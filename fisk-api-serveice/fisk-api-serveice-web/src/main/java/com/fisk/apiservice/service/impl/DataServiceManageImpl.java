package com.fisk.apiservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.apiservice.dto.dataservice.RequstDTO;
import com.fisk.apiservice.dto.dataservice.TokenDTO;
import com.fisk.apiservice.service.IDataServiceManageService;
import com.fisk.apiservice.utils.HttpUtils;
import com.fisk.apiservice.vo.dataservice.DataResponseBasicVO;
import com.fisk.apiservice.vo.dataservice.DataResponseVO;
import com.fisk.apiservice.vo.dataservice.TokenResponseVO;
import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class DataServiceManageImpl implements IDataServiceManageService {

    @Value("${dataservice.api.api_address}")
    private String api_address;

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        long startTime = System.currentTimeMillis();
        String logResult = "获取数据服务token，开始时间（毫秒）：" + startTime + "\n";

        String url = api_address + "/apiService/getToken";
        String getTokenParams = JSONObject.toJSONString(dto);
        TokenResponseVO getTokenResponse = HttpUtils.sendPostWebRequest(TokenResponseVO.class,
                url, getTokenParams, null);

        ResultEnum code = ResultEnum.BAD_REQUEST;
        String token = "";
        String msg = "";
        if (getTokenResponse != null) {
            code = ResultEnum.getEnum(getTokenResponse.getCode());
            token = getTokenResponse.getData();
            msg = getTokenResponse.getMsg();
        }

        long endTime = System.currentTimeMillis();
        logResult += "获取数据服务token，结束时间（毫秒）：" + endTime + "\n";
        logResult += "获取数据服务token，运行总时长：" + (endTime - startTime) + "ms\n";
        log.info("获取数据服务token日志信息：" + logResult);

        return ResultEntityBuild.buildData(code, token);
    }

    @Override
    public ResultEntity<Object> getData(RequstDTO dto) {
        long startTime = System.currentTimeMillis();
        String logResult = "获取数据服务业务数据，开始时间（毫秒）：" + startTime + "\n";

        // 获取请求属性集合
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            log.error("获取请求中token失败，错误原因：RequestAttributes为null");
            throw new FkException(ResultEnum.NOTFOUND_REQUEST_ATTR);
        }
        // 获取request对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取token
        String token = request.getHeader(SystemConstants.HTTP_HEADER_AUTH);
        if (StringUtils.isEmpty(token)) {
            log.error("获取请求中token失败，错误原因：请求头中缺少token");
            throw new FkException(ResultEnum.UNAUTHENTICATE, ResultEnum.AUTH_TOKEN_IS_NOTNULL.getMsg());
        }
        String getDataParams = JSONObject.toJSONString(dto);
        String url = api_address + "/apiService/getData";
        DataResponseBasicVO getDataResponse = HttpUtils.sendPostWebRequest(DataResponseBasicVO.class,
                url, getDataParams, token);

        ResultEnum code = ResultEnum.BAD_REQUEST;
        DataResponseVO data = new DataResponseVO();
        String msg = "";
        if (getDataResponse != null) {
            code = ResultEnum.getEnum(getDataResponse.getCode());
            data = getDataResponse.getData();
            msg = getDataResponse.getMsg();
        }

        long endTime = System.currentTimeMillis();
        logResult += "获取数据服务业务数据，结束时间（毫秒）：" + endTime + "\n";
        logResult += "获取数据服务业务数据，运行总时长：" + (endTime - startTime) + "ms\n";
        log.info("获取数据服务业务数据日志信息：" + logResult);

        return ResultEntityBuild.buildData(code, data);
    }
}
