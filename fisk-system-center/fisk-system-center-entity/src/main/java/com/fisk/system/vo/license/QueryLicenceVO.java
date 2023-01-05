package com.fisk.system.vo.license;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 查询许可证VO
 * @date 2023/1/5 10:45
 */
@Data
public class QueryLicenceVO {

    /**
     * 许可证列表
     */
    @ApiModelProperty(value = "许可证列表")
    public List<LicenceVO> licenceList;

    /**
     * 菜单列表
     */
    @ApiModelProperty(value = "菜单列表")
    public List<MenuVO> menuList;
}
