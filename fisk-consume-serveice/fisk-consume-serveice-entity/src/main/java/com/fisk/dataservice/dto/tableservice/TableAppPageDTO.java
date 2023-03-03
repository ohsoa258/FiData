package com.fisk.dataservice.dto.tableservice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableAppPageDTO {
    /**
     * 条件
     */
    @ApiModelProperty(value = "条件")
    public String where;

    /**
     * 分页
     */
    @ApiModelProperty(value = "分页")
    public Page<TableAppVO> page;
}
