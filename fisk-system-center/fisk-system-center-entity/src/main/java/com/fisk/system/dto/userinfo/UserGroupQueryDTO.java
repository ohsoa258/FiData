package com.fisk.system.dto.userinfo;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class UserGroupQueryDTO {

    public List<Integer> userIdList;

    public int page;

    public int size;

    /**
     *角查询字段名称
     */
    public String name;


}
