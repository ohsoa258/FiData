package com.fisk.dataaccess.dto.apiresultconfig;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiResultConfigDTO {
    /**
     * app数据源id
     */
    public Integer appDatasourceId;
    /**
     * 节点名称
     */
    public String name;
    /**
     * 父级名称
     */
    public String parent;
    /**
     * 是否选中
     */
    public Boolean checked;

}
