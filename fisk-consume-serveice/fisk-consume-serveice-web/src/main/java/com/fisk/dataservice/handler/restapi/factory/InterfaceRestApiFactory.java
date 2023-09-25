package com.fisk.dataservice.handler.restapi.factory;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.enums.AuthenticationTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.handler.restapi.impl.*;

/**
 * @Author: wangjian
 * @Date: 2023-09-14
 * @Description:
 */
public class InterfaceRestApiFactory {

        public static RestApiHandler getRestApiHandlerByType(AuthenticationTypeEnum authenticationTypeEnum) {
            switch (authenticationTypeEnum) {
                case BASIC_VALIDATION:
                    return new BasicValidation();
                case JWT_VALIDATION:
                    return new JwtValidation();
                case BEARER_TOKEN_VALIDATION:
                    return new BearerTokenValidation();
                case OAUTH_TWO_VALIDATION:
                    return new OauthTwoValidation();
                case API_KEY_VALIDATION:
                    return new ApiKeyValidation();
                case NONE_VALIDATION:
                    return new NoneValidation();
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
        }
}
