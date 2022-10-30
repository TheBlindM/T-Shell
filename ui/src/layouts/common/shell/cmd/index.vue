<!--<template>-->
<!--  <div style="padding: 5px 10px; height: 100%">-->
<!--    &lt;!&ndash;		<div style="margin: 0 auto">&ndash;&gt;-->
<!--    <n-space style="width: 260px">-->
<!--      <n-input v-model:value="patternRef" placeholder="搜索" />-->
<!--      <n-button-->
<!--        strong-->
<!--        secondary-->
<!--        @click="-->
<!--          showAddCmdPageModal = true;-->
<!--          currentCmdIdRef = 0;-->
<!--        "-->
<!--      >-->
<!--        <Icon class="text-primary" icon="material-symbols:add" />-->
<!--      </n-button>-->
<!--      &lt;!&ndash;		</div>&ndash;&gt;-->
<!--    </n-space>-->
<!--    <n-tree-->
<!--      block-line-->
<!--      selectable-->
<!--      key-field="id"-->
<!--      label-field="name"-->
<!--      children-field="children"-->
<!--      :pattern="patternRef"-->
<!--      :data="dataRef"-->
<!--      :node-props="nodeProps"-->
<!--      style="height: 100%; width: 100%"-->
<!--    />-->
<!--    &lt;!&ndash;	@drop="handleDrop"-->
<!--		:node-props="nodeProps"&ndash;&gt;-->
<!--    &lt;!&ndash; 又键菜单 &ndash;&gt;-->
<!--    <n-dropdown-->
<!--      trigger="manual"-->
<!--      placement="bottom-start"-->
<!--      :show="showDropdownRef"-->
<!--      :options="optionsRef"-->
<!--      :x="xRef"-->
<!--      :y="yRef"-->
<!--      @select="handleSelect"-->
<!--      @clickoutside="handleClickoutside"-->
<!--    />-->

<!--    <n-modal v-model:show="showAddCmdPageModal" :show-icon="false" preset="dialog" title="新建系统">-->
<!--      <AddCmd :parent-id="currentCmdIdRef" @change-show-modal="changeAddGroupShowModal" @refreshTree="initGroupTree" />-->
<!--    </n-modal>-->
<!--    <n-modal v-model:show="showUpdCmdPageModal" :show-icon="false" preset="dialog" title="更新系统">-->
<!--      <UpdCmd :cmd-id="currentCmdIdRef" @change-show-modal="changeUpdGroupShowModal" @refreshTree="initGroupTree" />-->
<!--    </n-modal>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup lang="ts">-->
<!--import { ref } from 'vue';-->
<!--import { useRouter } from 'vue-router';-->
<!--import type { TreeOption } from 'naive-ui';-->
<!--import { useMessage, useDialog } from 'naive-ui';-->
<!--import { Icon } from '@iconify/vue';-->
<!--import { useAppStore } from '@/store';-->
<!--import { useRouterPush } from '@/composables';-->
<!--import { tree, del as delCmd, partUpd as partUpdCmd } from '@/theblind_shell/service/shell/cmd';-->
<!--import { AddCmd, UpdCmd } from './components/index';-->

<!--const dialog = useDialog();-->
<!--const app = useAppStore();-->
<!--const router = useRouter();-->
<!--const { routerPush } = useRouterPush();-->

<!--const showAddCmdPageModal = ref(false);-->
<!--const showUpdCmdPageModal = ref(false);-->

<!--const currentCmdIdRef = ref(0);-->

<!--const changeAddGroupShowModal = (value: boolean) => {-->
<!--  showAddCmdPageModal.value = value;-->
<!--};-->

<!--const changeUpdGroupShowModal = (value: boolean) => {-->
<!--  showUpdCmdPageModal.value = value;-->
<!--};-->

<!--/* 获取真实id */-->
<!--const getRealId = (id: number) => {-->
<!--  return id < 0 ? id * -1 : id;-->
<!--};-->
<!--function findSiblingsAndIndex(node: TreeOption, nodes?: TreeOption[]): [TreeOption[], number] | [null, null] {-->
<!--  if (!nodes) return [null, null];-->
<!--  // eslint-disable-next-line no-plusplus-->
<!--  for (let i = 0; i < nodes.length; ++i) {-->
<!--    const siblingNode = nodes[i];-->
<!--    if (siblingNode.id === node.id) return [nodes, i];-->
<!--    const [siblings, index] = findSiblingsAndIndex(node, siblingNode.children);-->
<!--    if (siblings && index !== null) return [siblings, index];-->
<!--  }-->
<!--  return [null, null];-->
<!--}-->

<!--// eslint-disable-next-line consistent-return-->
<!--const findParent = (id: number, datas: any, parentId: number) => {-->
<!--  // eslint-disable-next-line no-restricted-syntax-->
<!--  for (const data of datas) {-->
<!--    if (data.id === id) {-->
<!--      return parentId;-->
<!--    }-->
<!--    if (data.children != null) {-->
<!--      const pid = findParent(id, data.children, data.id);-->
<!--      if (pid != null) {-->
<!--        return pid;-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--};-->

<!--/**-->
<!-- * 这个例子的时间复杂度确实可以优化 我实在是懒得改了-->
<!-- */-->

<!--const expandedKeysRef = ref<string[]>([]);-->
<!--const checkedKeysRef = ref<string[]>([]);-->
<!--const patternRef = ref('');-->
<!--const dataRef = ref([]);-->

<!--const message = useMessage();-->
<!--const showDropdownRef = ref<boolean>(false);-->
<!--const optionsRef = ref<any>([]);-->
<!--const xRef = ref(0);-->
<!--const yRef = ref(0);-->

<!--const CmdDropdown = [-->
<!--  {-->
<!--    label: '删除',-->
<!--    key: 'deleteCmd'-->
<!--  },-->
<!--  {-->
<!--    label: '编辑',-->
<!--    key: 'editCmd'-->
<!--  },-->
<!--  {-->
<!--    label: '新增子命令',-->
<!--    key: 'addCmd'-->
<!--  }-->
<!--];-->

<!--const initGroupTree = () => {-->
<!--  console.log('刷新tree');-->
<!--  tree().then(resultData => {-->
<!--    if (resultData.data != null) {-->
<!--      dataRef.value = resultData.data;-->
<!--    } else {-->
<!--      dataRef.value = [];-->
<!--    }-->
<!--  });-->
<!--};-->
<!--const init = () => {-->
<!--  initGroupTree();-->
<!--};-->

<!--const handleSelect = (key: string | number) => {-->
<!--  showDropdownRef.value = false;-->

<!--  switch (key) {-->
<!--    case 'editCmd':-->
<!--      showUpdCmdPageModal.value = true;-->
<!--      break;-->
<!--    case 'deleteCmd': {-->
<!--      const deleteCmdDialog = dialog.warning({-->
<!--        title: '删除',-->
<!--        content: '请确认删除吗?',-->
<!--        positiveText: '确认',-->
<!--        onPositiveClick: async () => {-->
<!--          if (deleteCmdDialog) {-->
<!--            deleteCmdDialog.loading = true;-->
<!--            await delCmd(getRealId(currentCmdIdRef.value));-->
<!--            initGroupTree();-->
<!--            deleteCmdDialog.loading = false;-->
<!--          }-->
<!--        }-->
<!--      });-->
<!--      break;-->
<!--    }-->
<!--    case 'addCmd':-->
<!--      showAddCmdPageModal.value = true;-->
<!--      break;-->
<!--    default:-->
<!--  }-->
<!--};-->
<!--const handleClickoutside = e => {-->
<!--  showDropdownRef.value = false;-->
<!--  app.enableDrawerMouseleave();-->
<!--};-->
<!--const nodeProps = ({ option }: { option: TreeOption }) => {-->
<!--  return {-->
<!--    onContextmenu(e: MouseEvent): void {-->
<!--      app.disabledDrawerMouseleave();-->
<!--      e.preventDefault();-->
<!--      e.stopPropagation();-->
<!--      optionsRef.value = CmdDropdown;-->

<!--      currentCmdIdRef.value = option.id;-->
<!--      showDropdownRef.value = true;-->
<!--      xRef.value = e.clientX;-->
<!--      yRef.value = e.clientY;-->
<!--    }-->
<!--  };-->
<!--};-->

<!--init();-->
<!--</script>-->
