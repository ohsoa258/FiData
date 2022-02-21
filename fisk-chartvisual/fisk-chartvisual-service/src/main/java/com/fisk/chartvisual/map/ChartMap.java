package com.fisk.chartvisual.map;

import com.alibaba.fastjson.JSONObject;
import com.fisk.chartvisual.dto.ChartPropertyEditDTO;
import com.fisk.chartvisual.dto.ChildvisualDTO;
import com.fisk.chartvisual.contentSplit.ContentDTO;
import com.fisk.chartvisual.dto.ReleaseChart;
import com.fisk.chartvisual.entity.BaseChartProperty;
import com.fisk.chartvisual.entity.ChartChildvisualPO;
import com.fisk.chartvisual.entity.ChartPO;
import com.fisk.chartvisual.entity.DraftChartPO;
import com.fisk.chartvisual.vo.ChartPropertyVO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import javax.xml.bind.DatatypeConverter;

/**
 * @author gy
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChartMap {

    ChartMap INSTANCES = Mappers.getMapper(ChartMap.class);

    /**
     * dto => po
     *
     * @param dto source
     * @return target
     */
    @Mappings({
            @Mapping(target = "image",source = "image",qualifiedByName="stringConvertByte"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="stringConvertByte"),
            @Mapping(target = "content",source = "content",qualifiedByName="publicSplit")
    })
    ChartPO dtoToPo(ReleaseChart dto);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(target = "image",source = "image",qualifiedByName="stringConvertByte"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="stringConvertByte")
    })
    ChartPO chartDtoToPo(ReleaseChart dto);

    /**
     * 发布草稿，draftPo => chartPo
     *
     * @param draft draft
     * @param release release
     * @return release
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "updateUser", ignore = true),

    })
    ChartPO draftToRelease(DraftChartPO draft, @MappingTarget ChartPO release);

    /**
     * po => vo
     * @param po source
     * @return target vo
     */
    @Mappings({
            @Mapping(target = "image",source = "image",qualifiedByName="byteConvertString"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="byteConvertString"),
    })
    ChartPropertyVO poToVo(ChartPO po);

    /**
     * editDto => po
     * @param dto source
     * @param po target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "image",source = "image",qualifiedByName="stringConvertByte"),
            @Mapping(target = "backgroundImage",source = "backgroundImage",qualifiedByName="stringConvertByte"),
            @Mapping(target = "content",source = "content",qualifiedByName="publicSplit")
    })
    void editDtoToPo(ChartPropertyEditDTO dto, @MappingTarget BaseChartProperty po);

    /**
     * dto => po
     * @param dto
     * @return
     */
    @Mappings({
            @Mapping(target = "componentBackground",source = "componentBackground",qualifiedByName="stringConvertByte"),
            @Mapping(target = "layComponentBackground",source = "layComponentBackground",qualifiedByName="stringConvertByte")
    })
    ChartChildvisualPO dtoToPo(ChildvisualDTO dto);

    /**
     * 字节转base64
     * @param image
     * @return
     */
    @Named("byteConvertString")
    default  String byteConvertStringFun(byte[] image){
        if (image==null){
            return  "";
        }
        return DatatypeConverter.printBase64Binary(image);
    }

    /**
     * base64转字节
     * @param image
     * @return
     */
    @Named("stringConvertByte")
    default byte[] stringConvertByteFun(String image){
        if (image==null){
            return  null;
        }
        return DatatypeConverter.parseBase64Binary(image);
    }

    /**
     * json拆分公共的
     * @param content
     * @return
     */
    @Named("publicSplit")
    default String publicSplit(String content){
        JSONObject jsonObject = JSONObject.parseObject(content);
        ContentDTO datalist = JSONObject.parseObject(jsonObject.toJSONString(), ContentDTO.class);
        return JSONObject.toJSONString(datalist);
    }
}
