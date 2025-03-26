package com.zyf.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

public class EqAnyUtil {

    static boolean yes = true;
    static boolean no = false;

    public static boolean eqAny(Object a, Object b) {
        return eqAny(a, b, true);
    }

    public static boolean eqAny(Object a, Object b, boolean ignoreCase) {
        if (a == null && b == null) {
            return no;
        } else if (a == null) {
            return no;
        } else if (b == null) {
            return no;
        } else {
            // 类型相等
            if (a.getClass().equals(b.getClass())) {
                // "" and ""
                // number and number、BigDecimal
                // array and array
                // list and list
                // date and date、localDateTime、localDate
                if (isNumberClass(a.getClass()) && isNumberClass(b.getClass())) {
                    // number and number、BigDecimal
                    final String strA = new BigDecimal(Convert.toStr(a)).toPlainString();
                    final String strB = new BigDecimal(Convert.toStr(b)).toPlainString();
                    return StrUtil.equals(strA, strB, ignoreCase);
                } else if (a instanceof CharSequence && b instanceof CharSequence) {
                    int scaleA = 0;
                    // number and number、BigDecimal
                    // 就还有 123L,56d 这些数字情况
                    String strA = Convert.toStr(a);
                    String strB = Convert.toStr(b);
                    if (NumberUtil.isNumber(strA)) {
                        final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                        scaleA = bigDecimalA.scale();
                        strA = bigDecimalA.toPlainString();
                    }
                    if (NumberUtil.isNumber(strB)) {
                        final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                        final int scaleB = bigDecimalB.scale();
                        if (scaleA > scaleB) {
                            strB = bigDecimalB.setScale(scaleA).toPlainString();
                        } else if (scaleA < scaleB) {
                            strB = bigDecimalB.toPlainString();
                            strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                        }
                    } else {
                        if (StrUtil.contains(strB, ",")) {
                            final List<String> attrB = StrUtil.splitTrim(strB, ",");
                            for (String oB : attrB) {
                                if (StrUtil.equals(strA, oB, ignoreCase)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                    // "" and ""
                    return StrUtil.equals(strA, strB, ignoreCase);
                } else if (ArrayUtil.isArray(a) && ArrayUtil.isArray(b)) {
                    // array and array
                    // 转为A数组元素是否包含b数组元素，需要全量包含
                    final Object[] attrA = ArrayUtil.wrap(a);
                    final Object[] attrB = ArrayUtil.wrap(b);
                    return attrEqAnyAttr(ignoreCase, attrA, attrB);
                } else if (a instanceof Iterable<?> && b instanceof Iterable<?>) {
                    // list and list
                    // 转为A数组元素是否包含b数组元素，需要全量包含
                    final Iterable<?> attrA = (Iterable<?>) a;
                    final Iterable<?> attrB = (Iterable<?>) b;
                    return attrEqAnyAttr(ignoreCase, attrA, attrB);
                } else {
                    final String strA = Convert.toStr(a);
                    final String strB = Convert.toStr(b);
                    return StrUtil.equals(strA, strB, ignoreCase);
                }
            }
            // 类型不相等
            else {
                // "" and number、BigDecimal
                // "" and date、localDateTime、localDate
                // "" and array
                // "" and list

                // array and ""
                // array and number、BigDecimal
                // array and date、localDateTime、localDate
                // array and list

                // list and ""
                // list and number、BigDecimal
                // list and date、localDateTime、localDate
                // list and array

                // number and ""
                // 不可达 number and date、localDateTime、localDate
                // 不可达 number and array
                // 不可达 number and list

                // date and ""
                // 不可达 date and number、BigDecimal
                // 不可达 date and array
                // 不可达 date and list

                if (a instanceof CharSequence strA) {
                    return strHandler(strA, b, ignoreCase);
                } else if (b instanceof CharSequence strB) {
                    return strHandler(strB, a, ignoreCase);
                } else if (ArrayUtil.isArray(a)) {
                    return attrHandler(ArrayUtil.wrap(a), b, ignoreCase);
                } else if (ArrayUtil.isArray(b)) {
                    return attrHandler(ArrayUtil.wrap(b), a, ignoreCase);
                } else if (a instanceof Iterable<?> attrA) {
                    return itHandler(attrA, b, ignoreCase);
                } else if (b instanceof Iterable<?> attrB) {
                    return itHandler(attrB, a, ignoreCase);
                } else if (isNumberClass(a.getClass()) || NumberUtil.isNumber(Convert.toStr(a))) {
                    return strHandler(Convert.toStr(a), b, ignoreCase);
                } else if (isNumberClass(b.getClass()) || NumberUtil.isNumber(Convert.toStr(b))) {
                    return strHandler(Convert.toStr(b), a, ignoreCase);
                } else if (a instanceof Date || a instanceof TemporalAccessor) {
                    return strHandler(Convert.toStr(a), b, ignoreCase);
                } else if (b instanceof Date || b instanceof TemporalAccessor) {
                    return strHandler(Convert.toStr(b), a, ignoreCase);
                } else {
                    return StrUtil.equals(Convert.toStr(a), Convert.toStr(b), ignoreCase);
                }
            }
        }

    }

    private static boolean itHandler(Iterable<?> attrA, Object b, boolean ignoreCase) {
        if (b instanceof CharSequence || isNumberClass(b.getClass())) {
            int scaleB = 0;
            String strB = Convert.toStr(b);
            // array and ""
            if (b instanceof CharSequence && NumberUtil.isNumber(strB)) {
                final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                scaleB = bigDecimalB.scale();
                strB = bigDecimalB.toPlainString();
            }
            boolean isFind;
            for (Object oA : attrA) {
                String strA = Convert.toStr(oA);

                if (NumberUtil.isNumber(strA)) {
                    final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                    final int scaleA = bigDecimalA.scale();
                    if (scaleB > scaleA) {
                        strA = bigDecimalA.setScale(scaleB).toPlainString();
                    } else if (scaleB < scaleA) {
                        strA = bigDecimalA.toPlainString();
                        strB = BigDecimalUtil.toBigDecimal(strB).setScale(scaleA).toPlainString();
                    }
                }
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
            return false;
        } else if (b instanceof Date || b instanceof TemporalAccessor) {
            // array and date、localDateTime、localDate
            final String strB = Convert.toStr(b);
            boolean isFind;
            for (Object oA : attrA) {
                final String strA = Convert.toStr(oA);
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
            return false;
        } else if (b instanceof final Iterable<?> attrB) {
            // array and list
            return attrEqAnyAttr(ignoreCase, attrA, attrB);
        } else {
            final String strB = Convert.toStr(b);
            boolean isFind;
            for (Object oA : attrA) {
                final String strA = Convert.toStr(oA);
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean attrHandler(Object[] attrA, Object b, boolean ignoreCase) {
        if (b instanceof CharSequence || isNumberClass(b.getClass())) {
            int scaleB = 0;
            String strB = Convert.toStr(b);
            // array and ""
            if (b instanceof CharSequence && NumberUtil.isNumber(strB)) {
                final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                scaleB = bigDecimalB.scale();
                strB = bigDecimalB.toPlainString();
            }
            for (Object oA : attrA) {
                String strA = Convert.toStr(oA);
                if (NumberUtil.isNumber(strA)) {
                    final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                    final int scaleA = bigDecimalA.scale();
                    if (scaleB > scaleA) {
                        strA = bigDecimalA.setScale(scaleB).toPlainString();
                    } else if (scaleB < scaleA) {
                        strA = bigDecimalA.toPlainString();
                        strB = BigDecimalUtil.toBigDecimal(strB).setScale(scaleA).toPlainString();
                    }
                }
                return StrUtil.equals(strA, strB, ignoreCase);
            }
            return false;
        } else if (b instanceof Date || b instanceof TemporalAccessor) {
            // array and date、localDateTime、localDate
            final String strB = Convert.toStr(b);
            boolean isFind;
            for (Object oA : attrA) {
                final String strA = Convert.toStr(oA);
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
            return false;
        } else if (ArrayUtil.isArray(b)) {
            final Object[] attrB = ArrayUtil.wrap(b);
            return attrEqAnyAttr(ignoreCase, attrA, attrB);
        } else if (b instanceof final Iterable<?> attrB) {
            // array and list
            return attrEqAnyAttr(ignoreCase, attrA, attrB);
        } else {
            final String strB = Convert.toStr(b);
            boolean isFind;
            for (Object oA : attrA) {
                final String strA = Convert.toStr(oA);
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean strHandler(CharSequence strA, Object b, boolean ignoreCase) {
        int scaleA = 0;
        if (b instanceof CharSequence strB) {
            // 就还有 123L,56d 这些数字情况
            if (NumberUtil.isNumber(strA)) {
                final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                scaleA = bigDecimalA.scale();
                strA = bigDecimalA.toPlainString();
            }
            if (NumberUtil.isNumber(strB)) {
                final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                final int scaleB = bigDecimalB.scale();
                if (scaleA > scaleB) {
                    strB = bigDecimalB.setScale(scaleA).toPlainString();
                } else if (scaleA < scaleB) {
                    strB = bigDecimalB.toPlainString();
                    strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                }
            }
            if (StrUtil.contains(strB, ",")) {
                final List<String> attrB = StrUtil.splitTrim(strB, ",");
                for (String oB : attrB) {
                    if (StrUtil.equals(strA, oB, ignoreCase)) {
                        return true;
                    }
                }
                return false;
            }
            // "" and ""
            return StrUtil.equals(strA, strB, ignoreCase);
        } else if (isNumberClass(b.getClass())) {
            // "" and number、BigDecimal
            String strB = Convert.toStr(b);
            if (NumberUtil.isNumber(strA)) {
                final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                scaleA = bigDecimalA.scale();
                strA = bigDecimalA.toPlainString();
            }
            if (NumberUtil.isNumber(strB)) {
                final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                final int scaleB = bigDecimalB.scale();
                if (scaleA > scaleB) {
                    strB = bigDecimalB.setScale(scaleA).toPlainString();
                } else if (scaleA < scaleB) {
                    strB = bigDecimalB.toPlainString();
                    strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                }
            }
            if (StrUtil.contains(strA, ",")) {
                final List<String> attrA = StrUtil.splitTrim(strA, ",");
                for (String oA : attrA) {
                    if (StrUtil.equals(strB, oA, ignoreCase)) {
                        return true;
                    }
                }
                return false;
            }
            return StrUtil.equals(strB, strA, ignoreCase);
        } else if (b instanceof Date || b instanceof TemporalAccessor) {
            // "" and date、localDateTime、localDate
            return StrUtil.equals(strA, Convert.toStr(b), ignoreCase);
        } else if (ArrayUtil.isArray(b)) {
            final Object[] attrB = ArrayUtil.wrap(b);
            if (StrUtil.contains(strA, ",")) {
                final List<String> attrA = StrUtil.splitTrim(strA, ",");
                return attrEqAnyAttr(ignoreCase, attrB, attrA);
            } else {
                // "" and array
                boolean isNotFind = true;
                for (Object oB : attrB) {
                    final String strB = Convert.toStr(oB);
                    if (StrUtil.equals(strB, strA, ignoreCase)) {
                        isNotFind = false;
                    }
                }
                if (isNotFind) {
                    return no;
                }
                return yes;
            }

        } else if (b instanceof final Iterable<?> attrB) {
            // "" and list
            for (Object oB : attrB) {
                final String strB = Convert.toStr(oB);
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return yes;
                }
            }
            return no;
        } else {
            return StrUtil.equals(strA, Convert.toStr(b), ignoreCase);
        }
    }


    private static boolean attrEqAnyAttr(boolean ignoreCase, Object[] attrA, Object[] attrB) {
        int scaleA = 0;
        for (Object oB : attrB) {
            String strB = Convert.toStr(oB);
            for (Object oA : attrA) {
                String strA = Convert.toStr(oA);
                if (NumberUtil.isNumber(strA)) {
                    final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                    scaleA = bigDecimalA.scale();
                    strA = bigDecimalA.toPlainString();
                }
                if (NumberUtil.isNumber(strB)) {
                    final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                    final int scaleB = bigDecimalB.scale();
                    if (scaleA > scaleB) {
                        strB = bigDecimalB.setScale(scaleA).toPlainString();
                    } else if (scaleA < scaleB) {
                        strB = bigDecimalB.toPlainString();
                        strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                    }
                }
                if (StrUtil.equals(strA, strB, ignoreCase)) {
                    return true;
                }
            }
        }
        return no;
    }

    private static boolean attrEqAnyAttr(boolean ignoreCase, Iterable<?> attrA, Iterable<?> attrB) {
        int scaleA = 0;
        for (Object oB : attrB) {
            String strB = Convert.toStr(oB);
            boolean isFind = false;
            for (Object oA : attrA) {
                String strA = Convert.toStr(oA);
                if (NumberUtil.isNumber(strA)) {
                    final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                    scaleA = bigDecimalA.scale();
                    strA = bigDecimalA.toPlainString();
                }
                if (NumberUtil.isNumber(strB)) {
                    final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                    final int scaleB = bigDecimalB.scale();
                    if (scaleA > scaleB) {
                        strB = bigDecimalB.setScale(scaleA).toPlainString();
                    } else if (scaleA < scaleB) {
                        strB = bigDecimalB.toPlainString();
                        strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                    }
                }
                if (StrUtil.equals(strA, strB, ignoreCase)) {
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


    private static boolean attrEqAnyAttr(boolean ignoreCase, Object[] attrA, Iterable<?> attrB) {
        int scaleA = 0;
        for (Object oB : attrB) {
            String strB = Convert.toStr(oB);
            boolean isFind = false;
            for (Object oA : attrA) {
                String strA = Convert.toStr(oA);
                if (NumberUtil.isNumber(strA)) {
                    final BigDecimal bigDecimalA = BigDecimalUtil.toBigDecimal(strA);
                    scaleA = bigDecimalA.scale();
                    strA = bigDecimalA.toPlainString();
                }
                if (NumberUtil.isNumber(strB)) {
                    final BigDecimal bigDecimalB = BigDecimalUtil.toBigDecimal(strB);
                    final int scaleB = bigDecimalB.scale();
                    if (scaleA > scaleB) {
                        strB = bigDecimalB.setScale(scaleA).toPlainString();
                    } else if (scaleA < scaleB) {
                        strB = bigDecimalB.toPlainString();
                        strA = BigDecimalUtil.toBigDecimal(strA).setScale(scaleB).toPlainString();
                    }
                }
                if (StrUtil.equals(strA, strB, ignoreCase)) {
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
