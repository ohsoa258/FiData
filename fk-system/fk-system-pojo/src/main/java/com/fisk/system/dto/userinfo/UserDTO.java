package com.fisk.system.dto.userinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.time.LocalDateTime;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date createTime;

    public String createUser;
    /**
     * 是否有效
     */
    public boolean valid;

}
