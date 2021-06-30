package com.fisk.task.utils;

import com.davis.client.model.PositionDTO;
import com.fisk.common.constants.NifiConstants;

/**
 * @author gy
 */
public class NifiPositionHelper {


    /**
     * 创建坐标对象
     *
     * @param level 组件横向排列是第几个
     * @return 坐标对象
     */
    public static PositionDTO buildXPositionDTO(int level) {
        int y = level / NifiConstants.AttrConstants.POSITION_X_MAX;
        PositionDTO dto = new PositionDTO();
        if(y > 0){
            int x = level % NifiConstants.AttrConstants.POSITION_X_MAX;
            dto.setX(NifiConstants.AttrConstants.POSITION_X * x);
            dto.setY(y * NifiConstants.AttrConstants.POSITION_X);
        }else{
            dto.setX(NifiConstants.AttrConstants.POSITION_X * level);
            dto.setY(0.0);
        }
        return dto;
    }

    /**
     * 创建坐标对象
     *
     * @param level 组件纵向排列是第几个
     * @return 坐标对象
     */
    public static PositionDTO buildYPositionDTO(int level) {
        PositionDTO dto = new PositionDTO();
        dto.setX(0.0);
        dto.setY(NifiConstants.AttrConstants.POSITION_Y * level);
        return dto;
    }
}
