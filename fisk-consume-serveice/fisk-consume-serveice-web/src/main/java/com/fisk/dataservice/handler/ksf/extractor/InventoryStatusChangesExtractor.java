package com.fisk.dataservice.handler.ksf.extractor;

import com.fisk.dataservice.dto.ksfwebservice.Inventory.InventoryStatusChangesDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author: wangjian
 * @Date: 2023-11-13
 * @Description:
 */
public class InventoryStatusChangesExtractor implements ResultSetExtractor<InventoryStatusChangesDTO> {
    @Override
    public InventoryStatusChangesDTO extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        InventoryStatusChangesDTO result = new InventoryStatusChangesDTO();

//        while (resultSet.next()) {
//            Master master = null;
//            Detail detail = null;
//
//            for (InventoryStatusChangesDTO.Business businesses : result.getBusinessList()) {
//                if (md.getMaster().getId() == rs.getInt("master_id")) {
//                    master = md.getMaster();
//                    break;
//                }
//            }
//
//            if (master == null) {
//                master = new Master();
//                master.setId(rs.getInt("master_id"));
//                // set other Master properties
//                result.add(new MasterDetail(master, new ArrayList<>()));
//            }
//
//            if (rs.getInt("detail_id") != 0) {
//                detail = new Detail();
//                detail.setId(rs.getInt("detail_id"));
//                // set other Detail properties
//                master.getDetails().add(detail);
//            }
//        }

        return result;
    }
}
