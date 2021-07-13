package com.fisk.system.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
public class UserDTO {

    public Long id;

    public String email;

    public String userAccount;

    public String username;

    public String password;

    public Date createTime;

    public String createUser;

}
