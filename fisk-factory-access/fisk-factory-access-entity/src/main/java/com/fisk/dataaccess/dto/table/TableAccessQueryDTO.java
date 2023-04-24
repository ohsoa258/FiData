package com.fisk.dataaccess.dto.table;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.dataaccess.vo.TableAccessVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class TableAccessQueryDTO {

    /**
     * 查询字段值
     */
    @ApiModelProperty(value = "key")
    public String key;
    @ApiModelProperty(value = "dto")
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    @ApiModelProperty(value = "分页,返回给前端的数据对象")
    public Page<TableAccessVO> page;
}
