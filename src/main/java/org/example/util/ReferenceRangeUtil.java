package org.example.util;

/**
 * 这是一个简单的参考范围工具类，用于解决 ReportService 中的编译错误。
 * 实际项目中，这些参考范围应该配置在数据库或更复杂的规则引擎中。
 */
public class ReferenceRangeUtil {

    /**
     * 根据指标键和性别获取参考范围描述
     *
     * @param metricKey 指标键 (e.g., "height", "weight", "sbp", "dbp")
     * @param gender    性别 (e.g., "男", "女", 或 null)
     * @return 参考范围字符串
     */
    public static String getReferenceRange(String metricKey, String gender) {
        if (metricKey == null) {
            return "";
        }

        switch (metricKey.toLowerCase()) {
            case "height":
            case "s身高":
                return "N/A"; // 身高通常没有参考范围
            case "weight":
            case "t体重":
                return "N/A"; // 体重通常结合身高看BMI
            case "bmi":
                return "18.5 - 23.9";
            case "sbp": // 收缩压
                return "90 - 140 mmHg";
            case "dbp": // 舒张压
                return "60 - 90 mmHg";
            case "heart_rate":
            case "xl心率":
                return "60 - 100 次/分";
            case "vision_left":
            case "vision_right":
                return "≥ 5.0";
            case "vital_capacity": // 肺活量
                if ("男".equals(gender))
                    return "≥ 2400 ml";
                if ("女".equals(gender))
                    return "≥ 2000 ml";
                return "2000 - 4000 ml";
            default:
                return "正常范围";
        }
    }
}
