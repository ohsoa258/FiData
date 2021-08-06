package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_area_data")
@EqualsAndHashCode(callSuper = true)
public class DataAreaPO extends BasePO {

    /**
     *  业务域表id
     */
    public long businessId;

    /**
     *  数据域名称
     */
    public String dataName;

    /**
     *  1true  0false
     */
    public boolean share;

    /**
     *  数据域描述
     */
    public String dataDes;
}
