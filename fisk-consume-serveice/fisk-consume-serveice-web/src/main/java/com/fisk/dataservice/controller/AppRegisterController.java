package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.vo.app.*;
import com.fisk.dataservice.service.IAppRegisterManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用注册控制器
 * @date 2022/1/6 14:51
 */

@Api(tags = {SwaggerConfig.TAG_2})
@RestController
@RequestMapping("/appRegister")
public class AppRegisterController {
    @Resource
    private IAppRegisterManageService service;

    // 基于构造器注入
    private final HttpServletResponse response;

    public AppRegisterController(HttpServletResponse response) {
        this.response = response;
    }

    /*
        基于字段注入，IDEA有警告提醒:不建议使用基于字段注入，调整成基于构造器注入
        @Autowired
        private HttpServletResponse response;
     */

    @ApiOperation(value = "查询下游系统总数")
    @GetMapping("/getAppCount")
    public ResultEntity<Object> getAppCount() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAppCount());
    }

    @ApiOperation(value = "应用过滤字段")
    @GetMapping("/getColumn")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getColumn());
    }

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<Page<AppRegisterVO>> pageFilter(@RequestBody AppRegisterQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.pageFilter(dto));
    }

    @ApiOperation("添加应用")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody AppRegisterDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑应用")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody AppRegisterEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除应用")
    @DeleteMapping("/delete/{appId}")
    public ResultEntity<Object> deleteData(@PathVariable("appId") int appId) {
        return ResultEntityBuild.build(service.deleteData(appId));
    }

    @ApiOperation("分页查询应用API订阅")
    @PostMapping("/appApiPage")
    public ResultEntity<Page<AppApiSubVO>> getSubscribeAll(@Validated @RequestBody AppApiSubQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getSubscribeAll(dto));
    }

    @ApiOperation("应用订阅api")
    @PostMapping("/appSubscribe")
    public ResultEntity<Object> appSubscribe(@Validated @RequestBody AppApiSubSaveDTO dto) {
        return ResultEntityBuild.build(service.appSubscribe(dto));
    }

    @ApiOperation("重置密码")
    @PutMapping("/resetPwd")
    public ResultEntity<Object> resetPwd(@Validated @RequestBody AppPwdResetDTO dto) {
        return ResultEntityBuild.build(service.resetPwd(dto));
    }

    @ApiOperation("生成文档")
    @PostMapping("/createDoc")
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> createDoc(@Validated @RequestBody CreateAppApiDocDTO dto) {
        return ResultEntityBuild.build(service.createDoc(dto,response));
    }

//    @ApiOperation(value = "下载文档")
//    @GetMapping("/downloadDoc/{fileName}")
//    public ResponseEntity downloadDoc(@PathVariable("fileName") String fileName) {
//        return service.downloadDoc(fileName);
//    }

    @ApiOperation("查询应用API参数")
    @PostMapping("/getParmAll")
    public ResultEntity<List<AppApiParmVO>> getParmAll(@Validated @RequestBody AppApiParmQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getParmAll(dto));
    }

    @ApiOperation("设置内置参数")
    @PostMapping("/setParm")
    public ResultEntity<Object> setParm(@Validated @RequestBody AppApiBuiltinParmEditDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setParm(dto));
    }
}
