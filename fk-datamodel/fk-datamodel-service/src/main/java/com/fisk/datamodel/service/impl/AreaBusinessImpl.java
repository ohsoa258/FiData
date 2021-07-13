package com.fisk.datamodel.service.impl;

import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datamodel.dto.AreaBusinessDTO;
import com.fisk.datamodel.entity.AreaBusinessPO;
import com.fisk.datamodel.map.AreaBusinessMap;
import com.fisk.datamodel.mapper.AreaBusinessMapper;
import com.fisk.datamodel.service.IAreaBusiness;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@Service
public class AreaBusinessImpl implements IAreaBusiness {

    @Resource
    UserHelper userHelper;

    @Resource
    AreaBusinessMapper areaBusinessMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(AreaBusinessDTO dto) {

        UserInfo userInfo = userHelper.getLoginUserInfo();
        AreaBusinessPO model = AreaBusinessMap.INSTANCES.dtoToPo(dto);
        model.createUser = userInfo.id.toString();
        return areaBusinessMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public AreaBusinessDTO getDataById(long id) {

        AreaBusinessPO po = areaBusinessMapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return null;
    }
}
