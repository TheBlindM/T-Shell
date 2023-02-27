<template>
  <n-drawer :show="app.aiChatDrawerVisible" display-directive="show" :width="730"  @mask-click="app.closeAiChatDrawer">
    <n-drawer-content title="小T机器人" :native-scrollbar="false" style="height: 100%">
     <chat />
    </n-drawer-content>
  </n-drawer>
  <chat-button />
</template>

<script setup lang="ts">
import { useAppStore } from '@/store';
import { ChatButton,Chat } from './components';

defineOptions({ name: 'AiChat' });

const app = useAppStore();
</script>


