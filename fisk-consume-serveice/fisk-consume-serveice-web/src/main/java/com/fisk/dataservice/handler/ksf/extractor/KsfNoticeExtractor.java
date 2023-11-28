package com.fisk.dataservice.handler.ksf.extractor;

import com.fisk.dataservice.dto.ksfwebservice.item.ItemDataDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-13
 * @Description:
 */
public class KsfNoticeExtractor implements ResultSetExtractor<List<ItemDataDTO>> {
    @Override
    public List<ItemDataDTO> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        return null;
    }
}
