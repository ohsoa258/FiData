package com.fisk.mdm.controller;
import com.fisk.mdm.service.AccessDataService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "数据接入")
@RestController
@RequestMapping("/access")
public class AccessDataController {
    @Autowired
    private AccessDataService accessDataService;


}
