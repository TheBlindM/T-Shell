<template>
  <div
    class="relative h-full transition-width duration-300 ease-in-out"
    :style="{ width: app.mixSiderFixed ? theme.sider.mixChildMenuWidth + 'px' : '0px' }"
    @contextmenu="disabledContextMenu"
  >
    <dark-mode-container
      class="drawer-shadow absolute-lt flex-col-stretch h-full nowrap-hidden"
      :style="{ width: showDrawer ? theme.sider.mixChildMenuWidth + 'px' : '0px' }"
    >
      <header class="header-height flex-y-center justify-between" :style="{ height: theme.header.height + 'px' }">
        <h2 class="text-primary pl-8px text-16px font-bold">{{ title }}</h2>
        <div class="px-8px text-16px text-gray-600 cursor-pointer" @click="app.toggleMixSiderFixed">
          <icon-mdi-pin-off v-if="app.mixSiderFixed" />
          <icon-mdi-pin v-else />
        </div>
      </header>
      <n-scrollbar class="flex-1-hidden">
        <Host v-if="props.name === 'host'" />
        <OsType v-else-if="props.name === 'osType'" />
        <ShortcutCmd v-else-if="props.name === 'shortcutCmd'" />
        <GlobalVariable v-else-if="props.name === 'globalVariable'" />
      </n-scrollbar>
    </dark-mode-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useAppStore, useThemeStore } from '@/store';
import { useAppInfo } from '@/composables';
import { Host,ShortcutCmd,GlobalVariable } from '@/layouts/common/shell';
import { disabledContextMenu } from '@/utils/common/contextmenu';

defineOptions({ name: 'MixMenuDrawer' });

interface Props {
  /** 菜单抽屉可见性 */
  visible: boolean;
  name: string;
}

const props = defineProps<Props>();

const app = useAppStore();
const theme = useThemeStore();
const { title } = useAppInfo();

const showDrawer = computed(() => props.visible || app.mixSiderFixed);

/* watch(
  () => route.name,
  () => {
  },
  { immediate: true }
); */
</script>
<style scoped>
.drawer-shadow {
  box-shadow: 2px 0 8px 0 rgb(29 35 41 / 5%);
}
</style>
