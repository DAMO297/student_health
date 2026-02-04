// 体检指标参考范围配置和智能判断算法
// 用于前端显示和异常值检测

/**
 * 体检指标参考范围配置
 * 支持性别差异和分级判断
 */
export const REFERENCE_RANGES = {
    // BMI (Body Mass Index)
    bmi: {
        name: 'BMI',
        unit: '',
        ranges: {
            underweight: { max: 18.5, label: '偏瘦', severity: 'warning' },
            normal: { min: 18.5, max: 23.9, label: '正常', severity: 'normal' },
            overweight: { min: 24, max: 27.9, label: '超重', severity: 'warning' },
            obese: { min: 28, label: '肥胖', severity: 'danger' }
        },
        getRange: () => '18.5-23.9',
        checkAbnormal: (value) => {
            if (value < 18.5) return { abnormal: true, severity: 'warning', msg: '偏瘦' };
            if (value >= 24 && value < 28) return { abnormal: true, severity: 'warning', msg: '超重' };
            if (value >= 28) return { abnormal: true, severity: 'danger', msg: '肥胖' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 身高 (Height)
    height: {
        name: '身高',
        unit: 'cm',
        getRange: (gender) => {
            // 1=男，2=女
            return gender === 1 ? '160-185' : '150-175';
        },
        checkAbnormal: (value, gender) => {
            // 仅提示，不标记为异常
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 体重 (Weight)
    weight: {
        name: '体重',
        unit: 'kg',
        getRange: () => '根据BMI判断',
        checkAbnormal: (value) => {
            // 体重异常通过BMI判断
            return { abnormal: false, severity: 'normal', msg: '-' };
        }
    },

    // 收缩压 (Systolic Blood Pressure)
    sbp: {
        name: '收缩压',
        unit: 'mmHg',
        ranges: {
            low: { max: 90, label: '偏低', severity: 'warning' },
            normal: { min: 90, max: 139, label: '正常', severity: 'normal' },
            hypertension1: { min: 140, max: 159, label: '高血压1级', severity: 'warning' },
            hypertension2: { min: 160, max: 179, label: '高血压2级', severity: 'danger' },
            hypertension3: { min: 180, label: '高血压3级', severity: 'danger' }
        },
        getRange: () => '90-139',
        checkAbnormal: (value) => {
            if (value < 90) return { abnormal: true, severity: 'warning', msg: '血压偏低' };
            if (value >= 140 && value < 160) return { abnormal: true, severity: 'warning', msg: '高血压1级' };
            if (value >= 160) return { abnormal: true, severity: 'danger', msg: '高血压2级以上' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 舒张压 (Diastolic Blood Pressure)
    dbp: {
        name: '舒张压',
        unit: 'mmHg',
        ranges: {
            low: { max: 60, label: '偏低', severity: 'warning' },
            normal: { min: 60, max: 89, label: '正常', severity: 'normal' },
            hypertension1: { min: 90, max: 99, label: '高血压1级', severity: 'warning' },
            hypertension2: { min: 100, max: 109, label: '高血压2级', severity: 'danger' },
            hypertension3: { min: 110, label: '高血压3级', severity: 'danger' }
        },
        getRange: () => '60-89',
        checkAbnormal: (value) => {
            if (value < 60) return { abnormal: true, severity: 'warning', msg: '血压偏低' };
            if (value >= 90 && value < 100) return { abnormal: true, severity: 'warning', msg: '高血压1级' };
            if (value >= 100) return { abnormal: true, severity: 'danger', msg: '高血压2级以上' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 心率 (Heart Rate)
    heart_rate: {
        name: '心率',
        unit: 'bpm',
        ranges: {
            low: { max: 60, label: '心动过缓', severity: 'warning' },
            normal: { min: 60, max: 100, label: '正常', severity: 'normal' },
            high: { min: 100, label: '心动过速', severity: 'warning' }
        },
        getRange: () => '60-100',
        checkAbnormal: (value) => {
            if (value < 60) return { abnormal: true, severity: 'warning', msg: '心动过缓' };
            if (value > 100) return { abnormal: true, severity: 'warning', msg: '心动过速' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 左眼视力 (Left Eye Vision)
    vision_l: {
        name: '左眼视力',
        unit: '',
        ranges: {
            poor: { max: 4.0, label: '严重不良', severity: 'danger' },
            low: { min: 4.0, max: 4.9, label: '视力不良', severity: 'warning' },
            normal: { min: 5.0, label: '正常', severity: 'normal' }
        },
        getRange: () => '≥5.0',
        checkAbnormal: (value) => {
            if (value < 4.0) return { abnormal: true, severity: 'danger', msg: '视力严重不良' };
            if (value < 5.0) return { abnormal: true, severity: 'warning', msg: '视力不良' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    },

    // 右眼视力 (Right Eye Vision)
    vision_r: {
        name: '右眼视力',
        unit: '',
        ranges: {
            poor: { max: 4.0, label: '严重不良', severity: 'danger' },
            low: { min: 4.0, max: 4.9, label: '视力不良', severity: 'warning' },
            normal: { min: 5.0, label: '正常', severity: 'normal' }
        },
        getRange: () => '≥5.0',
        checkAbnormal: (value) => {
            if (value < 4.0) return { abnormal: true, severity: 'danger', msg: '视力严重不良' };
            if (value < 5.0) return { abnormal: true, severity: 'warning', msg: '视力不良' };
            return { abnormal: false, severity: 'normal', msg: '正常' };
        }
    }
};

/**
 * 根据指标键获取参考范围文本
 * @param {string} metricKey - 指标键（如 'bmi', 'sbp'）
 * @param {number} gender - 性别 (1=男, 2=女)
 * @returns {string} 参考范围文本
 */
export function getReferenceRange(metricKey, gender = null) {
    const config = REFERENCE_RANGES[metricKey];
    if (!config) return '-';

    return typeof config.getRange === 'function'
        ? config.getRange(gender)
        : '-';
}

/**
 * 检测指标值是否异常
 * @param {string} metricKey - 指标键
 * @param {number} value - 指标值
 * @param {number} gender - 性别 (1=男, 2=女)
 * @returns {object} { abnormal, severity, msg }
 */
export function checkAbnormal(metricKey, value, gender = null) {
    const config = REFERENCE_RANGES[metricKey];
    if (!config || value === null || value === undefined) {
        return { abnormal: false, severity: 'normal', msg: '-' };
    }

    return config.checkAbnormal(value, gender);
}

/**
 * 批量检测多个指标并返回异常汇总
 * @param {Array} metrics - 指标数组
 * @param {number} gender - 性别
 * @returns {object} { hasAbnormal, abnormalCount, details }
 */
export function checkAllMetrics(metrics, gender = null) {
    const results = metrics.map(m => ({
        ...m,
        check: checkAbnormal(m.metricKey, m.valueDecimal, gender)
    }));

    const abnormalItems = results.filter(r => r.check.abnormal);

    return {
        hasAbnormal: abnormalItems.length > 0,
        abnormalCount: abnormalItems.length,
        dangerCount: abnormalItems.filter(r => r.check.severity === 'danger').length,
        warningCount: abnormalItems.filter(r => r.check.severity === 'warning').length,
        details: results
    };
}
