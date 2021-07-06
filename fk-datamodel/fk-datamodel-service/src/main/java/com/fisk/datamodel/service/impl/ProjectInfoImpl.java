package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.ProjectInfoDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.ProjectInfoPO;
import com.fisk.datamodel.mapper.ProjectInfoMapper;
import com.fisk.datamodel.service.IProjectInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author Lock
 */
@Service
public class ProjectInfoImpl extends ServiceImpl<ProjectInfoMapper, ProjectInfoPO> implements IProjectInfo {

    @Resource
    private BusinessAreaImpl businessAreaImpl;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addData(ProjectInfoDTO dto) {

        String businessName = dto.getBusinessName();
        BusinessAreaPO bpo = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();
        if (bpo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        ProjectInfoPO po = dto.toEntity(ProjectInfoPO.class);
        po.setBusinessid(bpo.getId());

        Date date = new Date(System.currentTimeMillis());
        po.setCreateTime(date);
        po.setUpdateTime(date);
        po.setDelFlag(1);

        boolean save = this.save(po);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ProjectInfoDTO getDataById(long id) {

        ProjectInfoPO po = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        ProjectInfoDTO dto = new ProjectInfoDTO(po);

        // 将businessName封装进去
        long bid = po.getBusinessid();
        if (bid == 0) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        BusinessAreaPO bpo = businessAreaImpl.query()
                .eq("id", bid)
                .eq("del_flag", 1)
                .one();
        if (bpo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        dto.setBusinessName(bpo.getBusinessName());

        return dto;
    }

    @Override
    public ResultEnum updateProjectInfo(ProjectInfoDTO dto) {

        long id = dto.getId();
        ProjectInfoPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        ProjectInfoPO po = dto.toEntity(ProjectInfoPO.class);
        String businessName = dto.getBusinessName();
        if (businessName == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        BusinessAreaPO bpo = businessAreaImpl.query()
                .eq("business_name", businessName)
                .eq("del_flag", 1)
                .one();

        po.setBusinessid(bpo.getId());
        po.setDelFlag(1);
        Date date = new Date(System.currentTimeMillis());
        po.setUpdateTime(date);

        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataById(long id) {

        ProjectInfoPO po = this.getById(id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 修改状态
        po.setDelFlag(0);

        // update tb_project_info set del_flag=1 where id=1;
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<Map<String, Object>> listData(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);
        rows = Math.max(rows, 1);

        // 2.新建分页
        Page<Map<String, Object>> pageMap = new Page<>(page, rows);

        return pageMap.setRecords(baseMapper.queryByPage(pageMap, key));
    }


}
