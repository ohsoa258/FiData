package com.fisk.dataservice.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.DownSystemQueryVO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/12/9 11:03
 */
@Data
public class DownSystemPageDTO {

    public String where;
    public Page<DownSystemQueryVO> page;
}
