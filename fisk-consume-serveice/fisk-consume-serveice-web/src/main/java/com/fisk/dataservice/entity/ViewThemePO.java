package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:58
 * @description
 */
@Data
@TableName("tb_view_theme")
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ViewThemePO extends BasePO {
    private String themeName;
    private String themeAbbr;
    private String themeDesc;
    private String whetherSchema;
    private String themePrincipal;
    private String themePrincipalEmail;
    private long targetDbId;
}
