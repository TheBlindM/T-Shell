<template>
  <admin-layout
    :mode="mode"
    :is-mobile="isMobile"
    :fixed-header-and-tab="theme.fixedHeaderAndTab"
    :header-height="0"
    :tab-visible="theme.tab.visible"
    :tab-height="theme.tab.height"
    :sider-visible="siderVisible"
    :sider-width="siderWidth"
    :sider-collapsed-width="siderCollapsedWidth"
    :sider-collapse="app.siderCollapse"
    :add-main-overflow-hidden="addMainOverflowHidden"
    :fixed-footer="theme.footer.fixed"
    :footer-height="theme.footer.height"
    @update:sider-collapse="app.setSiderCollapse"
  >
    <!--    <template #header>
      <global-header v-bind="headerProps" />
    </template>-->
    <template #tab>
      <global-tab />
    </template>
    <template #sider>
      <global-sider />
    </template>
    <global-content @hide-main-overflow="setAddMainOverflowHidden" />
    <!--    <template #footer>
      <global-footer />
    </template>-->
  </admin-layout>
  <global-back-top />
	<ai-chat/>
</template>

<script setup lang="ts">
import AdminLayout from '@soybeanjs/vue-admin-layout';
import { useAppStore, useThemeStore } from '@/store';
import { useBasicLayout } from '@/composables';
import { useBoolean } from '@/hooks';
import {
  GlobalHeader,
  GlobalTab,
  GlobalSider,
  GlobalContent,
  GlobalFooter,
  GlobalBackTop,
	AiChat
} from '../common';

defineOptions({ name: 'BasicLayout' });

const app = useAppStore();
const theme = useThemeStore();

const { mode, isMobile, headerProps, siderVisible, siderWidth, siderCollapsedWidth } = useBasicLayout();

const { bool: addMainOverflowHidden, setBool: setAddMainOverflowHidden } = useBoolean();
</script>

<style scoped></style>
