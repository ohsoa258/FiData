package com.fisk.dataservice.handler.restapi.impl;

import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import org.springframework.stereotype.Component;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
@Component
public class OauthTwoValidation extends RestApiHandler {

    @Override
    public ApiResultDTO sendHttpPost(TableAppPO tableAppPO, TableApiServicePO tableApiServicePO, String body, Boolean flag) {
        return null;
    }
}
