package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionDTO;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionQueryDTO;
import com.fisk.datamanagement.dto.DataSet.CodeSetQueryDTO;
import com.fisk.datamanagement.entity.CodeCollectionPO;
import com.fisk.datamanagement.map.CodeCollectionMap;
import com.fisk.datamanagement.mapper.CodeCollectionMapper;
import com.fisk.datamanagement.service.CodeCollectionService;
import com.fisk.datamanagement.vo.CodeCollectionVO;
import com.fisk.datamanagement.vo.CodeSetVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodeCollectionServiceImpl extends ServiceImpl<CodeCollectionMapper, CodeCollectionPO> implements CodeCollectionService {

    @Resource
    UserClient userClient;
    @Resource
    CodeSetServiceImpl codeSetService;
    @Override
    public ResultEnum addCodeCollection(CodeCollectionDTO dto) {
        CodeCollectionPO codeCollectionPO = CodeCollectionMap.INSTANCES.dtoToPo(dto);
        boolean save = this.save(codeCollectionPO);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateCodeCollection(CodeCollectionDTO dto) {
        if (dto.id ==null || dto.id == 0){
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        CodeCollectionPO codeCollectionPO = CodeCollectionMap.INSTANCES.dtoToPo(dto);
        boolean update = this.updateById(codeCollectionPO);
        if (!update) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delCodeCollection(Integer id) {
        boolean del = this.removeById(id);
        if (!del) {
            return ResultEnum.DELETE_ERROR;
        }
        codeSetService.delCodeSetByCollectionId(id);
        return ResultEnum.SUCCESS;
    }

    @Override
    public Page<CodeCollectionVO> getCodeCollection(CodeCollectionQueryDTO query) {
        long current = query.page.getCurrent();
        long size = query.page.getSize();
        Integer startIndex = Math.toIntExact((current - 1) * size);
        List<CodeCollectionVO> all = baseMapper.getCodeCollection(query.keyword,startIndex,(int)size);
        Integer total = baseMapper.getAllCodeCollectionCount();
        List<Long> userId = new ArrayList<>();
        Page<CodeCollectionVO> page = new Page<>(current, size, all.size());
        if (CollectionUtils.isNotEmpty(all)){
            List<List<Long>> userIdList = all.stream().map(i -> {
                List<CodeSetVO> codeSetVOList = i.getCodeSetVOList();
                if (CollectionUtils.isNotEmpty(codeSetVOList)){
                    List<Long> userIds = codeSetVOList.stream()
                            .filter(x -> StringUtils.isNotEmpty(x.createUser))
                            .map(x -> Long.valueOf(x.createUser))
                            .distinct()
                            .collect(Collectors.toList());
                    return userIds;
                }else {
                    return new ArrayList<Long>();
                }
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(userIdList)){
                for (List<Long> longs : userIdList) {
                    userId.addAll(longs);
                }
            }
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userId);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                    && CollectionUtils.isNotEmpty(userListByIds.getData())) {
                all = all.stream().map(i -> {
                    List<CodeSetVO> codeSetVOList = i.getCodeSetVOList();
                    if (CollectionUtils.isNotEmpty(codeSetVOList)) {
                        codeSetVOList.forEach(e -> {
                            userListByIds.getData()
                                    .stream()
                                    .filter(user -> user.getId().toString().equals(e.createUser))
                                    .findFirst()
                                    .ifPresent(user -> e.createUser = user.userAccount);
                            i.setCodeSetVOList(codeSetVOList);
                        });
                    }
                    return i;
                }).collect(Collectors.toList());
            }
            page.setRecords(all);
            page.setTotal(total);
        }
        return page;
    }

    @Override
    public Page<CodeCollectionVO> pageCollectionList(CodeCollectionQueryDTO query) {
        return baseMapper.pageCollectionList(query.page, query);
    }
}
