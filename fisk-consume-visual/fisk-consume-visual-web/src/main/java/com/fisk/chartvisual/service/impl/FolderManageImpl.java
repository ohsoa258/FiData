package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.FolderDTO;
import com.fisk.chartvisual.dto.FolderEditDTO;
import com.fisk.chartvisual.entity.FolderPO;
import com.fisk.chartvisual.map.FolderMap;
import com.fisk.chartvisual.mapper.FolderMapper;
import com.fisk.chartvisual.service.IFolderManageService;
import com.fisk.chartvisual.vo.FolderVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Service
public class FolderManageImpl extends ServiceImpl<FolderMapper, FolderPO> implements IFolderManageService {

    @Resource
    UserHelper userHelper;
    @Resource
    FolderMapper mapper;

    @Override
    public ResultEntity<Long> save(FolderDTO dto) {
        if (dto.pid != null) {
            if (mapper.selectById(dto.pid) == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, "父级目录不存在");
            }

            QueryWrapper<FolderPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(FolderPO::getPid, dto.pid).eq(FolderPO::getName, dto.name);
            if (mapper.selectOne(queryWrapper) != null) {
                return ResultEntityBuild.build(ResultEnum.DATA_EXISTS, "文件夹已存在");
            }
        }

        FolderPO model = FolderMap.INSTANCES.dtoToPo(dto);

        long res = mapper.insert(model);
        if (res > 0) {
            return ResultEntityBuild.buildData(ResultEnum.SUCCESS, model.id);
        } else {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    @Override
    public List<FolderVO> listData() {
        QueryWrapper<FolderPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "pid", "name", "create_time");
        List<FolderPO> list = mapper.selectList(queryWrapper);
        List<FolderVO> res = FolderMap.INSTANCES.poToVo(list);
        return res.stream()
                .filter(e -> e.pid == null)
                .peek(e -> e.setChild(getChild(e.id, res)))
                .collect(Collectors.toList());
    }

    @Override
    public ResultEntity<Object> delete(long id) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        FolderPO model = mapper.selectById(id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        mapper.deleteById(model);

        delChild(model.id, userInfo.id.toString());
        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    @Override
    public ResultEntity<Object> update(FolderEditDTO dto) {
        FolderPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        FolderMap.INSTANCES.editDtoToPo(dto, model);
        int res = mapper.updateById(model);
        return ResultEntityBuild.build(res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR);
    }

    private List<FolderVO> getChild(Long pid, List<FolderVO> list) {
        List<FolderVO> child = list.stream().filter(e -> e.pid != null && e.pid.equals(pid)).collect(Collectors.toList());
        for (FolderVO item : child) {
            item.child = getChild(item.id, list);
        }
        return child;
    }

    private void delChild(Long id, String userId) {
        QueryWrapper<FolderPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", id);
        List<FolderPO> list = mapper.selectList(queryWrapper);
        for (FolderPO item : list) {
            mapper.deleteByIdWithFill(item);
            delChild(item.id, userId);
        }
    }
}
