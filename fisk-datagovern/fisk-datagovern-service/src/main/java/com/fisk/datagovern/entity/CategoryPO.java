package com.fisk.datagovern.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_category")
@EqualsAndHashCode(callSuper = true)
public class CategoryPO extends BasePO {
    /**
     * 类目code
     */
    public String categoryCode;
    /**
     * 父级类目code
     */
    public String categoryParentCode;
    /**
     * 类目中文名称
     */
    public String categoryCnName;
    /**
     * 类目英文名称
     */
    public String categoryEnName;
}
