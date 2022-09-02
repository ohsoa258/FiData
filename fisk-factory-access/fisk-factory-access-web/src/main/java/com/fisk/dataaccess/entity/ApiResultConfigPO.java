package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_api_result_config")
@EqualsAndHashCode(callSuper = true)
public class ApiResultConfigPO extends BasePO {
    /**
     * app数据源id
     */
    public Long appDatasourceId;
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
