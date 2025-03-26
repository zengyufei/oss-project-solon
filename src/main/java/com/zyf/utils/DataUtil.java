//package com.zyf.utils;
//
//import cn.hutool.core.date.LocalDateTimeUtil;
//import cn.hutool.core.util.RandomUtil;
//import cn.hutool.core.util.StrUtil;
//import com.apifan.common.random.RandomSource;
//import com.apifan.common.random.constant.Province;
//import com.apifan.common.random.source.*;
//import com.github.javafaker.Faker;
//
//import java.time.LocalDate;
//import java.util.Locale;
//
//public class DataUtil {
//
//    public static final AreaSource areaSource = RandomSource.areaSource();
//    public static final LanguageSource languageSource = RandomSource.languageSource();
//    public static final DateTimeSource dateTimeSource = RandomSource.dateTimeSource();
//    public final static Faker faker = new Faker(new Locale("zh-CN"));
//    public final static Number number = faker.number();
//    public final static PersonInfoSource personInfoSource = RandomSource.personInfoSource();
//    public final static OtherSource otherSource = RandomSource.otherSource();
//
//    private DataUtil() {
//    }
//
//    public static String 随机零填充数字(int begin, int end, int len) {
//        return StrUtil.fillBefore(随机数字(begin, end), '0', len);
//    }
//
//    public static String 随机数字(int begin, int end) {
//        return String.valueOf(number.numberBetween(begin, end));
//    }
//
//    public static String 随机数字范围(int begin, int end) {
//        final int beginValue = number.numberBetween(begin, end);
//        final int endValue = number.numberBetween(begin, end);
//        return Math.min(beginValue, endValue) + "~" + Math.max(beginValue, endValue);
//    }
//
//    public static String 随机小数(int begin, int end, int scale) {
//        return String.valueOf(number.randomDouble(scale, begin, end));
//    }
//
//    public static String 随机数字(int len) {
//        return number.digits(len);
//    }
//
//    public static String 随机年龄() {
//        return 随机数字(1, 99);
//    }
//
//
//    public static String 随机身份证号码() {
//        final Province province = RandomUtil.randomEle(Province.values());
//        final LocalDate beginDate = LocalDate.of(2014, 1, 1);
//        final LocalDate endDate = LocalDate.of(2024, 4, 1);
//        if (RandomUtil.randomBoolean()) {
//            return personInfoSource.randomMaleIdCard(province, beginDate, endDate);
//        }
//        return personInfoSource.randomFemaleIdCard(province, beginDate, endDate);
//    }
//
//    public static String 随机省份() {
//        return areaSource.randomProvince();
//    }
//
//
//    public static String 随机性别() {
//        return personInfoSource.randomGender()==0?"女":"男";
//    }
//
//    public static String 随机省份城市() {
//        return areaSource.randomCity("");
//    }
//
//    public static String 随机公司名称() {
//        return otherSource.randomCompanyName(随机省份());
//    }
//
//    public static String 随机邮政编码() {
//        return areaSource.randomZipCode();
//    }
//
//    public static String 随机中国详细地址() {
//        return areaSource.randomAddress();
//    }
//
//    public static String 随机汉字(int 长度) {
//        return languageSource.randomChinese(长度);
//    }
//
//    public static String 随机汉字(int begin, int end) {
//        return languageSource.randomChinese(number.numberBetween(begin, end));
//    }
//
//    public static String 随机中文短句() {
//        return languageSource.randomChineseSentence();
//    }
//
//    public static String 随机固话号码() {
//        return areaSource.randomPhoneNumber(随机省份(), "-");
//    }
//
//    public static String 随机中文姓名() {
//        return personInfoSource.randomChineseName();
//    }
//
//    public static String 随机中国手机号码() {
//        return personInfoSource.randomChineseMobile();
//    }
//
//    public static String 随机民族名称() {
//        return otherSource.randomEthnicName();
//    }
//
//    public static String 随机行业名称() {
//        return otherSource.randomEconomicCategory().getName();
//    }
//
//    public static String 随机英文名() {
//        return personInfoSource.randomEnglishName();
//    }
//
//    public static String 随机英文(int len) {
//        return languageSource.randomEnglishText(len);
//    }
//
//    public static String 随机英文词组(int len) {
//        final StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < number.numberBetween(2, Math.max(3, len)); i++) {
//            sb.append(languageSource.randomEnglishText(number.numberBetween(3, 9)));
//        }
//        return sb.toString();
//    }
//
//    public static String 随机英文(int beginLen, int endLen) {
//        return languageSource.randomEnglishText(number.numberBetween(Math.max(beginLen, 2), endLen));
//    }
//
//    public static String 随机英文编号(int beginLen, int endLen) {
//        return languageSource.randomEnglishText(Math.max(beginLen, 2)) + number.digits(endLen);
//    }
//
//    public static String 随机英文编号(int beginLen, int endLen, int numLen) {
//        return languageSource.randomEnglishText(number.numberBetween(Math.max(beginLen, 2), endLen)) + number.digits(numLen);
//    }
//
//    public static String 随机行业编码() {
//        return otherSource.randomEconomicCategory().getCode();
//    }
//
//    public static String 随机部门名称() {
//        return otherSource.randomCompanyDepartment();
//    }
//
//    public static String 随机是否() {
//        return faker.regexify("是|否");
//    }
//
//    public static String 随机血型() {
//        return faker.regexify("A|B|O|AB");
//    }
//
//
//    public static String 随机自定义(String str) {
//        return faker.regexify(str);
//    }
//
//    public static String 随机有无() {
//        return faker.regexify("有|无");
//    }
//
//    public static String 随机0或1() {
//        return faker.regexify("0|1");
//    }
//
//    public static String 随机过去一年内日期() {
//        return LocalDateTimeUtil.format(dateTimeSource.randomPastTime(365), "yyyy-MM-dd") + " 00:00:00";
//    }
//
//    public static String 随机过去一年内日期时间() {
//        return LocalDateTimeUtil.format(dateTimeSource.randomPastTime(365), "yyyy-MM-dd HH:mm:ss");
//    }
//
//    public static String 随机过去十年内日期() {
//        return LocalDateTimeUtil.format(dateTimeSource.randomPastTime(3650), "yyyy-MM-dd") + " 00:00:00";
//    }
//
//    public static String 随机过去十年内日期时间() {
//        return LocalDateTimeUtil.format(dateTimeSource.randomPastTime(3650), "yyyy-MM-dd HH:mm:ss");
//    }
//}
