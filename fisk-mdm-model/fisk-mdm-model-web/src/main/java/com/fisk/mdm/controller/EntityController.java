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
import java.util.List;

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
    public ResultEntity<Page<EntityVO>> list(@Validated @RequestBody EntityPageDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,entityService.listData(dto));
    }

    @ApiOperation("修改实体")
    @PutMapping("/update")
    @ResponseBody
    public ResultEntity<ResultEnum> update(@Validated @RequestBody UpdateEntityDTO dto) {
        return ResultEntityBuild.build(entityService.updateData(dto));
    }

    @ApiOperation("删除实体")
    @DeleteMapping("/delete")
    @ResponseBody
    public ResultEntity<ResultEnum> delete(Integer id) {
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
    public ResultEntity<EntityInfoVO> getAttributeById(Integer id, String name) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, entityService.getAttributeById(id, name));
    }

    @ApiOperation("根据实体id获取已发布和发布成功的属性")
    @GetMapping("/getFilterAttributeById")
    @ResponseBody
    public ResultEntity<EntityInfoVO> getFilterAttributeById(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, entityService.getFilterAttributeById(id));
    }

    @ApiOperation("获取可关联的实体")
    @GetMapping("/getCreateSuccessEntity")
    @ResponseBody
    public ResultEntity<List<EntityVO>> getCreateSuccessEntity(Integer modelId, Integer entityId) {
        return entityService.getCreateSuccessEntity(modelId, entityId);
    }
}
