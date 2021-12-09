package com.fisk.dataservice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author WangYan
 * @date 2021/12/9 10:49
 */
@Data
public class DownSystemQueryVO {

    public long id;
    /**
     * 下游系统名称
     */
    public String downSystemName;
    /**
     * 描述
     */
    public String systemInfo;
    /**
     * 账号(用户名）
     */
    public String userName;
    /**
     * 密码
     */
    public String password;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;
}
