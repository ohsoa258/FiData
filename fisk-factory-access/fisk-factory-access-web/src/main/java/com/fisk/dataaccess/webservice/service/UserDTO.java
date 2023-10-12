package com.fisk.dataaccess.webservice.service;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String name;
    private Integer age;
    private String address;
}
