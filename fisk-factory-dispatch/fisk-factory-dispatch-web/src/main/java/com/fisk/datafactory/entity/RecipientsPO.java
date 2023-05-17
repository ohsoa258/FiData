package com.fisk.datafactory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.util.List;

@Data
@TableName("tb_recipients")
public class RecipientsPO extends BasePO {

    /**
     * 发送邮箱id
     */
    public int dispatchEmailId;
    /*
    * 企业微信用户Id
    * */
    public String wechatUserId;
    /*
     * 企业微信用户名称
     * */
    public String wechatUserName;
    /*
    * 本地用户Id
    * */
    public String userId;
    /*
     * 本地用户名称
     * */
    public String userName;
}
