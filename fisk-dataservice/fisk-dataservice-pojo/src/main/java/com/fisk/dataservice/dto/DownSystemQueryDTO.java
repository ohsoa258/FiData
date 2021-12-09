package com.fisk.dataservice.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import com.fisk.dataservice.vo.DownSystemQueryVO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/12/9 10:28
 */
@Data
public class DownSystemQueryDTO {

    /**
     * 查询字段值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<DownSystemQueryVO> page;
}
