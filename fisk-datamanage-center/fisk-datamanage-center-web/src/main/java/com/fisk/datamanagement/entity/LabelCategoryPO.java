package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 属性分类
 * @author JianWenYang
 */
@Data
@TableName("tb_category")
@EqualsAndHashCode(callSuper = true)
public class LabelCategoryPO extends BasePO {
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
