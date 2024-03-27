package com.fisk.dataservice.dto.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.api.ApiConfigVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @author dick
 * @version v1.0
 * @description API查询 DTO
 * @date 2022/1/6 14:51
 */
@Data
public class ApiRegisterQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 创建api类型
     */
    @ApiModelProperty(value = "创建api类型：1 创建新api 2 使用现有api 3 代理API")
    public Integer createApiType;

    /**
     * 参数类型 1:api信息 2:参数信息
     */
    @ApiModelProperty(value = "参数类型 1:api信息 2:参数信息")
    public Integer apiParamType;
    /**
     * menuId
     */
    @ApiModelProperty(value = "menuId")
    public Integer menuId;

    /**
     * menuIds
     */
    @ApiModelProperty(value = "menuIds")
    public List<String> menuIds;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<ApiConfigVO> page;
}
