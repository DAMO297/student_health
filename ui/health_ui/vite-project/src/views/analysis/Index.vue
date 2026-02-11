<template>
  <div class="analysis-page">
    <div class="page-header">
      <div class="header-left">
        <h1>高级数据分析</h1>
        <p>通过多维度统计与机器学习算法识别群体健康风险与趋势</p>
      </div>
    </div>

    <!-- Top Stats Rows -->
    <div class="analysis-grid">
      <!-- Gender Distribution -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>性别分布与异常率</h3>
          <span class="card-subtitle">按性别统计的异常记录占比</span>
        </div>
        <div class="chart-container">
          <Pie v-if="genderChart.datasets[0].data.length" :data="genderChart" :options="pieOptions" />
          <div v-else class="loading-state">加载中...</div>
        </div>
      </div>

      <!-- Grade Distribution -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>年级健康分布</h3>
          <span class="card-subtitle">各年级学生异常人数统计</span>
        </div>
        <div class="chart-container">
          <Bar v-if="gradeChart.datasets[0].data.length" :data="gradeChart" :options="barOptions" />
          <div v-else class="loading-state">加载中...</div>
        </div>
      </div>
    </div>

    <!-- Individual Health Trend Prediction -->
    <div class="card chart-card">
      <div class="card-header">
        <h3>个体健康趋势预测</h3>
        <span class="card-subtitle">搜索特定学生并选择指标，基于线性回归预测未来走势</span>
      </div>
      <div class="search-box">
        <input v-model="searchKeyword" placeholder="学号或姓名..." @keyup.enter="searchStudent" />
        <select v-model="selectedMetric">
          <option value="bmi">BMI</option>
          <option value="sbp">收缩压 (sbp)</option>
          <option value="dbp">舒张压 (dbp)</option>
        </select>
        <button @click="searchStudent" class="btn-primary">分析</button>
      </div>
      
      <div v-if="searchCandidates.length > 1 && !individualData.found" class="candidates-selection">
        <p class="selection-tip">找到多个匹配项，请选择：</p>
        <div class="candidate-list">
          <button v-for="c in searchCandidates" :key="c.id" @click="selectStudent(c)">
            {{ c.name }} ({{ c.studentNo }}) - {{ c.college }}
          </button>
        </div>
      </div>

      <div v-if="individualData.found" class="individual-result">
        <div class="prediction-info">
          <div class="student-meta">
            <span class="student-info-tag">{{ individualData.studentName }}</span>
            <span class="student-no">{{ individualData.studentNo }}</span>
          </div>
          <span class="trend-tag" :class="individualData.trend === '上升' ? 'trend-up' : (individualData.trend === '下降' ? 'trend-down' : 'trend-stable')">
            分析建议: {{ individualData.trend }}
          </span>
          <span class="prediction-val">未来预测: {{ individualData.prediction.toFixed(2) }}</span>
          <button class="btn-text" @click="clearSearch" v-if="searchCandidates.length > 1">重新选择</button>
        </div>
        <div class="chart-container-medium">
          <Line :data="individualTrendChart" :options="individualLineOptions" />
        </div>
      </div>
      <div v-else-if="searchKeyword && !individualData.found && !searchCandidates.length" class="empty-tip">未找到匹配的学生</div>

    </div>


    <!-- Trend Analysis -->
    <div class="card chart-card full-width">
      <div class="card-header">
        <h3>体检异常趋势分析与预测</h3>
        <span class="card-subtitle">各批次异常率变化情况 (包含线性回归预测趋势)</span>
      </div>
      <div class="chart-container-large">
        <Line v-if="trendChart.datasets[0].data.length" :data="trendChart" :options="lineOptions" />
        <div v-else class="loading-state">加载中...</div>
      </div>
    </div>

    <!-- Risk Clustering Analysis -->
    <div class="card chart-card full-width">
      <div class="card-header">
        <h3>群体健康风险识别 (K-Means 聚类)</h3>
        <span class="card-subtitle">基于 BMI 与 收缩压 的人群聚类分布，识别高风险群体</span>
      </div>
      <div class="chart-container-large">
        <Scatter v-if="riskChart.datasets[0].data.length" :data="riskChart" :options="scatterOptions" />
        <div v-else class="loading-state">加载中...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Filler
} from 'chart.js';
import { Bar, Line, Pie, Scatter } from 'vue-chartjs';
import { getGenderStats, getGradeStats, getTrendStats, getRiskClusters, getStudentTrend } from '../../api/analysis';
import { getStudentList } from '../../api/student';

ChartJS.register(
  Title, Tooltip, Legend, 
  BarElement, CategoryScale, LinearScale, 
  PointElement, LineElement, ArcElement, Filler
);

// --- Data & Charts ---

const searchKeyword = ref('');
const selectedMetric = ref('bmi');
const searchCandidates = ref([]);
const individualData = reactive({
  found: false,
  studentName: '',
  studentNo: '',
  trend: '',
  prediction: 0,
  currentId: null
});

const individualTrendChart = reactive({
  labels: [],
  datasets: []
});

const genderChart = reactive({
  labels: [],
  datasets: [{
    data: [],
    backgroundColor: ['#4285F4', '#EA4335', '#FBBC05']
  }]
});

const gradeChart = reactive({
  labels: [],
  datasets: [
    { label: '总人数', backgroundColor: '#e0e0e0', data: [] },
    { label: '异常人数', backgroundColor: '#EA4335', data: [] }
  ]
});

const trendChart = reactive({
  labels: [],
  datasets: [{
    label: '异常率 (%)',
    borderColor: '#4285F4',
    backgroundColor: 'rgba(66, 133, 244, 0.1)',
    data: [],
    fill: true,
    tension: 0.4
  }]
});

const riskChart = reactive({
  datasets: [
    { label: '集群A (健康)', backgroundColor: '#34A853', data: [] },
    { label: '集群B (关注)', backgroundColor: '#FBBC05', data: [] },
    { label: '集群C (风险)', backgroundColor: '#EA4335', data: [] }
  ]
});

// --- Options ---

const commonOptions = {
  responsive: true,
  maintainAspectRatio: false,
};

const pieOptions = { ...commonOptions, plugins: { legend: { position: 'bottom' } } };
const barOptions = { ...commonOptions, scales: { y: { beginAtZero: true } } };
const lineOptions = { ...commonOptions, scales: { y: { beginAtZero: true, ticks: { callback: v => v + '%' } } } };
const scatterOptions = {
  ...commonOptions,
  plugins: {
    tooltip: {
      callbacks: {
        label: function(context) {
          const p = context.raw;
          return `学生: ${p.studentName} (${p.studentNo})\nBMI: ${p.x}, 收缩压: ${p.y}`;
        }
      }
    }
  },
  scales: {
    x: { title: { display: true, text: 'BMI' } },
    y: { title: { display: true, text: '收缩压 (sbp)' } }
  }
};
const individualLineOptions = {
  ...commonOptions,
  scales: {
    y: { beginAtZero: false }
  }
};


const searchStudent = async () => {
  if (!searchKeyword.value) return;
  try {
    const students = await getStudentList({ keyword: searchKeyword.value, pageSize: 15 });
    searchCandidates.value = students.list || [];
    individualData.found = false;

    if (searchCandidates.value.length === 1) {
      selectStudent(searchCandidates.value[0]);
    }
  } catch (e) {
    console.error('Search failed:', e);
    searchCandidates.value = [];
  }
};

const selectStudent = async (student) => {
  try {
    const res = await getStudentTrend(student.id, selectedMetric.value);
    if (res && res.history) {
      individualData.found = true;
      individualData.currentId = student.id;
      individualData.studentName = student.name;
      individualData.studentNo = student.studentNo;
      individualData.trend = res.trend;
      individualData.prediction = res.prediction;

      individualTrendChart.labels = [...res.history.map(h => h.date), '预测'];
      
      const histData = res.history.map(h => h.value);
      const predDataFull = [...(new Array(histData.length - 1).fill(null)), histData[histData.length-1], res.prediction];
      
      individualTrendChart.datasets = [
        {
          label: '历史数据',
          borderColor: '#4285F4',
          backgroundColor: '#4285F4',
          data: [...histData, null],
          pointRadius: 5
        },
        {
          label: '预测点',
          borderColor: '#EA4335',
          backgroundColor: '#EA4335',
          data: predDataFull,
          borderDash: [5, 5],
          pointRadius: 8,
          pointStyle: 'rectRot'
        }
      ];
    } else {
      alert('该学生暂无足够的历史体检记录进行趋势分析');
    }
  } catch (e) {
    console.error('Fetch trend failed:', e);
  }
};

const clearSearch = () => {
  individualData.found = false;
  individualData.currentId = null;
};

import { watch } from 'vue';
watch(selectedMetric, () => {
  if (individualData.currentId) {
    selectStudent({ id: individualData.currentId, name: individualData.studentName, studentNo: individualData.studentNo });
  }
});


onMounted(async () => {
  try {
    const genders = await getGenderStats();
    genderChart.labels = genders.map(g => g.gender);
    genderChart.datasets[0].data = genders.map(g => g.count);

    const grades = await getGradeStats();
    gradeChart.labels = grades.map(g => g.grade);
    gradeChart.datasets[0].data = grades.map(g => g.count);
    gradeChart.datasets[1].data = grades.map(g => g.abnormalCount);

    const trends = await getTrendStats();
    // Reverse to show oldest to newest
    const sortedTrends = [...trends].reverse();
    trendChart.labels = sortedTrends.map(t => t.batchName);
    trendChart.datasets[0].data = sortedTrends.map(t => t.rate);

    const clusters = await getRiskClusters();
    clusters.forEach(p => {
      const clusterIdx = p.cluster % 3;
      riskChart.datasets[clusterIdx].data.push({ 
        x: p.bmi, 
        y: p.sbp,
        studentName: p.studentName,
        studentNo: p.studentNo
      });
    });

  } catch (e) {
    console.error('Analysis data load failed:', e);
  }
});
</script>

<style scoped>
.analysis-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 500;
  margin-bottom: 4px;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.chart-card {
  padding: 20px;
}

.full-width {
  grid-column: span 1;
}

@media (min-width: 1200px) {
  .full-width {
    grid-column: span 1;
  }
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-container-medium {
  height: 350px;
  position: relative;
  margin-top: 20px;
}

.chart-container-large {
  height: 400px;
  position: relative;
}

.search-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-box input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.search-box select {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.search-box button {
  padding: 8px 20px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.individual-result {
  border-top: 1px dashed var(--border-color);
  padding-top: 20px;
}

.prediction-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.candidates-selection {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.selection-tip {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.candidate-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.candidate-list button {
  padding: 6px 12px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.candidate-list button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: #f0f7ff;
}

.student-meta {
  display: flex;
  flex-direction: column;
}

.student-no {
  font-size: 12px;
  color: var(--text-secondary);
}

.btn-text {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  margin-left: auto;
}

.trend-stable { background: #e8f0fe; color: #1967d2; }

.empty-tip {
  color: var(--text-secondary);
  padding: 20px 0;
  text-align: center;
}

.loading-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}
</style>
