package com.lvt.apps.myvote.ms.configs;

import com.optum.ofsc.bds.accounts.model.ResultType;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ResultTypeConverter implements Converter<String, ResultType> {
    @Override
    public ResultType convert(@NonNull String source) {
        return ResultType.fromValue(source);
    }
}

