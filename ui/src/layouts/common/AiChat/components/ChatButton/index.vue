<template>
  <n-button
		v-show="!app.settingDrawerVisible"
    type="primary"
    :class="[{ '!right-430px': app.aiChatDrawerVisible }, app.aiChatDrawerVisible ? 'ease-out' : 'ease-in']"
    class="fixed top-300px right-14px z-10000 w-42px h-42px !p-0 transition-all duration-300 animation-show"
    @click="app.toggleAiChatDrawerVisible()"
  >
    <icon-ant-design-close-outlined v-if="app.aiChatDrawerVisible" class="text-24px" />
		<Icon icon="ph:chat-centered-dots-bold" v-else class="text-24px" />
  </n-button>
</template>

<script setup lang="ts">
import { useAppStore } from '@/store';
import { Icon } from '@iconify/vue';

defineOptions({ name: 'ChatButton' });

const app = useAppStore();
</script>

<style scoped>
.animation-show{
	animation:fadenum 400ms;
}
@keyframes fadenum{
	0%{opacity: 0;}
	30%{opacity: 0.3;}
	50%{opacity: 0.5;}
	70%{opacity: 0.7;}
	100%{opacity: 1;}
}
</style>
