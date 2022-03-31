package com.fisk.auth.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.auth.dto.clientregister.ClientRegisterDTO;
import com.fisk.auth.dto.clientregister.ClientRegisterQueryDTO;
import com.fisk.auth.service.IClientRegisterService;
import com.fisk.auth.vo.ClientRegisterVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 * @description 客户端注册功能只有添加和删除
 */
@RestController
@RequestMapping("/clientRegister")
public class ClientRegisterController {

    @Autowired
    private IClientRegisterService service;

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显: 根据id查询客户端数据")
    public ResultEntity<ClientRegisterDTO> getData(@PathVariable("id") long id){

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加客户端")
    public ResultEntity<Object> addData(@Validated @RequestBody ClientRegisterDTO clientRegister){

        return ResultEntityBuild.build(service.addData(clientRegister));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改客户端")
    public ResultEntity<Object> editData(@RequestBody ClientRegisterDTO dto){

        return ResultEntityBuild.build(service.editData(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除客户端")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

    @GetMapping("/getClientInfoList")
    @ApiOperation(value = "获取所有客户端信息")
    public ResultEntity<List<String>> getClientInfoList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getClientInfoList());
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取安全集成筛选器字段")
    public ResultEntity<Object> getClientColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @PostMapping("/pageFilter")
    @ApiOperation(value = "筛选器")
    public ResultEntity<Page<ClientRegisterVO>> listData(@RequestBody ClientRegisterQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData(query));
    }
}
