package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.DataSet.CodeSetDTO;
import com.fisk.datamanagement.dto.DataSet.CodeSetQueryDTO;
import com.fisk.datamanagement.entity.CodeSetPO;
import com.fisk.datamanagement.map.CodeSetMap;
import com.fisk.datamanagement.mapper.CodeSetMapper;
import com.fisk.datamanagement.service.ICodeSetService;
import com.fisk.datamanagement.vo.CodeSetVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service("dataSetService")
public class CodeSetServiceImpl extends ServiceImpl<CodeSetMapper, CodeSetPO> implements ICodeSetService {

    @Resource
    UserClient userClient;
    @Override
    public Page<CodeSetVO> getAll(CodeSetQueryDTO query) {
        Page<CodeSetVO> all = baseMapper.getAll(query.page, query);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Long> userIds = all.getRecords().stream()
                    .filter(x -> StringUtils.isNotEmpty(x.createUser))
                    .map(x -> Long.valueOf(x.createUser))
                    .distinct()
                    .collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                all.getRecords().forEach(e -> {
                    userListByIds.getData()
                            .stream()
                            .filter(user -> user.getId().toString().equals(e.createUser))
                            .findFirst()
                            .ifPresent(user -> e.createUser = user.userAccount);
                });
            }
        }
        return all;
    }

    @Override
    public ResultEnum addCodeSet(CodeSetDTO dto) {
        CodeSetPO codeSetPO = CodeSetMap.INSTANCES.dtoToPo(dto);
        boolean save = this.save(codeSetPO);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateCodeSet(CodeSetDTO dto) {
        if (dto.id ==null || dto.id == 0){
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        CodeSetPO codeSetPO = CodeSetMap.INSTANCES.dtoToPo(dto);
        boolean update = this.updateById(codeSetPO);
        if (!update) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delCodeSet(Integer id) {
        boolean del = this.removeById(id);
        if (!del) {
            return ResultEnum.DELETE_ERROR;
        }
        return ResultEnum.SUCCESS;
    }
}
