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
 * @createTime 2023-03-22 13:05
 * @description
 */
@Data
@TableName("tb_view")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ViewPO extends BasePO {
    private long viewThemeId;
    private String name;
    private String displayName;
    private String viewDesc;
    private String viewScript;
}
