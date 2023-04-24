package com.fisk.dataaccess.dto.table;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.TableAccessVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableAccessPageDTO {
    @ApiModelProperty(value = "where")
    public String where;
    @ApiModelProperty(value = "分页")
    public Page<TableAccessVO> page;
}
