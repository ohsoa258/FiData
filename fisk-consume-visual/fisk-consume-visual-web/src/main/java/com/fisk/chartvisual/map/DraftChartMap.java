package com.fisk.chartvisual.map;

import com.fisk.chartvisual.dto.chartvisual.ChartPropertyDTO;
import com.fisk.chartvisual.dto.chartvisual.ChartPropertyEditDTO;
import com.fisk.chartvisual.entity.BaseChartProperty;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import javax.xml.bind.DatatypeConverter;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DraftChartMap {
    DraftChartMap INSTANCES = Mappers.getMapper(DraftChartMap.class);


    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(target = "image",source = "image",qualifiedByName="stringConvertByte"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="stringConvertByte")
    })
    DraftChartPO dtoToPo(ChartPropertyDTO dto);

    /**
     * po => vo
     * @param po source
     * @return target vo
     */
    @Mappings({
            @Mapping(target = "image",source = "image",qualifiedByName="byteConvertString"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="byteConvertString")
    })
    ChartPropertyVO poToVo(DraftChartPO po);

    /**
     * editDto => po
     *
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "image",source = "image",qualifiedByName="stringConvertByte"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="stringConvertByte")
    })
    void editDtoToPo(ChartPropertyEditDTO dto, @MappingTarget BaseChartProperty po);

    /**
     * 字节转base64
     * @param image
     * @return
     */
    @Named("byteConvertString")
    default String byteConvertStringFun(byte[] image){
        if (image==null){
            return  "";
        }
        return DatatypeConverter.printBase64Binary(image);

    }

    /**
     * base64 转字节
     * @param image
     * @return
     */
    @Named("stringConvertByte")
    default  byte[] stringConvertByteFun(String image){
        if (image==null){
            return  null;
        }
        return DatatypeConverter.parseBase64Binary(image);
    }
}
