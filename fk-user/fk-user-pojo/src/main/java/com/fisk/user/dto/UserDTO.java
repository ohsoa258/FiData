package com.fisk.user.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Lock
 * @data: 2021/5/14 17:37
 */
@Data
@NoArgsConstructor
// 此类是为了存储用户名和密码,并没有数据库中tb_user_info的其他属性,
// 所以要重写父类方法,只比较UserDTO中的具体中,不牵扯tb_user_info表的其他属性
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO {

    private Long id;
    private String username;

    public UserDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<UserDTO> convertEntityList(Collection<T> list){
        return list.stream().map(UserDTO::new).collect(Collectors.toList());
    }

}
