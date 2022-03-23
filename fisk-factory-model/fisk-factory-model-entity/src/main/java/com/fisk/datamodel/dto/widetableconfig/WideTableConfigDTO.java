package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class WideTableConfigDTO {

    public long id;
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 宽表名称
     */
    public String name;
    /**
     * 宽表sql脚本
     */
    public String sqlScript;
    /**
     * 配置详情
     */
    public String configDetails;

}
