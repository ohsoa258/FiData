package com.fisk.auth.dto.clientregister;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.auth.vo.ClientRegisterVO;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/3/7 17:45
 */
@Data
public class ClientRegisterPageDTO {
    public String where;
    public Page<ClientRegisterVO> page;
}
