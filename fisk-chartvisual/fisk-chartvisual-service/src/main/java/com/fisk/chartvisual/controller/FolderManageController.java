package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.FolderDTO;
import com.fisk.chartvisual.dto.FolderEditDTO;
import com.fisk.chartvisual.service.IFolderManageService;
import com.fisk.chartvisual.vo.FolderVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件夹管理
 *
 * @author gy
 */
@RestController
@RequestMapping("/folder")
public class FolderManageController {

    @Resource
    private IFolderManageService service;

    @ApiOperation("创建文件夹")
    @PostMapping("/add")
    public ResultEntity<Long> addFolder(@Validated @RequestBody FolderDTO dto) {
        return service.save(dto);
    }

    @ApiOperation("查询文件夹")
    @GetMapping("/list")
    public ResultEntity<List<FolderVO>> list() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listData());
    }

    @ApiOperation("编辑文件夹")
    @PutMapping("/update")
    public ResultEntity<Object> update(@Validated @RequestBody FolderEditDTO dto) {
        return service.update(dto);
    }

    @ApiOperation("删除文件夹")
    @DeleteMapping("/delete")
    public ResultEntity<Object> delete(Long id) {
        return service.delete(id);
    }
}
