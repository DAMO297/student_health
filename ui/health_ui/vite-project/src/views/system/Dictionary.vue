<template>
  <div class="page-header">
    <div class="header-left">
      <h1>数据字典</h1>
      <p>管理系统下拉选项、状态定义等常量数据</p>
    </div>
    <button class="btn btn-primary" @click="openAddModal">
      <Plus :size="18" /> 新增项
    </button>
  </div>

  <div class="dict-layout">
    <div class="card table-card full-width">
      <table class="data-table">
        <thead>
          <tr>
            <th>类型编码</th>
            <th>显示名</th>
            <th>存储值</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td><code>{{ item.typeCode }}</code></td>
            <td>{{ item.label }}</td>
            <td>{{ item.value }}</td>
            <td>{{ item.sort }}</td>
            <td>
              <span class="badge" :class="item.status === 0 ? 'badge-success' : 'badge-error'">
                {{ item.status === 0 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="icon-btn" @click="handleDelete(item.id)"><Trash2 :size="16" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Plus, Trash2 } from 'lucide-vue-next';
import { getDictList, deleteDict } from '../../api/system';

const list = ref([]);

const fetchData = async () => {
  list.value = await getDictList();
};

const handleDelete = async (id) => {
  if (confirm('确定要删除该字典项吗？')) {
    await deleteDict(id);
    fetchData();
  }
};

onMounted(fetchData);
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; margin-bottom: 24px; }
.full-width { width: 100%; padding: 0; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f8f9fa; padding: 12px 24px; text-align: left; }
.data-table td { padding: 16px 24px; border-bottom: 1px solid var(--border-color); font-size: 14px; }
code { background: #f1f3f4; padding: 2px 4px; border-radius: 4px; color: var(--google-red); }

.badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; }
.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-error { background: #fce8e6; color: #d93025; }

.action-btns { display: flex; gap: 8px; }
.icon-btn { padding: 6px; border-radius: 4px; color: var(--text-secondary); }
.icon-btn:hover { background: #fce8e6; color: var(--google-red); }
</style>
