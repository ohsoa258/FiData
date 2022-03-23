package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.entity.DataSourceAreaPO;
import com.fisk.datamodel.map.DataSourceAreaMap;
import com.fisk.datamodel.mapper.DataSourceAreaMapper;
import com.fisk.datamodel.service.IDataSourceArea;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class DataSourceAreaImpl extends ServiceImpl<DataSourceAreaMapper, DataSourceAreaPO> implements IDataSourceArea {

    @Resource
    private DataSourceAreaMapper mapper;
    @Resource
    UserHelper userHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(DataSourceAreaDTO dto) {

        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();

        // dto -> po
        DataSourceAreaPO po = dto.toEntity(DataSourceAreaPO.class);
        po.setCreateUser(String.valueOf(userInfo.id));

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
        return DataSourceAreaMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateDataSourceArea(DataSourceAreaDTO dto) {

        // 获取当前登录人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();

        long id = dto.getId();
        DataSourceAreaPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        DataSourceAreaPO po = dto.toEntity(DataSourceAreaPO.class);
        po.setUpdateUser(String.valueOf(userInfo.id));

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
        return  mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DataSourceAreaDTO> listDataSource() {

        List<DataSourceAreaPO> listPo = this.query().eq("del_flag", 1).list();

        if (null == listPo || listPo.isEmpty()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return DataSourceAreaMap.INSTANCES.listPoToDto(listPo);
    }
}
