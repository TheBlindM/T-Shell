<template>
  <!-- :class="{ 'p-10px': showPadding }" -->
  <div
    :class="{ 'p-10px': showPadding }"
    class="h-full bg-[#f6f9f8] dark:bg-[#101014] transition duration-300 ease-in-out"
  >
    <router-view v-slot="{ Component, route }">
      <transition
        :name="theme.pageAnimateMode"
        mode="out-in"
        :appear="true"
        @before-leave="handleBeforeLeave"
        @after-enter="handleAfterEnter"
      >
        <keep-alive :include="routeStore.cacheRoutes">
          <component :is="Component" v-if="app.reloadFlag" :key="route.fullPath" />
        </keep-alive>
      </transition>
    </router-view>
    <!--    <n-tabs v-model:value="nameRef" type="card" closable tab-style="min-width: 80px;" @close="handleClose">
      <n-tab-pane v-for="panel in panelsRef" :key="panel" :tab="panel.toString()" :name="panel">
        {{ panel }}
      </n-tab-pane>
    </n-tabs>-->
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useMessage } from 'naive-ui';
import { useAppStore, useThemeStore, useRouteStore } from '@/store';

defineOptions({ name: 'GlobalContent' });

interface Props {
  /** 显示padding */
  showPadding?: boolean;
}

withDefaults(defineProps<Props>(), {
  showPadding: false
});

interface Emits {
  /** 禁止主体溢出 */
  (e: 'hide-main-overflow', hidden: boolean): void;
}

const emit = defineEmits<Emits>();

const app = useAppStore();
const theme = useThemeStore();
const routeStore = useRouteStore();

function handleBeforeLeave() {
  emit('hide-main-overflow', true);
}
function handleAfterEnter() {
  emit('hide-main-overflow', false);
}

const nameRef = ref(1);
const message = useMessage();
const panelsRef = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]);
function handleClose(name: number) {
  const { value: panels } = panelsRef;
  if (panels.length === 1) {
    message.error('最后一个了');
    return;
  }
  message.info(`关掉 ${name}`);
  const index = panels.findIndex(v => name === v);
  panels.splice(index, 1);
  if (nameRef.value === name) {
    nameRef.value = panels[index];
  }
}
</script>

<style scoped></style>
