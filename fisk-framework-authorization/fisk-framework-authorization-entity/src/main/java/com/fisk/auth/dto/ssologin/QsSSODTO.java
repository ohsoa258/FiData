package com.fisk.auth.dto.ssologin;

import lombok.Data;

/**
 * 强生交通获取accessToken的参数对象
 */
@Data
public class QsSSODTO {

    //客户端id
    private String clientId;
    //客户端密钥
    private String clientSecret;

}
