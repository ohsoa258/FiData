package com.fisk.datagovernance.entity.dataquality;

import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量PO
 * @date 2022/4/21 14:12
 */
@Data
public class DataQualityPO {
    /**
     * 模板PO
     */
    public TemplatePO templatePO;

    /**
     * 数据源PO
     */
    public DataSourceConPO dataSourceConPO;

    /**
     * 数据校验PO
     */
    public DataCheckPO dataCheckPO;

    /**
     * 业务清洗PO
     */
    public BusinessFilterPO businessFilterPO;

    /**
     * 生命周期PO
     */
    public LifecyclePO lifecyclePO;

    /**
     * 消息内容
     */
    public  String msgBody;
}
