package com.zyf.test.je.annotations;

import com.zyf.test.je.enums.UnitConvertType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author : JCccc
 * @CreateTime : 2023/01/14
 * @Description :
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JcBigDecConvert {

    UnitConvertType name();
}
