package com.fisk.auth.dto;

import lombok.Data;

/**
 * @author Lock
 * @date 2021/5/17 10:52
 *
 * JWT的在恶化
 */
@Data
public class Payload {

    private long id;

    /**
     * jwt的唯一标识id
     */
    private String jti;

    /**
     * jwt的用户身份信息
     */
    private UserDetail userDetail;

}
