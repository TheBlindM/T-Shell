<template>
  <div style="padding: 5px 10px; height: 100%">
    <!--		<div style="margin: 0 auto">-->
    <n-space style="width: 260px">
      <n-input v-model:value="patternRef" placeholder="搜索" />
      <n-button
        strong
        secondary
        @click="
          showAddGroupPageModal = true;
          currentGroupIdRef = 0;
        "
      >
        <Icon class="text-primary" icon="material-symbols:add" />
      </n-button>
      <!--		</div>-->
    </n-space>
    <n-tree
      :default-expand-all="true"
      block-line
      key-field="id"
      label-field="name"
      children-field="children"
      :pattern="patternRef"
      :data="dataRef"
      :node-props="nodeProps"
      style="height: 100%; width: 100%"
    />
    <!--	@drop="handleDrop"
		:node-props="nodeProps"-->
    <!-- 又键菜单 -->
    <n-dropdown
      trigger="manual"
      placement="bottom-start"
      :show="showDropdownRef"
      :options="(optionsRef as any)"
      :x="xRef"
      :y="yRef"
      @select="handleSelect"
      @clickoutside="handleClickoutside"
    />

    <n-modal
      v-model:show="showAddGroupPageModal"
      :show-icon="false"
      preset="dialog"
      title="新建组"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <AddGroup
        :parent-id="currentGroupIdRef"
        @change-show-modal="changeAddGroupShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>
    <n-modal
      v-model:show="showUpdGroupPageModal"
      :show-icon="false"
      preset="dialog"
      title="更新组"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <UpdGroup
        :host-group-id="currentGroupIdRef"
        @change-show-modal="changeUpdGroupShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>

    <n-modal
      v-model:show="showAddCmdPageModal"
      :show-icon="false"
      preset="dialog"
      title="新建快捷命令"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <AddCmd :group-id="currentGroupIdRef" @change-show-modal="changeAddCmdShowModal" @refreshTree="initGroupTree" />
    </n-modal>
    <n-modal
      v-model:show="showUpdCmdPageModal"
      :show-icon="false"
      preset="dialog"
      title="更新快捷命令"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <UpdCmd :group-id="currentCmdIdImplRef" @change-show-modal="changeUpdCmdShowModal" @refreshTree="initGroupTree" />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import type { TreeOption, TreeDropInfo } from 'naive-ui';
import { useMessage, useDialog } from 'naive-ui';
import { nanoid } from 'nanoid';
import { Icon } from '@iconify/vue';
import type { DropPosition } from 'naive-ui/lib/tree/src/interface';
import { routeName } from '@/router';
import { useAppStore, useTabStore } from '@/store';
import { useRouterPush } from '@/composables';
import { del as delCmd, partUpd as partUpdCmd } from '@/theblind_shell/service/shell/shortcutCmd';
import { tree, del as delGroup, partUpd as partUpdGroup } from '@/theblind_shell/service/shell/shortcutCmdGroup';
import { AddGroup, AddCmd, UpdGroup, UpdCmd } from './components/index';
import RequestResult = Service.RequestResult;
import { disabledContextMenu } from '@/utils/common/contextmenu';

const dialog = useDialog();
const app = useAppStore();
const router = useRouter();
const { routerPush } = useRouterPush();

const showAddGroupPageModal = ref(false);
const showUpdGroupPageModal = ref(false);
const showAddCmdPageModal = ref(false);
const showUpdCmdPageModal = ref(false);

const currentGroupIdRef = ref(0);
const currentCmdIdImplRef = ref(0);

const onAfterLeave = () => {
  app.enableDrawerMouseleave();
};

const changeAddCmdShowModal = (value: boolean) => {
  showAddCmdPageModal.value = value;
};

const changeUpdCmdShowModal = (value: boolean) => {
  showUpdCmdPageModal.value = value;
};

const changeAddGroupShowModal = (value: boolean) => {
  showAddGroupPageModal.value = value;
};

const changeUpdGroupShowModal = (value: boolean) => {
  showUpdGroupPageModal.value = value;
};

function findSiblingsAndIndex(node: TreeOption, nodes?: TreeOption[]): [TreeOption[], number] | [null, null] {
  if (!nodes) return [null, null];
  // eslint-disable-next-line no-plusplus
  for (let i = 0; i < nodes.length; ++i) {
    const siblingNode = nodes[i];
    if (siblingNode.id === node.id) return [nodes, i];
    const [siblings, index] = findSiblingsAndIndex(node, siblingNode.children);
    if (siblings && index !== null) return [siblings, index];
  }
  return [null, null];
}

// eslint-disable-next-line consistent-return
const findParent = (id: number, datas: any, parentId: number) => {
  debugger;
  // eslint-disable-next-line no-restricted-syntax
  for (const data of datas) {
    if (data.id === id) {
      return parentId;
    }
    if (data.children != null) {
      const pid = findParent(id, data.children, data.id);
      if (pid != null) {
        return pid;
      }
    }
  }
};

/**
 * 这个例子的时间复杂度确实可以优化 我实在是懒得改了
 */

const expandedKeysRef = ref<string[]>([]);
const checkedKeysRef = ref<string[]>([]);
const patternRef = ref('');
const dataRef = ref([]);

const message = useMessage();
const showDropdownRef = ref(false);
const optionsRef = ref([]);
const xRef = ref(0);
const yRef = ref(0);
const groupDropdown = [
  {
    label: '新增',
    key: 'addGroup'
  },
  {
    label: '删除',
    key: 'deleteGroup'
  },
  {
    label: '编辑',
    key: 'editGroup'
  },
  {
    label: '新增快捷命令',
    key: 'addCmd'
  }
];

const hostDropdown = [
  {
    label: '删除',
    key: 'deleteHost'
  },
  {
    label: '编辑',
    key: 'editHost'
  }
];

const initGroupTree = () => {
  console.log('刷新tree');
  tree().then(resultData => {
    if (resultData.data != null) {
      dataRef.value = resultData.data;
    }
  });
};
const init = () => {
  initGroupTree();
};

const handleSelect = (key: string | number) => {
  showDropdownRef.value = false;

  switch (key) {
    case 'deleteHost':
      const deleteHostDialog = dialog.warning({
        title: '删除',
        content: '请确认删除吗?',
        positiveText: '确认',
        onPositiveClick: async () => {
          if (deleteHostDialog) {
            deleteHostDialog.loading = true;
            await delCmd(currentCmdIdImplRef.value);
            initGroupTree();
            deleteHostDialog.loading = false;
          }
        }
      });
      break;
    case 'editHost':
      showUpdCmdPageModal.value = true;
      break;
    case 'addCmd':
      showAddCmdPageModal.value = true;
      break;
    case 'editGroup':
      showUpdGroupPageModal.value = true;
      break;
    case 'deleteGroup':
      const deleteGroupDialog = dialog.warning({
        title: '删除',
        content: '请确认删除吗?',
        positiveText: '确认',
        onPositiveClick: async () => {
          if (deleteGroupDialog) {
            deleteGroupDialog.loading = true;
            await delGroup(currentGroupIdRef.value);
            initGroupTree();
            deleteGroupDialog.loading = false;
          }
        }
      });
      break;
    case 'addGroup':
      showAddGroupPageModal.value = true;
      break;
    default:
  }
};
const handleClickoutside = e => {
  showDropdownRef.value = false;
  app.enableDrawerMouseleave();
};
const nodeProps = ({ option }: { option: TreeOption }) => {
  return {
    onContextmenu(e: MouseEvent): void {
      app.disabledDrawerMouseleave();
      e.preventDefault();
      e.stopPropagation();
      if (option.id > 0) {
        optionsRef.value = groupDropdown;
        currentGroupIdRef.value = option.id;
      } else {
        optionsRef.value = hostDropdown;
        currentGroupIdRef.value = option.parentId as number;
        currentCmdIdImplRef.value = option.id * -1;
        console.log(option.id * -1);
        console.log(currentGroupIdRef.value);
      }
      showDropdownRef.value = true;
      xRef.value = e.clientX;
      yRef.value = e.clientY;
    }
  };
};
const handleDrop = ({ node, dragNode, dropPosition }: TreeDropInfo) => {
  const [dragNodeSiblings, dragNodeIndex] = findSiblingsAndIndex(dragNode, dataRef.value);
  if (dragNodeSiblings === null || dragNodeIndex === null) return;
  dragNodeSiblings.splice(dragNodeIndex, 1);
  console.log(`拖动${JSON.stringify(dragNode)}`);
  console.log(`node${JSON.stringify(node)}`);
  if (dropPosition === 'inside') {
    if (node.children) {
      node.children.unshift(dragNode);
    } else {
      // eslint-disable-next-line no-param-reassign
      node.children = [dragNode];
    }
  } else if (dropPosition === 'before') {
    const [nodeSiblings, nodeIndex] = findSiblingsAndIndex(node, dataRef.value);
    if (nodeSiblings === null || nodeIndex === null) return;
    nodeSiblings.splice(nodeIndex, 0, dragNode);
  } else if (dropPosition === 'after') {
    const [nodeSiblings, nodeIndex] = findSiblingsAndIndex(node, dataRef.value);
    if (nodeSiblings === null || nodeIndex === null) return;
    nodeSiblings.splice(nodeIndex + 1, 0, dragNode);
  }
  dataRef.value = Array.from(dataRef.value);

  console.log(`拖动${JSON.stringify(dragNode)}`);
  console.log(`node${JSON.stringify(node)}`);
  console.log('插入');
  console.log('parentid:', node.id);
  const dropId: number = parseInt(dragNode.id);
  if (dropPosition !== 'inside') {
    console.log('非插入');
    // console.log('parentid:', findParent(node.id, dataRef.value, 0));

    if (node.parentId !== dragNode.parentId) {
      if (dropId > 0) {
        partUpdGroup({ parentId: 0 }, dropId);
      } else {
      }
    }
  } else {
    // 大于0为组
    if (dropId > 0) {
      partUpdGroup({ parentId: node.id }, dropId);
    } else {
      partUpdCmd({ hostGroupId: node.id }, dropId * -1);
    }
  }
};

const handleAllowDrop = (info: { dropPosition: DropPosition; node: TreeOption; phase: 'drag' | 'drop' }) => {
  if (info.dropPosition === 'inside') {
    return true;
  }
  return false;
};

init();
</script>
