<template>
  <n-divider title-placement="center">主题配置</n-divider>
  <textarea id="themeConfigCopyTarget" v-model="dataClipboardText" class="absolute opacity-0" />
  <n-space vertical>
    <n-button type="primary" :block="true" @click="handleExportData">导出数据</n-button>
    <n-button type="warning" :block="true" @click="handleImportData">导入数据</n-button>
  </n-space>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue';
import Clipboard from 'clipboard';
import { useThemeStore } from '@/store';

defineOptions({ name: 'TerminalConfig' });

const theme = useThemeStore();

const dataClipboardText = ref(getClipboardText());

function getClipboardText() {
  return JSON.stringify(theme.$state);
}

function handleResetConfig() {
  theme.resetThemeStore();
  window.$message?.success('已重置配置，请重新拷贝！');
}

const handleExportData = () => {};

const handleImportData = () => {};

const stopHandle = watch(
  () => theme.$state,
  () => {
    dataClipboardText.value = getClipboardText();
  },
  { deep: true }
);

onMounted(() => {});
onUnmounted(() => {
  stopHandle();
});
</script>

<style scoped></style>
