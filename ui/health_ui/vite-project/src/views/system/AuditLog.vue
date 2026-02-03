<template>
  <div class="page-header">
    <div class="header-left">
      <h1>审计日志</h1>
      <p>按时间线记录系统关键操作，用于安全审计</p>
    </div>
  </div>

  <div class="card table-card">
    <table class="data-table">
      <thead>
        <tr>
          <th>操作时间</th>
          <th>操作人ID</th>
          <th>动作</th>
          <th>资源</th>
          <th>结果</th>
          <th>耗时</th>
          <th>IP地址</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.createdAt }}</td>
          <td>{{ item.userId }}</td>
          <td>{{ item.action }}</td>
          <td>{{ item.resource }}</td>
          <td>
            <span :class="item.result === 1 ? 'text-success' : 'text-error'">
              {{ item.result === 1 ? '成功' : '失败' }}
            </span>
          </td>
          <td>{{ item.costMs }}ms</td>
          <td>{{ item.ip }}</td>
        </tr>
      </tbody>
    </table>
    
    <div v-if="!list.length" class="empty-state">
      <ShieldCheck :size="48" />
      <p>暂无审计记录，请先在后台执行操作或检查接口定义</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ShieldCheck } from 'lucide-vue-next';
import { getAuditLogs } from '../../api/system';

const list = ref([]);

const fetchData = async () => {
  // Assuming list returns empty if endpoint not yet implemented but defined
  try {
    const res = await getAuditLogs({ page: 1, pageSize: 50 });
    list.value = res.list || [];
  } catch (e) {
    list.value = [];
  }
};

onMounted(fetchData);
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.table-card { padding: 0; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f8f9fa; padding: 12px 24px; text-align: left; }
.data-table td { padding: 16px 24px; border-bottom: 1px solid var(--border-color); font-size: 13px; }

.text-success { color: var(--success); }
.text-error { color: var(--error); }

.empty-state { padding: 64px; text-align: center; color: var(--text-secondary); }
.empty-state p { margin-top: 16px; }
</style>
