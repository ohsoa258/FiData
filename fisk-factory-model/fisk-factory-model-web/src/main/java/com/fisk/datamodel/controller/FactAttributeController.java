package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeAddDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDropQueryDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.service.IFactAttribute;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.FACT_ATTRIBUTE})
@RestController
@RequestMapping("/factAttribute")
@Slf4j
public class FactAttributeController {

    @Resource
    IFactAttribute service;

    /*@ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{factId}")
    public ResultEntity<Object> getFactAttributeList(@PathVariable int factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeList(factId));
    }*/

    @ApiOperation("获取事实字段表列表")
    @GetMapping("/getFactAttributeList/{id}")
    public ResultEntity<Object> getFactAttributeList(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeDataList(id));
    }

    @ApiOperation("添事实字段")
    @PostMapping("/addFactAttribute")
    public ResultEntity<Object> addFactAttribute(@Validated @RequestBody FactAttributeAddDTO dto) {
        return ResultEntityBuild.build(service.addFactAttribute(dto));
    }

    /**
     * 数仓--建doris三种模型表  聚合  主键  冗余
     *
     * @param dto
     * @return
     */
    @ApiOperation("数仓--建doris三种模型表")
    @PostMapping("/buildDorisAggregateTbl")
    public ResultEntity<Object> buildDorisAggregateTbl(@Validated @RequestBody FactAttributeAddDTO dto) {
        return ResultEntityBuild.build(service.buildDorisAggregateTbl(dto));
    }

    @ApiOperation("删除事实字段")
    @PostMapping("/deleteFactAttribute")
    public ResultEntity<Object> deleteFactAttribute(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(service.deleteFactAttribute(ids));
    }

    @GetMapping("/getFactAttributeDetail/{id}")
    @ApiOperation("根据事实字段id获取事实字段详情")
    public ResultEntity<Object> getFactAttributeDetail(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeDetail(id));
    }


    @PostMapping("/getFactAttributeByIds")
    @ApiOperation("根据事实字段id集合获取事实字段详情")
    public ResultEntity<List<FactAttributeDTO>> getFactAttributeByIds(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeByIds(ids));
    }

    @ApiOperation("修改事实字段")
    @PutMapping("/editFactAttribute")
    public ResultEntity<Object> editFactAttribute(@Validated @RequestBody FactAttributeUpdateDTO dto) {
        return ResultEntityBuild.build(service.updateFactAttribute(dto));
    }

    @GetMapping("/getFactEntity")
    @ApiOperation("获取事实表元数据(用于Doris创建表)")
    public ResultEntity<ModelMetaDataDTO> getFactEntity(@RequestParam("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactMetaData(id));
    }

    @PostMapping("/getFactAttributeData")
    @ApiOperation("根据事实id获取事实字段详情")
    public ResultEntity<Object> getFactAttributeData(@RequestBody FactAttributeDropQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeData(dto));
    }

    @ApiOperation("根据事实id获取事实字段所有来源id")
    @GetMapping("/getFactAttributeSourceId/{id}")
    public ResultEntity<Object> getFactAttributeSourceId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttributeSourceId(id));
    }

    @ApiOperation("根据事实id获取事实字段及其关联详情(nifi)")
    @GetMapping("/selectAttributeList")
    public ResultEntity<List<ModelPublishFieldDTO>> selectAttributeList(@RequestParam("factId") int factId) {
        return service.selectAttributeList(factId);
    }

    @ApiOperation("根据事实id获取事实字段(宽表)")
    @GetMapping("/getFactAttribute/{id}")
    public ResultEntity<Object> getFactAttribute(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFactAttribute(id));
    }

    @ApiOperation("根据事实字段id获取事实字段配置详情")
    @GetMapping("/getConfigDetailsByFactAttributeId/{id}")
    public ResultEntity<Object> getConfigDetailsByFactAttributeId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getConfigDetailsByFactAttributeId(id));
    }

    @ApiOperation("添加单个事实字段")
    @PostMapping("/addFactField")
    public ResultEntity<Object> addFactField(@Validated @RequestBody FactAttributeDTO dto) {
        return ResultEntityBuild.build(service.addFactField(dto));
    }

    @ApiOperation("修改单个事实字段")
    @PutMapping("/editFactField")
    public ResultEntity<Object> editFactField(@Validated @RequestBody FactAttributeDTO dto) {
        return ResultEntityBuild.build(service.editFactField(dto));
    }

    @ApiOperation("根据业务域id,获取业务域下的维度表详情和共享维度表详情(表名+字段),查询的都是已发布的表")
    @GetMapping("/getDimensionDetailByBusinessId/{id}")
    public ResultEntity<Object> getDimensionDetailByBusinessId(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDimensionDetailByBusinessId(id));
    }

    @ApiOperation("查询事实表关联数据")
    @PostMapping("/executeFactTableSql")
    public ResultEntity<Object> executeFactTableSql(@Validated @RequestBody WideTableFieldConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.executeFactTableSql(dto));
    }

    /**
     * 关联数仓表字段和数据元标准
     * @param dtos
     * @return
     */
    @ApiOperation("关联数仓表字段和数据元标准")
    @PostMapping("/mapModelFieldsWithStandards")
    public ResultEntity<Object> mapModelFieldsWithStandards(@RequestBody List<StandardsBeCitedDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.mapModelFieldsWithStandards(dtos));
    }

    /**
     * 关联数仓表字段和指标标准
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @ApiOperation("关联数仓表字段和指标标准")
    @PostMapping("/mapModelFieldsWithIndex")
    public ResultEntity<Object> mapModelFieldsWithIndex(@RequestBody List<ModelAndIndexMappingDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.mapModelFieldsWithIndex(dtos));
    }

}
