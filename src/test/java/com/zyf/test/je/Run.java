package com.zyf.test.je;

import com.zyf.test.je.entity.MyYearSumReportDTO;
import com.zyf.test.je.utils.UnitConvertUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Run {
    public static void main(String[] args) {

        List<MyYearSumReportDTO> yearsList = getMyYearSumReportList();
        UnitConvertUtil.unitAnnotateConvert(yearsList);
        System.out.println("通过注解标识的自动转换玩法：" + yearsList.toString());
    }

    private static List<MyYearSumReportDTO> getMyYearSumReportList() {
        MyYearSumReportDTO mySumReportDTO = new MyYearSumReportDTO();
        mySumReportDTO.setPayTotalAmount(new BigDecimal(1100000));
        mySumReportDTO.setJcAmountPercentage(BigDecimal.valueOf(0.695));
        mySumReportDTO.setJcCountPermillage(BigDecimal.valueOf(0.7894));
        mySumReportDTO.setLength(BigDecimal.valueOf(1300.65112));
        mySumReportDTO.setWidth(BigDecimal.valueOf(6522.12344));
        MyYearSumReportDTO mySumReportDTO1 = new MyYearSumReportDTO();
        mySumReportDTO1.setPayTotalAmount(new BigDecimal(2390000));
        mySumReportDTO1.setJcAmountPercentage(BigDecimal.valueOf(0.885));
        mySumReportDTO1.setJcCountPermillage(BigDecimal.valueOf(0.2394));
        mySumReportDTO1.setLength(BigDecimal.valueOf(1700.64003));
        mySumReportDTO1.setWidth(BigDecimal.valueOf(7522.12344));

        List<MyYearSumReportDTO> list = new ArrayList<>();
        list.add(mySumReportDTO);
        list.add(mySumReportDTO1);
        return list;
    }

}
