package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.entity.DataSourceAreaPO;
import com.fisk.datamodel.mapper.DataSourceAreaMapper;
import com.fisk.datamodel.service.IDataSourceArea;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class DataSourceAreaImpl extends ServiceImpl<DataSourceAreaMapper, DataSourceAreaPO> implements IDataSourceArea {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataSourceAreaDTO dto) {

        DataSourceAreaPO po = dto.toEntity(DataSourceAreaPO.class);

        Date date = new Date(System.currentTimeMillis());

        po.setCreateTime(date);
        po.setUpdateTime(date);
        po.setDelFlag(1);

        return this.save(po) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DataSourceAreaDTO getData(long id) {

        DataSourceAreaPO po = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();

        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return new DataSourceAreaDTO(po);
    }

    @Override
    public ResultEnum updateDataSourceArea(DataSourceAreaDTO dto) {

        long id = dto.getId();
        DataSourceAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        DataSourceAreaPO po = dto.toEntity(DataSourceAreaPO.class);

        // 设置删除状态
        po.setDelFlag(1);
        Date date = new Date(System.currentTimeMillis());
        po.setUpdateTime(date);

        // 执行方法
        return this.updateById(po)?ResultEnum.SUCCESS:ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSourceArea(long id) {

        // 1.非空判断
        DataSourceAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 2.将del_flag状态改为0
        model.setDelFlag(0);

        return this.updateById(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DataSourceAreaDTO> listDataSource() {

        List<DataSourceAreaPO> list = this.query().eq("del_flag", 1).list();

        if (null == list || list.isEmpty()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return DataSourceAreaDTO.convertEntityList(list);
    }
}
