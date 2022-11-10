package com.fisk.common.framework.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lock
 * @date 2021/5/17 10:55
 * <p>
 * 用户数据,jwt载荷数据的一部分
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserDetail {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名
     */
    private String userAccount;
}
