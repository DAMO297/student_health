package org.example.util;

/**
 * 体检参考范围工具类
 * 
 * 基于常见的中国成年人及大中学生健康标准：
 * 1. BMI: 中国成人标准 (WS/T 428-2013)
 * 2. 血压: 中国高血压防治指南标准
 * 3. 肺活量: 国家学生体质健康标准 (2014年修订)
 */
public class ReferenceRangeUtil {

    /**
     * 根据指标键、性别和年龄获取参考范围字符串
     *
     * @param metricKey 指标键 (e.g., "height", "bmi", "sbp")
     * @param gender    性别 (1:男, 2:女, 0:未知)
     * @param age       年龄 (可选，null 则默认为大学生/成人)
     * @return 格式化的参考范围描述
     */
    public static String getReferenceRange(String metricKey, Integer gender, Integer age) {
        if (metricKey == null)
            return "N/A";

        switch (metricKey.toLowerCase()) {
            case "height":
            case "weight":
                return "N/A"; // 身高体重本身不设正负参考值，主要看趋势和BMI

            case "bmi":
                // 中国成人标准：18.5-23.9 正常，24-27.9 超重，>=28 肥胖
                return "18.5 - 23.9 kg/m²";

            case "sbp":
            case "blood_pressure_sys":
                // 中国标准：正常收缩压 < 120，正常高值 120-139
                return "90 - 139 mmHg";

            case "dbp":
            case "blood_pressure_dia":
                // 中国标准：正常舒张压 < 80，正常高值 80-89
                return "60 - 89 mmHg";

            case "heart_rate":
            case "pulse":
                return "60 - 100 bpm";

            case "vision_left":
            case "vision_right":
            case "vision_l":
            case "vision_r":
                // 中国常用5.0对数视力表
                return "≥ 5.0";

            case "vital_capacity": // 肺活量 (ml)
                // 参考《国家学生体质健康标准》，大学生平均水平
                if (gender != null && gender == 1) { // 男
                    return "≥ 3100 ml";
                } else if (gender != null && gender == 2) { // 女
                    return "≥ 2000 ml";
                }
                return "2000 - 4500 ml";

            case "blood_glucose": // 血糖 (空腹)
                return "3.9 - 6.1 mmol/L";

            case "hemoglobin": // 血红蛋白
                if (gender != null && gender == 1)
                    return "130 - 175 g/L";
                if (gender != null && gender == 2)
                    return "115 - 150 g/L";
                return "115 - 175 g/L";

            default:
                return "见备注";
        }
    }

    /**
     * 兼容旧接口
     */
    public static String getReferenceRange(String metricKey, String genderStr) {
        Integer gender = 0;
        if ("男".equals(genderStr))
            gender = 1;
        else if ("女".equals(genderStr))
            gender = 2;
        return getReferenceRange(metricKey, gender, null);
    }
}
