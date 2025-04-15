package org.thluon.tdrive.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.thluon.tdrive.entity.EType;

@WritingConverter
public class ETypeToString implements Converter<EType, String> {
    @Override
    public String convert(EType source) {
        return source.toString();
    }
}
