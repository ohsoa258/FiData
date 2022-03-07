package com.fisk.auth.dto.clientregister;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.auth.vo.ClientRegisterVO;
import com.fisk.common.filter.dto.FilterQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 筛选器查询条件
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
@Data
public class ClientRegisterQueryDTO {

    /**
     * 查询字段值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<ClientRegisterVO> page;
}
