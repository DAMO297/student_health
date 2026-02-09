<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="header-left">
        <h1>系统仪表盘</h1>
        <p>实时监控系统运行状态与数据变化</p>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="card stat-card">
        <div class="stat-icon" :style="{ backgroundColor: stat.color + '15', color: stat.color }">
          <component :is="stat.icon" :size="28" />
        </div>
        <div class="stat-info">
          <span class="stat-label">{{ stat.label }}</span>
          <div class="stat-value-row">
            <h2 class="stat-value">{{ stat.value }}</h2>
            <span v-if="stat.trend" class="stat-trend" :class="stat.trend > 0 ? 'trend-up' : 'trend-down'">
              <TrendingUp v-if="stat.trend > 0" :size="16" />
              <TrendingDown v-else :size="16" />
              {{ Math.abs(stat.trend) }}%
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-grid">
      <!-- Activity Trend Chart -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>体检活动趋势</h3>
          <span class="card-subtitle">近7日体检记录数量变化</span>
        </div>
        <div class="chart-container">
          <Line 
            v-if="activityChart.labels.length" 
            :data="activityChart" 
            :options="lineChartOptions" 
          />
          <div v-else class="loading-state">
            <div class="spinner"></div>
            <p>加载中...</p>
          </div>
        </div>
      </div>

      <!-- Report Status Chart -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>报告状态分布</h3>
          <span class="card-subtitle">当前系统中各状态报告占比</span>
        </div>
        <div class="chart-container chart-small">
          <Doughnut 
            v-if="reportStatusChart.datasets[0].data.reduce((a, b) => a + b, 0) > 0" 
            :data="reportStatusChart" 
            :options="doughnutChartOptions" 
          />
          <div v-else class="empty-state">
            <p>暂无报告数据</p>
            <span class="empty-hint">待审核: {{ reportStatusChart.datasets[0].data[0] }} | 已完成: {{ reportStatusChart.datasets[0].data[1] }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Data Tables -->
    <div class="tables-grid">
      <!-- College Stats -->
      <div class="card table-card">
        <div class="card-header">
          <h3>学院统计</h3>
          <span class="card-subtitle">各学院学生及体检情况</span>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>学院</th>
                <th>学生数</th>
                <th>体检数</th>
                <th>异常数</th>
                <th>完成率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="collegeStats.length === 0">
                <td colspan="5" style="text-align: center; color: var(--text-secondary)">
                  暂无数据
                </td>
              </tr>
              <tr v-for="item in collegeStats" :key="item.college">
                <td><strong>{{ item.college }}</strong></td>
                <td>{{ item.studentCount }}</td>
                <td>{{ item.examCount }}</td>
                <td>
                  <span class="badge badge-danger" v-if="item.abnormalCount > 0">
                    {{ item.abnormalCount }}
                  </span>
                  <span v-else>0</span>
                </td>
                <td>
                  <div class="progress-cell">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: item.rate + '%' }"></div>
                    </div>
                    <span class="progress-text">{{ item.rate }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="card table-card">
        <div class="card-header">
          <h3>最近活动</h3>
          <span class="card-subtitle">系统最新操作记录</span>
        </div>
        <div class="activity-list">
          <div v-if="recentActivity.length === 0" class="empty-state">
            <p>暂无活动记录</p>
          </div>
          <div v-for="(activity, index) in recentActivity" :key="index" class="activity-item">
            <div class="activity-icon">
              <component :is="getActivityIcon(activity.type)" :size="18" />
            </div>
            <div class="activity-content">
              <p class="activity-text">{{ activity.text }}</p>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { 
  Users, 
  Target, 
  CalendarCheck, 
  AlertTriangle,
  TrendingUp,
  TrendingDown,
  FileText,
  UserPlus,
  Activity as ActivityIcon
} from 'lucide-vue-next';
import { getOverview, getActivity, getCollegeStats } from '../../api/analysis';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Line, Doughnut } from 'vue-chartjs';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const stats = ref([
  { label: '学生总数', value: 0, icon: Users, color: '#4285F4', trend: 0 },
  { label: '体检批次', value: 0, icon: Target, color: '#34A853', trend: 0 },
  { label: '今日体检', value: 0, icon: CalendarCheck, color: '#FBBC05', trend: 0 },
  { label: '异常记录', value: 0, icon: AlertTriangle, color: '#EA4335', trend: 0 }
]);

const activityChart = reactive({
  labels: [],
  datasets: [
    {
      label: '体检数量',
      backgroundColor: 'rgba(66, 133, 244, 0.1)',
      borderColor: '#4285F4',
      data: [],
      tension: 0.4,
      fill: true,
      pointBackgroundColor: '#4285F4',
      pointRadius: 5,
      pointHoverRadius: 7
    }
  ]
});

const reportStatusChart = reactive({
  labels: ['待审核', '已完成'],
  datasets: [{
    data: [0, 0],
    backgroundColor: [
      '#FBBC05',
      '#34A853'
    ],
    borderWidth: 0
  }]
});

const collegeStats = ref([]);
const recentActivity = ref([]);

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: { size: 14 },
      bodyFont: { size: 13 }
    }
  },
  scales: {
    y: { 
      beginAtZero: true, 
      grid: { color: '#f0f0f0' },
      ticks: { font: { size: 12 } }
    },
    x: { 
      grid: { display: false },
      ticks: { font: { size: 12 } }
    }
  }
};

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        padding: 15,
        font: { size: 13 },
        usePointStyle: true
      }
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12
    }
  }
};

const getActivityIcon = (type) => {
  const icons = {
    'exam': CalendarCheck,
    'report': FileText,
    'student': UserPlus,
    'default': ActivityIcon
  };
  return icons[type] || icons.default;
};

onMounted(async () => {
  try {
    // Fetch overview data
    const overview = await getOverview();
    stats.value[0].value = overview.studentCount || 0;
    stats.value[0].trend = overview.studentTrend || 0;
    
    stats.value[1].value = overview.batchCount || 0;
    stats.value[1].trend = overview.batchTrend || 0;
    
    stats.value[2].value = overview.todayCheck || 0;
    stats.value[2].trend = overview.todayTrend || 0;
    
    stats.value[3].value = overview.abnormalCount || 0;
    stats.value[3].trend = overview.abnormalTrend || 0;

    // Fetch activity trend
    const activity = await getActivity();
    activityChart.labels = activity.map(item => item.date);
    activityChart.datasets[0].data = activity.map(item => item.count);

    // Report status: 1=待审核, 2=已完成
    reportStatusChart.datasets[0].data = [
      overview.reportPending || 0,
      overview.reportCompleted || 0
    ];

    // Fetch college stats from API
    const collegeData = await getCollegeStats();
    collegeStats.value = collegeData.map(item => ({
      college: item.college,
      studentCount: item.studentCount,
      examCount: item.examCount,
      abnormalCount: item.abnormalCount,
      rate: item.rate
    }));

    // Mock data for recent activity (替换为实际API调用)
    recentActivity.value = [
      { type: 'exam', text: '新增体检记录 - 学生 张三 (20220101)', time: '2分钟前' },
      { type: 'report', text: '生成健康报告 - 批次 2025秋季体检', time: '15分钟前' },
      { type: 'student', text: '新注册学生 - 李四 (计算机学院)', time: '1小时前' },
      { type: 'exam', text: '批量导入体检数据 - 成功320条', time: '2小时前' }
    ];

  } catch (e) {
    console.error('Dashboard data fetch failed:', e);
  }
});
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 8px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.header-left p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
}

.trend-up {
  color: #34A853;
  background: #E6F4EA;
}

.trend-down {
  color: #EA4335;
  background: #FCE8E6;
}

/* Charts Grid */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  padding: 24px;
}

.card-header {
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
}

.chart-container {
  height: 320px;
  position: relative;
}

.chart-small {
  height: 280px;
}

.loading-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 12px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #4285F4;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Tables Grid */
.tables-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.table-card {
  padding: 24px;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8f9fa;
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
}

.data-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-danger {
  background: #FCE8E6;
  color: #EA4335;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #4285F4, #34A853);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  min-width: 40px;
}

/* Activity List */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 36px;
  height: 36px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.5;
}

.activity-time {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 1200px) {
  .charts-grid,
  .tables-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
