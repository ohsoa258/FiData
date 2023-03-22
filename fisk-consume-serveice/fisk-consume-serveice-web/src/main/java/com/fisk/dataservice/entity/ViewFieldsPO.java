package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 13:07
 * @description
 */
@Data
@TableName("tb_view_fields")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ViewFieldsPO extends BasePO {
    private long   viewId;
    private String fieldName;
    private String fieldType;
    private String fieldDesc;
    private String fieldLength;
    private long fieldPrecision;
    private String showName;
    private String dataType;
}
