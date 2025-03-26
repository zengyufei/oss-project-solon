package com.zyf.utils.converters;

import cn.hutool.core.convert.AbstractConverter;
import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.*;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class CustomStringConverter extends AbstractConverter<String> {
    private static final long serialVersionUID = 1L;

    @Override
    protected String convertInternal(Object value) {
        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        } else if (value instanceof org.w3c.dom.Node) {
            return XmlUtil.toStr((org.w3c.dom.Node) value);
        } else if (value instanceof Clob) {
            return clobToStr((Clob) value);
        } else if (value instanceof Blob) {
            return blobToStr((Blob) value);
        } else if (value instanceof Type) {
            return ((Type) value).getTypeName();
        }

        // 其它情况
        return convertToStr(value);
    }

    /**
     * Clob字段值转字符串
     *
     * @param clob {@link Clob}
     * @return 字符串
     * @since 5.4.5
     */
    private static String clobToStr(Clob clob) {
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            return IoUtil.read(reader);
        } catch (SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoUtil.close(reader);
        }
    }

    /**
     * Blob字段值转字符串
     *
     * @param blob {@link Blob}
     * @return 字符串
     * @since 5.4.5
     */
    private static String blobToStr(Blob blob) {
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            return IoUtil.read(in, CharsetUtil.CHARSET_UTF_8);
        } catch (SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoUtil.close(in);
        }
    }

    /**
     * 值转为String，用于内部转换中需要使用String中转的情况<br>
     * 转换规则为：
     *
     * <pre>
     * 1、字符串类型将被强转
     * 2、数组将被转换为逗号分隔的字符串
     * 3、其它类型将调用默认的toString()方法
     * </pre>
     *
     * @param value 值
     * @return String
     */
    @Override
    protected String convertToStr(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof CharSequence) {
            return StrUtil.trim(value.toString());
        } else if (value instanceof final Date date) {
            return DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);
        } else if (value instanceof final LocalDate localDate) {
            return LocalDateTimeUtil.format(localDate, DatePattern.NORM_DATETIME_PATTERN);
        } else if (value instanceof final LocalDateTime localDateTime) {
            return LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_DATETIME_PATTERN);
        } else if (value instanceof final BigDecimal big) {
            return big.toPlainString();
        } else if (ArrayUtil.isArray(value)) {
            return ArrayUtil.toString(value);
        } else if (value instanceof final Iterable<?> it) {
            return StrJoiner.of(",").append(it).toString();
        } else if (CharUtil.isChar(value)) {
            //对于ASCII字符使用缓存加速转换，减少空间创建
            return CharUtil.toString((char) value);
        }
        return StrUtil.trim(value.toString());
    }
}
