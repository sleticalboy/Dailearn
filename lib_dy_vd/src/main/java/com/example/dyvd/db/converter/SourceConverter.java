package com.example.dyvd.db.converter;

import com.example.dyvd.DySource;
import com.example.dyvd.db.Converter;

/**
 * Created on 2022/11/05
 *
 * @author binlee
 */
public final class SourceConverter implements Converter<DySource, Integer> {
    @Override
    public Integer encode(final DySource input) {
        return input.ordinal();
    }

    @Override
    public DySource decode(final Integer input) {
        return DySource.values()[input];
    }
}
