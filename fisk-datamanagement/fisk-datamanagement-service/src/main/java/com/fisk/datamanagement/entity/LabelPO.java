package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_label")
@EqualsAndHashCode(callSuper = true)
public class LabelPO extends BasePO {
    /**
     * 标签中文名称
     */
    public String labelCnName;
    /**
     * 类目id
     */
    public int categoryId;
    /**
     * 标签英文名称
     */
    public String labelEnName;
    /**
     * 标签描述
     */
    public String labelDes;
    /**
     * 应用模块id集合
     */
    public String applicationModule;
}
