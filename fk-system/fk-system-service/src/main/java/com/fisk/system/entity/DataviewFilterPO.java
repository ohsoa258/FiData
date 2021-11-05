package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/2 17:52
 */
@TableName("tb_dataview_filtercondition")
@Data
public class DataviewFilterPO extends BasePO {

    /**
     * 数据视图Id
     */
    private Integer dataviewId;
    /**
     * 字段
     */
    private String field;
    /**
     * 运算符
     */
    private String operator;
    /**
     * 内容
     */
    private String result;
}
