package com.fisk.dataaccess.webservice.service;

import com.fisk.dataaccess.webservice.IUserServer;
import com.fisk.dataaccess.webservice.entity.UserDTO;
import org.springframework.stereotype.Service;

import javax.jws.WebService;

@Service
@WebService
public class UserServerImpl implements IUserServer {
    @Override
    public UserDTO getUser(Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setAddress("上海市浦东新区");
        user.setAge(25);
        user.setName("gongj");
        return user;
    }
}
