package org.thluon.tdrive.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.thluon.tdrive.entity.EType;

@ReadingConverter
public class StringToEType implements Converter<String, EType> {
    @Override
    public EType convert(String source) {
        if(source.equals("File")) return EType.File;
        return EType.Folder;
    }
}
