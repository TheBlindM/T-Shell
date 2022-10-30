<template>
  <div class="h-full" @contextmenu="disabledContextMenu">
    <dark-mode-container class="flex h-full" :inverted="theme.sider.inverted" @mouseleave="resetFirstDegreeMenus">
      <div class="flex-1 flex-col-stretch h-full">
        <global-logo :show-title="false" :style="{ height: theme.header.height + 'px' }" />
        <n-scrollbar class="flex-1-hidden">
          <mix-menu-detail
            v-for="item in firstDegreeMenus"
            :key="item.routeName"
            :route-name="item.routeName"
            :active-route-name="activeParentRouteName"
            :label="item.label"
            :icon="item.icon"
            :is-mini="app.siderCollapse"
            @click="handleMixMenu(item.routeName, item.hasChildren)"
          />
        </n-scrollbar>
        <mix-menu-collapse />
      </div>
      <mix-menu-drawer
        :visible="drawerVisible"
        :name="activeParentRouteName"
        @mouseleave="
          () => {
            menuDetailMouseLeave();
          }
        "
      />
    </dark-mode-container>
    <div
      v-show="drawerVisible && !app.mixSiderFixed"
      class="h-full"
      style="width: 100%; position: fixed; z-index: -1; background: rgb(111, 111, 111, 0.3); top: 0"
      @click="resetFirstDegreeMenus"
      @mouseover="
        () => {
          if (menuDetailMouseLeaveStatus && !outStatus) {
            if (app.drawerMouseleaveState) {
              hideDrawer();
            }
          }
        }
      "
      @mouseleave="ltest"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAppStore, useThemeStore, useRouteStore } from '@/store';
import { useRouterPush } from '@/composables';
import { useBoolean } from '@/hooks';
import { GlobalLogo } from '@/layouts/common';
import { disabledContextMenu } from '@/utils/common/contextmenu';
import { MixMenuDetail, MixMenuDrawer, MixMenuCollapse } from './components';

defineOptions({ name: 'VerticalMixSider' });

const route = useRoute();
const app = useAppStore();
const theme = useThemeStore();
const routeStore = useRouteStore();
const { routerPush } = useRouterPush();
const { bool: drawerVisible, setTrue: openDrawer, setFalse: hideDrawer } = useBoolean();
const { bool: menuDetailMouseLeaveStatus, setTrue: menuDetailMouseLeave, setFalse: menuDetailMouseover } = useBoolean();

const activeParentRouteName = ref('');
function setActiveParentRouteName(routeName: string) {
  activeParentRouteName.value = routeName;
}
const ltest = () => {
  console.log('离开');
  menuDetailMouseover();
};
const firstDegreeMenus = computed(() => [
  {
    routeName: 'host',
    label: '主机',
    icon: 'clarity:host-line',
    hasChildren: 'false'
  },
  {
    routeName: 'shortcutCmd',
    label: '快捷命令',
    icon: 'mingcute:lightning-line',
    hasChildren: 'false'
  },
  {
    routeName: 'globalVariable',
    label: '全局变量',
    icon: 'tabler:variable',
    hasChildren: 'false'
  }
]);

function getActiveParentRouteName() {
  /* firstDegreeMenus.value.some(item => {
    const routeName = (route.meta?.activeMenu ? route.meta.activeMenu : route.name) as string;
    const flag = routeName?.includes(item.routeName);
    if (flag) {
      setActiveParentRouteName(item.routeName);
    }
    return flag;
  }); */
}

function handleMixMenu(routeName: string, hasChildren: boolean) {
  console.log('handleMixMenu');
  setActiveParentRouteName(routeName);
  if (hasChildren) {
    outStatus = false;
    openDrawer();
  } else {
    routerPush({ name: routeName });
  }
}

function resetFirstDegreeMenus() {
  getActiveParentRouteName();
  if (app.drawerMouseleaveState) {
    hideDrawer();
  }
}

function onHideDrawer() {
  getActiveParentRouteName();

  hideDrawer();
}

const activeChildMenus = computed(() => {
  const menus: GlobalMenuOption[] = [];
  routeStore.menus.some(item => {
    const flag = item.routeName === activeParentRouteName.value && Boolean(item.children?.length);
    if (flag) {
      menus.push(...(item.children || []));
    }
    return flag;
  });
  return menus;
});

/* watch(
  () => route.name,
  () => {
    getActiveParentRouteName();
  },
  { immediate: true }
); */
let outStatus = false;
watch(
  () => app.drawerMouseleaveState,
  newVal => {
    if (newVal) {
      console.log('在外面');
      outStatus = true;
    } else {
      console.log('进入');
      outStatus = false;
    }
  },
  { immediate: true }
);
</script>

<style scoped></style>
