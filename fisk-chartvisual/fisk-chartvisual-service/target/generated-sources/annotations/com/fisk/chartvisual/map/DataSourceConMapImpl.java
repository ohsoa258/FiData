package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-27T15:26:44+0800",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 1.8.0_191 (Oracle Corporation)"
)
public class DataSourceConMapImpl implements DataSourceConMap {

    @Override
    public DataSourceConPO dtoToPo(DataSourceConDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DataSourceConPO dataSourceConPO = new DataSourceConPO();

        dataSourceConPO.setConType( dtoConTypeValue( dto ) );
        dataSourceConPO.setConStr( dto.getConStr() );
        dataSourceConPO.setConAccount( dto.getConAccount() );
        dataSourceConPO.setConPassword( dto.getConPassword() );

        dataSourceConPO.setDelFlag( 1 );
        dataSourceConPO.setCreateTime( new java.util.Date() );

        return dataSourceConPO;
    }

    @Override
    public void editDtoToPo(DataSourceConEditDTO dto, DataSourceConPO po) {
        if ( dto == null ) {
            return;
        }

        po.setConType( dtoConTypeValue1( dto ) );
        if ( dto.getConStr() != null ) {
            po.setConStr( dto.getConStr() );
        }
        if ( dto.getConAccount() != null ) {
            po.setConAccount( dto.getConAccount() );
        }
        if ( dto.getConPassword() != null ) {
            po.setConPassword( dto.getConPassword() );
        }

        po.setDelFlag( 1 );
        po.setUpdateTime( new java.util.Date() );
    }

    private int dtoConTypeValue(DataSourceConDTO dataSourceConDTO) {
        if ( dataSourceConDTO == null ) {
            return 0;
        }
        DataSourceTypeEnum conType = dataSourceConDTO.getConType();
        if ( conType == null ) {
            return 0;
        }
        int value = conType.getValue();
        return value;
    }

    private int dtoConTypeValue1(DataSourceConEditDTO dataSourceConEditDTO) {
        if ( dataSourceConEditDTO == null ) {
            return 0;
        }
        DataSourceTypeEnum conType = dataSourceConEditDTO.getConType();
        if ( conType == null ) {
            return 0;
        }
        int value = conType.getValue();
        return value;
    }
}
