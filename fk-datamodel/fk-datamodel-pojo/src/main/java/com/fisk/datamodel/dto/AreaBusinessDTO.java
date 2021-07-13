package com.fisk.datamodel.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AreaBusinessDTO{

    /**
     * 主键
     */
    public long id;

    /**
     * 业务域名称
     */
    public String businessName;

    /**
     * 业务域描述
     */
    public String businessDes;

    /**
     * 业务需求管理员
     */
    public String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    public String businessEmail;

}
