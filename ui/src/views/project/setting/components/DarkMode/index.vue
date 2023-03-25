<template>
  <n-divider title-placement="center">主题模式</n-divider>
  <n-space vertical size="large">
    <SettingMenu label="深色主题">
      <n-switch :value="theme.darkMode" @update:value="theme.setDarkMode">
        <template #checked>
          <icon-mdi-white-balance-sunny class="text-14px text-primary" />
        </template>
        <template #unchecked>
          <icon-mdi-moon-waning-crescent class="text-14px text-primary" />
        </template>
      </n-switch>
    </SettingMenu>
    <SettingMenu label="跟随系统">
      <n-switch :value="theme.followSystemTheme" @update:value="theme.setFollowSystemTheme">
        <template #checked>
          <icon-ic-baseline-do-not-disturb class="text-14px text-primary" />
        </template>
        <template #unchecked>
          <icon-ic-round-hdr-auto class="text-14px text-primary" />
        </template>
      </n-switch>
    </SettingMenu>
  </n-space>
</template>

<script setup lang="ts" >
import { useThemeStore } from '@/store';
import SettingMenu from '../SettingMenu/index.vue';


const theme = useThemeStore();
</script>
<style scoped>
:deep(.n-switch__rail) {
  background-color: #000e1c !important;
}
</style>
