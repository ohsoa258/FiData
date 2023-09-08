package com.fisk.dataaccess.webservice;

import com.fisk.dataaccess.webservice.entity.UserDTO;

public interface IUserServer {

    UserDTO getUser(Long str);
}
