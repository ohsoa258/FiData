package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;
import com.fisk.datamodel.entity.CustomScriptPO;
import com.fisk.datamodel.map.CustomScriptMap;
import com.fisk.datamodel.mapper.CustomScriptMapper;
import com.fisk.datamodel.service.ICustomScript;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class CustomScriptImpl
        extends ServiceImpl<CustomScriptMapper, CustomScriptPO>
        implements ICustomScript {

    @Resource
    CustomScriptMapper mapper;

    @Resource
    UserClient userClient;

    @Override
    public ResultEnum addCustomScript(CustomScriptDTO dto) {
        return mapper.insert(CustomScriptMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.DATA_SUBMIT_ERROR;
    }

    @Override
    public ResultEnum updateCustomScript(CustomScriptDTO dto) {
        CustomScriptPO customScriptPO = mapper.selectById(dto.id);
        if (customScriptPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return mapper.updateById(CustomScriptMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum batchDelCustomScript(List<Integer> ids) {
        QueryWrapper<CustomScriptPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        return mapper.delete(queryWrapper) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public CustomScriptDTO getCustomScript(Integer id) {
        CustomScriptPO customScriptPO = mapper.selectById(id);
        if (customScriptPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return CustomScriptMap.INSTANCES.poToDto(customScriptPO);
    }

    @Override
    public List<CustomScriptInfoDTO> listCustomScript(CustomScriptQueryDTO dto) {
        QueryWrapper<CustomScriptPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CustomScriptPO::getTableId, dto.tableId)
                .eq(CustomScriptPO::getType, dto.type)
                .eq(CustomScriptPO::getExecType, dto.execType);
        List<CustomScriptPO> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<CustomScriptInfoDTO> customScriptInfoList = CustomScriptMap.INSTANCES.poListToDtoList(list);


        List<String> userIds = customScriptInfoList
                .stream()
                .map(CustomScriptInfoDTO::getCreateUser)
                .collect(Collectors.toList());
        //类型转换
        List<Long> codesInteger = userIds.stream().map(Long::parseLong).collect(Collectors.toList());

        //获取用户集合
        ResultEntity<List<UserDTO>> resultEntity = userClient.getUserListByIds(codesInteger);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        List<UserDTO> userList = resultEntity.data;

        //用户id替换用户名
        customScriptInfoList.forEach(e -> {
            userList.forEach(t -> {
                if (t.id.toString().equals(e.createUser)) {
                    e.createUser = t.username;
                }
            });
        });

        return customScriptInfoList;
    }

    @Override
    public ResultEnum addOrUpdateCustomScript(List<CustomScriptDTO> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.SUCCESS;
        }
        List<CustomScriptPO> poList = CustomScriptMap.INSTANCES.dtoListToPoList(dtoList);
        boolean b = this.saveOrUpdateBatch(poList);
        if (!b) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        //删除
        List<Integer> idList = dtoList
                .stream()
                .filter(e -> e.id != 0)
                .map(e -> e.id)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(idList)) {
            return ResultEnum.SUCCESS;
        }

        return batchDelCustomScriptByIds(idList, dtoList.get(0).tableId, dtoList.get(0).type);

    }

    /**
     * 批量删除根据id集合
     *
     * @param idList
     * @param tableId
     * @param type
     * @return
     */
    public ResultEnum batchDelCustomScriptByIds(List<Integer> idList, Integer tableId, Integer type) {
        QueryWrapper<CustomScriptPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("id", idList)
                .select("id")
                .lambda()
                .eq(CustomScriptPO::getTableId, tableId)
                .eq(CustomScriptPO::getType, type);
        List<CustomScriptPO> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return ResultEnum.SUCCESS;
        }
        boolean flat = this.remove(queryWrapper);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取批量脚本,分号隔开
     *
     * @param dto
     * @return
     */
    public String getBatchScript(CustomScriptQueryDTO dto) {
        List<CustomScriptInfoDTO> customScriptInfoDTOS = listCustomScript(dto);
        if (CollectionUtils.isEmpty(customScriptInfoDTOS)) {
            return null;
        }
        List<String> collect = customScriptInfoDTOS.stream()
                .sorted(Comparator.comparing(CustomScriptInfoDTO::getSequence))
                .map(e -> e.getScript())
                .collect(Collectors.toList());

        return String.join(";", collect);
    }


}
