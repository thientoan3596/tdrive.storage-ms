package org.thluon.tdrive.config;
import com.github.thientoan3596.BytesToUUIDConverter;
import com.github.thientoan3596.UUIDToBytesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.thluon.tdrive.converter.ETypeToString;
import org.thluon.tdrive.converter.StringToEType;

import java.util.List;

@Configuration
public class R2dbcConf {
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(
                CustomConversions.StoreConversions.NONE,
                List.of(
                        new UUIDToBytesConverter(),
                        new BytesToUUIDConverter(),
                        new StringToEType(),
                        new ETypeToString()
                )
        );
    }

}
