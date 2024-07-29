package com.fisk.mdm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.tablefield.CAndLDTO;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attribute.*;
import com.fisk.mdm.service.AttributeService;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityMsgVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author ChenYa
 * @date 2022/4/14 20:35
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/attribute")
public class AttributeController {
    @Resource
    AttributeService service;

    @ApiOperation("分页查询所有attribute")
    @PostMapping("/list")
    public ResultEntity<Page<AttributeVO>> getAll(@RequestBody AttributeQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("根据id查询attribute")
    @GetMapping("/get")
    public ResultEntity<AttributeVO> detail(Integer id) {
        return service.getById(id);
    }

    @ApiOperation("添加attribute")
    @PostMapping("/insert")
    public ResultEntity<ResultEnum> addData(@RequestBody AttributeDTO attributeDTO) {
        return ResultEntityBuild.build(service.addData(attributeDTO));
    }

    @ApiOperation("编辑attribute")
    @PutMapping("/update")
    public ResultEntity<ResultEnum> editData(@Validated @RequestBody AttributeUpdateDTO attributeUpdateDTO) {
        return ResultEntityBuild.build(service.editData(attributeUpdateDTO));
    }

    @ApiOperation("删除attribute(逻辑删除)")
    @DeleteMapping("/delete")
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(service.deleteDataById(id));
    }

    @ApiOperation("发布待添加和待修改的属性")
    @GetMapping("/getNotSubmittedData")
    public ResultEntity<ResultEnum> getNotSubmittedData(Integer entityId) {
        return service.getNotSubmittedData(entityId);
    }

    @ApiOperation("获取实体、属性信息")
    @GetMapping("/getER")
    public ResultEntity<List<EntityMsgVO>> getER() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getEntityMsg());
    }

    @ApiOperation("根据id集合查询属性信息")
    @PostMapping("/getByIds")
    public ResultEntity<List<AttributeInfoDTO>> getByIds(@RequestBody List<Integer> ids) {
        return service.getByIds(ids);
    }

    @ApiOperation("根据domainId查询属性")
    @PostMapping("/getByDomainId")
    public ResultEntity<AttributeInfoDTO> getByDomainId(@RequestBody AttributeDomainDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getByDomainId(dto));
    }

    @ApiOperation("修改属性状态")
    @PutMapping("/updateStatus")
    public ResultEntity<ResultEnum> updateStatus(@Validated @RequestBody AttributeStatusDTO statusDto) {
        return ResultEntityBuild.build(service.updateStatus(statusDto));
    }

    @ApiOperation("删除attribute")
    @DeleteMapping("/deleteData")
    public ResultEntity<ResultEnum> deleteAttribute(Integer id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }
    @ApiOperation("获取poi信息")
    @PostMapping("/getPoiDetails")
    public ResultEntity<List<PoiDetailDTO>> getPoiDetails(@RequestBody PoiQueryDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getPoiDetails(dto));
    }

    @ApiOperation("获取poi权限")
    @GetMapping("/getPoiAuthorization")
    public ResultEntity<Map<String, Object>> getPoiAuthorization() {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getPoiAuthorization());
    }

    /**
     * 获取主数据字段数据分类和数据级别
     *
     * @return
     */
    @GetMapping("/getDataClassificationsAndLevels")
    @ApiOperation(value = "获主数据字段数据分类和数据级别")
    public ResultEntity<CAndLDTO> getDataClassificationsAndLevels() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataClassificationsAndLevels());
    }

    /**
     * 关联主数据表字段和数据元标准
     * @param dtos
     * @return
     */
    @ApiOperation("关联主数据表字段和数据元标准")
    @PostMapping("/mapMDMFieldsWithStandards")
    public ResultEntity<Object> mapMDMFieldsWithStandards(@RequestBody List<StandardsBeCitedDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.mapMDMFieldsWithStandards(dtos));
    }
}
