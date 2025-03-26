package com.zyf.utils;

import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

public class EqUtil {

    static boolean yes = true;
    static boolean no = false;

    public static boolean eq(Object a, Object b) {
        return eq(a, b, true);
    }

    public static boolean eq(Object a, Object b, boolean ignoreCase) {
        if (a == null && b == null) {
            return yes;
        } else if (a == null) {
            return no;
        } else if (b == null) {
            return no;
        } else {
            // 类型相等
            if (a.getClass().equals(b.getClass())) {
                if (isNumberClass(a.getClass()) && isNumberClass(b.getClass())) {
                    return new BigDecimal(Convert.toStr(a)).compareTo(new BigDecimal(Convert.toStr(b))) == 0;
                } else if (a instanceof CharSequence && b instanceof CharSequence) {
                    if (NumberUtil.isNumber(Convert.toStr(a)) && NumberUtil.isNumber(Convert.toStr(b))) {
                        return new BigDecimal(Convert.toStr(a)).compareTo(new BigDecimal(Convert.toStr(b))) == 0;
                    }
                    return StrUtil.equals((CharSequence) a, (CharSequence) b, ignoreCase);
                } else if (a instanceof Comparable && b instanceof Comparable) {
                    // 类型相同，直接进行比较
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comparableA = (Comparable<Object>) a;
                    return comparableA.compareTo(b) == 0;
                } else if (ArrayUtil.isArray(a) && ArrayUtil.isArray(b)) {
                    if (ignoreCase) {
                        final Object[] attrA = ArrayUtil.wrap(a);
                        final Object[] attrB = ArrayUtil.wrap(b);
                        if (attrA.length != attrB.length) {
                            return no;
                        } else {
                            return attrEqAttr(ignoreCase, attrA, attrB);
                        }
                    } else {
                        return ArrayUtil.equals(a, b);
                    }
                } else if (a instanceof Iterable<?> && b instanceof Iterable<?>) {
                    final Iterable<?> attrA = (Iterable<?>) a;
                    final Iterable<?> attrB = (Iterable<?>) b;
                    if (ignoreCase) {
                        if (IterUtil.size(attrA) != IterUtil.size(attrB)) {
                            return no;
                        } else {
                            return attrEqAttr(ignoreCase, attrA, attrB);
                        }
                    } else {
                        return IterUtil.isEqualList(attrA, attrB);
                    }
                } else {
                    return Convert.toStr(a).compareTo(Convert.toStr(b)) == 0;
                }
            }
            // 类型不相等
            else {
                // Comparable
                if (a instanceof Comparable && b instanceof Comparable) {
                    // Comparable 数组或集合1
                    if (ArrayUtil.isArray(a) || a instanceof Iterable<?>) {
                        // Comparable 数组
                        if (ArrayUtil.isArray(a)) {
                            final Object[] attrA = ArrayUtil.wrap(a);
                            // 处理 数组和集合
                            if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                                if (ArrayUtil.isArray(b)) {
                                    final Object[] attrB = ArrayUtil.wrap(b);
                                    if (attrA.length != attrB.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                } else {
                                    Iterable<?> attrB = (Iterable<?>) b;
                                    if (attrA.length != IterUtil.size(attrB)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                }
                            }
                            // 处理字符串
                            else if (b instanceof CharSequence) {
                                final List<String> bStrs = StrUtil.split((CharSequence) b, ",");
                                return attrEqAttr(ignoreCase, bStrs, attrA);
                            }
                            // 处理日期
                            else if (b instanceof Date) {
                                for (Object oA : attrA) {
                                    if (oA instanceof CharSequence) {
                                        if (StrUtil.equals(Convert.toStr(oA), Convert.toStr(b))) {
                                            return yes;
                                        }
                                    } else if (oA instanceof Date) {
                                        if (oA == b) {
                                            return yes;
                                        }
                                    } else if (oA instanceof TemporalAccessor) {
                                        if (LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oA) == ((Date) b).getTime()) {
                                            return yes;
                                        }
                                    }
                                }
                                return no;
                            }
                            // 处理数字
                            else {
                                return no;
                            }
                        }
                        // Comparable 集合
                        else {
                            Iterable<?> attrA = (Iterable<?>) a;
                            // 处理 Comparable 数组和集合
                            if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                                if (ArrayUtil.isArray(b)) {
                                    final Object[] attrB = ArrayUtil.wrap(b);
                                    if (IterUtil.size(attrA) != attrB.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                } else {
                                    Iterable<?> attrB = (Iterable<?>) b;
                                    if (IterUtil.size(attrA) != IterUtil.size(attrB)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                }
                            }
                            // 处理 Comparable 字符串
                            else if (b instanceof CharSequence) {
                                final List<String> bStrs = StrUtil.split((CharSequence) b, ",");
                                return attrEqAttr(ignoreCase, bStrs, attrA);
                            }
                            // 处理 Comparable 日期
                            else if (b instanceof Date) {
                                for (Object oA : attrA) {
                                    if (oA instanceof CharSequence) {
                                        if (StrUtil.equals(Convert.toStr(oA), Convert.toStr(b))) {
                                            return yes;
                                        }
                                    } else if (oA instanceof Date) {
                                        if (oA == b) {
                                            return yes;
                                        }
                                    } else if (oA instanceof TemporalAccessor) {
                                        if (LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oA) == ((Date) b).getTime()) {
                                            return yes;
                                        }
                                    }
                                }
                                return no;
                            }
                            // 处理数字
                            else {
                                return no;
                            }
                        }
                    }
                    // Comparable 数组或集合2
                    else if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                        // Comparable 数组
                        if (ArrayUtil.isArray(b)) {
                            final Object[] attrB = ArrayUtil.wrap(b);
                            // 处理 Comparable 数组
                            if (ArrayUtil.isArray(a)) {
                                final Object[] attrA = ArrayUtil.wrap(a);
                                if (attrB.length != attrA.length) {
                                    return no;
                                } else {
                                    return attrEqAttr(ignoreCase, attrB, attrA);
                                }
                            }
                            // 处理 Comparable 字符串
                            else if (a instanceof CharSequence) {
                                final List<String> aStrs = StrUtil.split((CharSequence) a, ",");
                                return attrEqAttr(ignoreCase, aStrs, attrB);
                            }
                            // 处理 Comparable 日期
                            else if (a instanceof Date) {
                                for (Object oB : attrB) {
                                    if (oB instanceof CharSequence) {
                                        if (StrUtil.equals(Convert.toStr(oB), Convert.toStr(a))) {
                                            return yes;
                                        }
                                    } else if (oB instanceof Date) {
                                        if (oB == a) {
                                            return yes;
                                        }
                                    } else if (oB instanceof TemporalAccessor) {
                                        if (LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oB) == ((Date) a).getTime()) {
                                            return yes;
                                        }
                                    }
                                }
                                return no;
                            }
                            // 处理 Comparable 数字
                            else {
                                return no;
                            }
                        } else {
                            Iterable<?> attrB = (Iterable<?>) b;
                            // 处理 Comparable 数组
                            if (ArrayUtil.isArray(a)) {
                                if (ArrayUtil.isArray(a)) {
                                    final Object[] attrA = ArrayUtil.wrap(a);
                                    if (IterUtil.size(attrB) != attrA.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrB, attrA);
                                    }
                                } else {
                                    Iterable<?> attrA = (Iterable<?>) a;
                                    if (IterUtil.size(attrB) != IterUtil.size(attrA)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrB, attrA);
                                    }
                                }
                            }
                            // 处理 Comparable 字符串
                            else if (a instanceof CharSequence) {
                                final List<String> aStrs = StrUtil.split((CharSequence) a, ",");
                                return attrEqAttr(ignoreCase, aStrs, attrB);
                            }
                            // 处理 Comparable 日期
                            else if (a instanceof Date) {
                                for (Object oB : attrB) {
                                    if (oB instanceof CharSequence) {
                                        if (StrUtil.equals(Convert.toStr(oB), Convert.toStr(a))) {
                                            return yes;
                                        }
                                    } else if (oB instanceof Date) {
                                        if (oB == a) {
                                            return yes;
                                        }
                                    } else if (oB instanceof TemporalAccessor) {
                                        if (LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oB) == ((Date) a).getTime()) {
                                            return yes;
                                        }
                                    }
                                }
                                return no;
                            }
                            // 处理 Comparable 数字
                            else {
                                return no;
                            }
                        }
                    }
                    // Comparable 处理 a=Date
                    else if (Date.class.isAssignableFrom(a.getClass())) {
                        if (b instanceof CharSequence) {
                            return timeEqTime((Date) a, (CharSequence) b);
                        } else if (b instanceof Date) {
                            return timeEqTime((Date) a, (Date) b);
                        } else if (b instanceof TemporalAccessor) {
                            return timeEqTime((Date) a, (TemporalAccessor) b);
                        } else {
                            return no;
                        }
                    }
                    // Comparable 处理 b=Date
                    else if (Date.class.isAssignableFrom(b.getClass())) {
                        if (a instanceof CharSequence) {
                            return timeEqTime((Date) b, (CharSequence) a);
                        } else if (a instanceof TemporalAccessor) {
                            return timeEqTime((TemporalAccessor) a, (Date) b);
                        } else {
                            return no;
                        }
                    }
                    // Comparable 处理 a=TemporalAccessor
                    else if (TemporalAccessor.class.isAssignableFrom(a.getClass())) {
                        if (b instanceof CharSequence) {
                            return timeEqTime((TemporalAccessor) a, (CharSequence) b);
                        } else if (b instanceof TemporalAccessor) {
                            return timeEqTime((TemporalAccessor) a, (TemporalAccessor) b);
                        } else {
                            return no;
                        }
                    }
                    // Comparable 处理 b=TemporalAccessor
                    else if (TemporalAccessor.class.isAssignableFrom(b.getClass())) {
                        if (a instanceof CharSequence) {
                            return timeEqTime((TemporalAccessor) b, (CharSequence) a);
                        } else {
                            return no;
                        }
                    } else if (NumberUtil.isNumber(Convert.toStr(a)) && NumberUtil.isNumber(Convert.toStr(b))) {
                        return new BigDecimal(Convert.toStr(a)).compareTo(new BigDecimal(Convert.toStr(b))) == 0;
                    }
                    // Comparable 例外
                    else {
                        // Comparable 类型不同，先转换为字符串再进行比较
                        return Convert.toStr(a).compareTo(Convert.toStr(b)) == 0;
                    }
                }
                // 非 Comparable
                else {
                    // 非 Comparable 数组或集合1
                    if (ArrayUtil.isArray(a) || a instanceof Iterable<?>) {
                        // 非 Comparable 数组
                        if (ArrayUtil.isArray(a)) {
                            final Object[] attrA = ArrayUtil.wrap(a);
                            if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                                if (ArrayUtil.isArray(b)) {
                                    final Object[] attrB = ArrayUtil.wrap(b);
                                    if (attrA.length != attrB.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                } else {
                                    Iterable<?> attrB = (Iterable<?>) b;
                                    if (attrA.length != IterUtil.size(attrB)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                }
                            } else if (b instanceof CharSequence) {
                                final List<String> bStrs = StrUtil.split((CharSequence) b, ",");
                                return attrEqAttr(ignoreCase, bStrs, attrA);
                            } else {
                                return no;
                            }
                        }
                        // 非 Comparable 集合
                        else {
                            Iterable<?> attrA = (Iterable<?>) a;
                            // 处理 非Comparable 数组和集合
                            if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                                if (ArrayUtil.isArray(b)) {
                                    final Object[] attrB = ArrayUtil.wrap(b);
                                    if (IterUtil.size(attrA) != attrB.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                } else {
                                    Iterable<?> attrB = (Iterable<?>) b;
                                    if (IterUtil.size(attrA) != IterUtil.size(attrB)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrA, attrB);
                                    }
                                }
                            }
                            // 处理 非Comparable 字符串
                            else if (b instanceof CharSequence) {
                                final List<String> bStrs = StrUtil.split((CharSequence) b, ",");
                                return attrEqAttr(ignoreCase, bStrs, attrA);
                            }
                            // 处理 非Comparable 日期
                            else if (b instanceof Date) {
                                for (Object oA : attrA) {
                                    if (oA instanceof CharSequence) {
                                        if (StrUtil.equals(Convert.toStr(oA), Convert.toStr(b))) {
                                            return yes;
                                        }
                                    } else if (oA instanceof Date) {
                                        if (oA == b) {
                                            return yes;
                                        }
                                    } else if (oA instanceof TemporalAccessor) {
                                        if (LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oA) == ((Date) b).getTime()) {
                                            return yes;
                                        }
                                    }
                                }
                                return no;
                            }
                            // 处理 非Comparable 数字
                            else {
                                return no;
                            }
                        }
                    }
                    // 非 Comparable 数组或集合2
                    else if (ArrayUtil.isArray(b) || b instanceof Iterable<?>) {
                        // Comparable 数组
                        if (ArrayUtil.isArray(b)) {
                            final Object[] attrB = ArrayUtil.wrap(b);
                            // 处理 Comparable 数组
                            if (ArrayUtil.isArray(a)) {
                                final Object[] attrA = ArrayUtil.wrap(a);
                                if (attrB.length != attrA.length) {
                                    return no;
                                } else {
                                    return attrEqAttr(ignoreCase, attrB, attrA);
                                }
                            }
                            // 处理 Comparable 字符串
                            else if (a instanceof CharSequence) {
                                final List<String> aStrs = StrUtil.split((CharSequence) a, ",");
                                return attrEqAttr(ignoreCase, aStrs, attrB);
                            }
                            // 处理 Comparable 日期
                            else if (a instanceof Date) {
                                for (Object oB : attrB) {
                                    if (oB instanceof CharSequence) {
                                        return StrUtil.equals(Convert.toStr(oB), Convert.toStr(a));
                                    } else if (oB instanceof Date) {
                                        return oB == a;
                                    } else if (oB instanceof TemporalAccessor) {
                                        return LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oB) == ((Date) a).getTime();
                                    }
                                }
                                return no;
                            }
                            // 处理 Comparable 数字
                            else {
                                return no;
                            }
                        } else {
                            Iterable<?> attrB = (Iterable<?>) b;
                            // 处理 Comparable 数组
                            if (ArrayUtil.isArray(a)) {
                                if (ArrayUtil.isArray(a)) {
                                    final Object[] attrA = ArrayUtil.wrap(a);
                                    if (IterUtil.size(attrB) != attrA.length) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrB, attrA);
                                    }
                                } else {
                                    Iterable<?> attrA = (Iterable<?>) a;
                                    if (IterUtil.size(attrB) != IterUtil.size(attrA)) {
                                        return no;
                                    } else {
                                        return attrEqAttr(ignoreCase, attrB, attrA);
                                    }
                                }
                            }
                            // 处理 Comparable 字符串
                            else if (a instanceof CharSequence) {
                                final List<String> aStrs = StrUtil.split((CharSequence) a, ",");
                                return attrEqAttr(ignoreCase, aStrs, attrB);
                            }
                            // 处理 Comparable 日期
                            else if (a instanceof Date) {
                                for (Object oB : attrB) {
                                    if (oB instanceof CharSequence) {
                                        return StrUtil.equals(Convert.toStr(oB), Convert.toStr(a));
                                    } else if (oB instanceof Date) {
                                        return oB == a;
                                    } else if (oB instanceof TemporalAccessor) {
                                        return LocalDateTimeUtil.toEpochMilli((TemporalAccessor) oB) == ((Date) a).getTime();
                                    }
                                }
                                return no;
                            }
                            // 处理 Comparable 数字
                            else {
                                return no;
                            }
                        }
                    }
                    // 非 Comparable 处理 a=Date
                    else if (Date.class.isAssignableFrom(a.getClass())) {
                        if (b instanceof CharSequence) {
                            return timeEqTime((Date) a, (CharSequence) b);
                        } else if (b instanceof TemporalAccessor) {
                            return timeEqTime((Date) a, (TemporalAccessor) b);
                        } else {
                            return no;
                        }
                    }
                    // 非 Comparable 处理 b=Date
                    else if (Date.class.isAssignableFrom(b.getClass())) {
                        if (a instanceof CharSequence) {
                            return timeEqTime((Date) b, (CharSequence) a);
                        } else if (a instanceof TemporalAccessor) {
                            return timeEqTime((TemporalAccessor) a, (Date) b);
                        } else {
                            return no;
                        }
                    }
                    // 非 Comparable 处理 a=TemporalAccessor
                    else if (TemporalAccessor.class.isAssignableFrom(a.getClass())) {
                        if (b instanceof CharSequence) {
                            return timeEqTime((TemporalAccessor) a, (CharSequence) b);
                        }
                        // localDate 和 LocalDateTime 的场景
                        else if (b instanceof TemporalAccessor) {
                            return timeEqTime((TemporalAccessor) a, (TemporalAccessor) b);
                        } else {
                            return no;
                        }
                    }
                    // 非 Comparable 处理 b=TemporalAccessor
                    else if (TemporalAccessor.class.isAssignableFrom(b.getClass())) {
                        if (a instanceof CharSequence) {
                            return timeEqTime((TemporalAccessor) b, (CharSequence) a);
                        }
                        // localDate 和 LocalDateTime 的场景
                        else {
                            return no;
                        }
                    }
                    // 非 Comparable 例外
                    else {
                        // 非 Comparable 类型不同，先转换为字符串再进行比较
                        return Convert.toStr(a).compareTo(Convert.toStr(b)) == 0;
                    }
                }
            }
        }
    }

    private static boolean timeEqTime(Date a, Date b) {
        final long aTime = a.getTime();
        final long bTime = b.getTime();
        return aTime == bTime;
    }

    private static boolean timeEqTime(Date a, TemporalAccessor b) {
        final long aTime = a.getTime();
        final long bTime = LocalDateTimeUtil.toEpochMilli(b);
        return aTime == bTime;
    }

    private static boolean timeEqTime(TemporalAccessor a, Date b) {
        final long bTime = b.getTime();
        final long aTime = LocalDateTimeUtil.toEpochMilli(a);
        return aTime == bTime;
    }

    private static boolean timeEqTime(TemporalAccessor a, TemporalAccessor b) {
        final long aTime = LocalDateTimeUtil.toEpochMilli(a);
        final long bTime = LocalDateTimeUtil.toEpochMilli(b);
        return aTime == bTime;
    }

    private static boolean timeEqTime(TemporalAccessor a, CharSequence b) {
        try {
            final long aTime = LocalDateTimeUtil.toEpochMilli(a);
            final long bTime = DateUtil.parse(b).getTime();
            return aTime == bTime;
        } catch (Exception e) {
            return no;
        }
    }

    private static boolean timeEqTime(Date a, CharSequence b) {
        try {
            final long aTime = a.getTime();
            final long bTime = DateUtil.parse(b).getTime();
            return aTime == bTime;
        } catch (Exception e) {
            return no;
        }
    }

    private static boolean attrEqAttr(boolean ignoreCase, Iterable<?> attrA, Object[] attrB) {
        for (Object oA : attrA) {
                final String strA = Convert.toStr(oA);
            boolean isFind = false;
            for (Object oB : attrB) {
                final String strB = Convert.toStr(oB);
                if (StrUtil.equals(strB, strA, ignoreCase)) {
                    isFind = true;
                    break;
                } else if (NumberUtil.isNumber(strA) && NumberUtil.isNumber(strB)
                        && BigDecimalUtil.toBigDecimal(strA).compareTo(BigDecimalUtil.toBigDecimal(strB)) == 0) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return no;
            }
        }
        return yes;
    }

    private static boolean attrEqAttr(boolean ignoreCase, Object[] attrA, Iterable<?> attrB) {
        for (Object oA : attrA) {
            final String strA = Convert.toStr(oA);
            boolean isFind = false;
            for (Object oB : attrB) {
                final String strB = Convert.toStr(oB);
                if (StrUtil.equals(strB, strA, ignoreCase)) {
                    isFind = true;
                    break;
                } else if (NumberUtil.isNumber(strA) && NumberUtil.isNumber(strB)
                        && BigDecimalUtil.toBigDecimal(strA).compareTo(BigDecimalUtil.toBigDecimal(strB)) == 0) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return no;
            }
        }
        return yes;
    }

    private static boolean attrEqAttr(boolean ignoreCase, Iterable<?> attrA, Iterable<?> attrB) {
        for (Object oA : attrA) {
            final String strA = Convert.toStr(oA);
            boolean isFind = false;
            for (Object oB : attrB) {
                final String strB = Convert.toStr(oB);
                if (StrUtil.equals(strB, strA, ignoreCase)) {
                    isFind = true;
                    break;
                } else if (NumberUtil.isNumber(strA) && NumberUtil.isNumber(strB)
                        && BigDecimalUtil.toBigDecimal(strA).compareTo(BigDecimalUtil.toBigDecimal(strB)) == 0) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return no;
            }
        }
        return yes;
    }

    private static boolean attrEqAttr(boolean ignoreCase, Object[] attrA, Object[] attrB) {
        for (Object oA : attrA) {
            final String strA = Convert.toStr(oA);
            boolean isFind = false;
            for (Object oB : attrB) {
                final String strB = Convert.toStr(oB);
                if (StrUtil.equals(strB, strA, ignoreCase)) {
                    isFind = true;
                    break;
                } else if (NumberUtil.isNumber(strA) && NumberUtil.isNumber(strB)
                        && BigDecimalUtil.toBigDecimal(strA).compareTo(BigDecimalUtil.toBigDecimal(strB)) == 0) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return no;
            }
        }
        return yes;
    }


    private static boolean isNumberClass(Class<?> clazz) {
        return clazz == int.class || clazz == long.class || clazz == double.class || clazz == short.class
                || clazz == Integer.class || clazz == Long.class || clazz == Double.class || clazz == Short.class || clazz == BigDecimal.class;
    }
}
