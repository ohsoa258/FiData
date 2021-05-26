package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-05-26T15:33:24+0800",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 1.8.0_201 (Oracle Corporation)"
)
public class DataSourceConMapImpl implements DataSourceConMap {

    @Override
    public DataSourceConPO dtoToPo(DataSourceConDTO dto) {
        if ( dto == null ) {
            return null;
        }

        DataSourceConPO dataSourceConPO = new DataSourceConPO();

        dataSourceConPO.setConType( dtoConTypeCode( dto ) );
        dataSourceConPO.setConStr( dto.getConStr() );
        dataSourceConPO.setConAccount( dto.getConAccount() );
        dataSourceConPO.setConPassword( dto.getConPassword() );

        return dataSourceConPO;
    }

    private int dtoConTypeCode(DataSourceConDTO dataSourceConDTO) {
        if ( dataSourceConDTO == null ) {
            return 0;
        }
        DataSourceTypeEnum conType = dataSourceConDTO.getConType();
        if ( conType == null ) {
            return 0;
        }
        int code = conType.getCode();
        return code;
    }
}
