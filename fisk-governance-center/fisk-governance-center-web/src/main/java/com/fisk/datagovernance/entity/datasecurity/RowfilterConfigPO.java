package com.fisk.datagovernance.entity.datasecurity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-30 15:29:16
 */
@Data
@TableName("tb_rowfilter_config")
@EqualsAndHashCode(callSuper = true)
public class RowfilterConfigPO extends BasePO {

    /**
     * tb_rowsecurity_config表  id
     */
    public long rowsecurityId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 运算符:  > = < !=  like
     */
    public String operator;

    /**
     * 查询内容
     */
    public String result;

    /**
     * 查询的链式关系: and or
     */
    public String chainRelationship;

    /**
     * 创建人
     */
    public String createUser;

}
