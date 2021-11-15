package com.fisk.dataaccess.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.TableAccessNonDTO;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.dataaccess.service.ITableFields;
import com.fisk.dataaccess.vo.AtlasIdsVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/tableFields")
@Slf4j
public class TableFieldsController {
    @Resource
    public ITableFields service;
    @Resource
    private PublishTaskClient publishTaskClient;

    /**
     * 查询表字段
     *
     * @return 返回值
     */
    @PostMapping("/getTableField")
    @ApiOperation(value = "查询表字段")
    public ResultEntity<TableFieldsDTO> getTableField(@RequestParam("id") int id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableField(id));
    }

    /**
     * 添加物理表
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加物理表字段")
    public ResultEntity<Object> addData(@RequestBody TableAccessNonDTO dto) {
        ResultEntity<AtlasIdsVO> atlasIdsVO = service.addData(dto);

        AtlasIdsVO atlasIds = atlasIdsVO.data;

        if (atlasIds == null) {
            return ResultEntityBuild.buildData(atlasIdsVO.code,atlasIdsVO.msg);
        }

        if (atlasIdsVO.code == 0) {
            AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();
            atlasEntityQueryDTO.userId = atlasIds.userId;
            // 应用注册id
            atlasEntityQueryDTO.appId = atlasIds.appId;
            atlasEntityQueryDTO.dbId = atlasIds.dbId;
//            ResultEntity<Object> task = publishTaskClient.publishBuildAtlasTableTask(atlasEntityQueryDTO);
//            log.info("task:" + JSON.toJSONString(task));
//            System.out.println(task);
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, atlasIdsVO);
    }

    /**
     * 修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表字段")
    public ResultEntity<Object> editData(@RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.updateData(dto));
    }
}
