package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.EntityPageDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/4/2 17:58
 */
@Api(tags = {SwaggerConfig.TAG_1})
@RestController
@RequestMapping("/entity")
public class EntityController {

    @Resource
    EntityService entityService;

    @ApiOperation("根据id获取实体")
    @GetMapping("/getDataById")
    @ResponseBody
    public ResultEntity<EntityVO> getDataById(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,entityService.getDataById(id));
    }

    @ApiOperation("分页查询实体")
    @PostMapping("/list")
    @ResponseBody
    public ResultEntity<Page<EntityVO>> listData(@Validated @RequestBody EntityPageDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,entityService.listData(dto));
    }

    @ApiOperation("修改实体")
    @PutMapping("/updateData")
    @ResponseBody
    public ResultEntity<ResultEnum> updateData(@Validated @RequestBody UpdateEntityDTO dto) {
        return ResultEntityBuild.build(entityService.updateData(dto));
    }

    @ApiOperation("删除实体")
    @DeleteMapping("/deleteData")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(entityService.deleteData(id));
    }

    @ApiOperation("创建实体")
    @PostMapping("/saveEntity")
    @ResponseBody
    public ResultEntity<ResultEnum> saveEntity(@RequestBody EntityDTO dto) {
        return ResultEntityBuild.build(entityService.saveEntity(dto));
    }

    @ApiOperation("根据实体id获取属性")
    @GetMapping("/getAttributeById")
    @ResponseBody
    public ResultEntity<EntityInfoVO> getAttributeById(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,entityService.getAttributeById(id));
    }
}
