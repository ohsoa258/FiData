package com.fisk.datafactory.service;

import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/11 11:54
 */
public interface IDataFactory {
    /**
     * 判断物理表是否在管道使用
     *
     * @param dto dto
     * @return boolean
     */
    boolean loadDepend(LoadDependDTO dto);
}
