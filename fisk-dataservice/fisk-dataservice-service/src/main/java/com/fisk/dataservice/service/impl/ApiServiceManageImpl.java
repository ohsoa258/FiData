package com.fisk.dataservice.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.service.IApiServiceManageService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * api服务接口实现类
 *
 * @author dick
 */
@Service
public class ApiServiceManageImpl implements IApiServiceManageService {

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        return null;
    }

    @Override
    public ResultEntity<Object> getData(String token) {
        return null;
    }
}
