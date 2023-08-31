package com.fisk.auth.dto.ssologin;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author lsj
 */
@Data
public class SSOUserInfoDTO {

    /**
     * 用户编码
     */
    @JSONField(name = "U_ID")
    private String U_ID;

    /**
     * 姓名
     */
    @JSONField(name = "U_TRUENAME")
    private String U_TRUENAME;

    /**
     * 手机号
     */
    @JSONField(name = "U_MOBILE")
    private String U_MOBILE;

    /**
     * 部门编码
     */
    @JSONField(name = "D_ID")
    private String D_ID;

    /**
     * 部门名称
     */
    @JSONField(name = "D_NAME")
    private String D_NAME;

    /**
     * 角色编码集合
     */
    @JSONField(name = "ROLELIST")
    private List<String> ROLELIST;

}
