package com.fisk.dataaccess.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.TableAccessVO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableAccessPageDTO {
    public String where;
    public Page<TableAccessVO> page;
}
