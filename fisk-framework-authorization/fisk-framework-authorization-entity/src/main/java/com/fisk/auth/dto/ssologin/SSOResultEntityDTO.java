package com.fisk.auth.dto.ssologin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author lsj
 */
@Data
public class SSOResultEntityDTO {

    /**
     * 返回信息
     */
    @JSONField(name = "MSG")
    private String MSG;

    /**
     * 返回码
     */
    @JSONField(name = "CODE")
    private String CODE;

    /**
     * 用户信息
     */
    @JSONField(name = "DATA")
    private SSOUserInfoDTO DATA;


}
