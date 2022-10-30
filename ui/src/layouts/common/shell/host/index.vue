<template>
  <div style="padding: 5px 10px; height: 100%">
    <n-space style="width: 260px">
      <n-input v-model:value="patternRef" placeholder="搜索" />
      <n-button
        strong
        secondary
        @click="
          showAddGroupPageModal = true;
          currentSessionGroupIdRef = 0;
        "
      >
        <Icon class="text-primary" icon="material-symbols:add" />
      </n-button>
      <!--		</div>-->
    </n-space>
    <n-tree
      :default-expand-all="true"
      block-line
      selectable
      draggable
      key-field="id"
      label-field="name"
      children-field="children"
      :pattern="patternRef"
      :data="dataRef"
      :node-props="nodeProps"
      style="height: 100%; width: 100%"
      :allow-drop="handleAllowDrop"
      @drop="handleDrop"
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
        :parent-id="currentSessionGroupIdRef"
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
        :host-group-id="currentSessionGroupIdRef"
        @change-show-modal="changeUpdGroupShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>

    <n-modal
      v-model:show="showAddHostPageModal"
      :show-icon="false"
      preset="dialog"
      title="新建主机"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <AddSession
        :session-group-id="currentSessionGroupIdRef"
        @change-show-modal="changeAddHostShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>
    <n-modal
      v-model:show="showUpdHostPageModal"
      :show-icon="false"
      preset="dialog"
      title="更新主机"
      :on-after-leave="onAfterLeave"
      @contextmenu="disabledContextMenu"
    >
      <UpdSession
        :host-id="currentHostIdRef"
        @change-show-modal="changeUpdHostShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { TreeOption, TreeDropInfo } from 'naive-ui';
import { useMessage, useDialog } from 'naive-ui';
import { nanoid } from 'nanoid';
import { Icon } from '@iconify/vue';
import type { DropPosition } from 'naive-ui/lib/tree/src/interface';
import { routeName } from '@/router';
import { useAppStore } from '@/store';
import { useRouterPush } from '@/composables';
import { del as delHost, updGroup as hostUpdGroup } from '@/theblind_shell/service/shell/host';
import { tree, del as delGroup, updGroup as hostGroupUpdGroup } from '@/theblind_shell/service/shell/hostGroup';
import { disabledContextMenu } from '@/utils/common/contextmenu';
import { AddGroup, AddSession, UpdGroup, UpdSession } from './components/index';

const dialog = useDialog();
const app = useAppStore();
const router = useRouter();
const { routerPush } = useRouterPush();

const showAddGroupPageModal = ref(false);
const showUpdGroupPageModal = ref(false);
const showAddHostPageModal = ref(false);
const showUpdHostPageModal = ref(false);

const currentSessionGroupIdRef = ref(0);
const currentHostIdRef = ref(0);

const changeAddHostShowModal = (value: boolean) => {
  showAddHostPageModal.value = value;
};

const changeUpdHostShowModal = (value: boolean) => {
  showUpdHostPageModal.value = value;
};

const changeAddGroupShowModal = (value: boolean) => {
  showAddGroupPageModal.value = value;
};

const changeUpdGroupShowModal = (value: boolean) => {
  showUpdGroupPageModal.value = value;
};
const onAfterLeave = () => {
  app.enableDrawerMouseleave();
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
    label: '新增主机',
    key: 'addHost'
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
            await delHost(currentHostIdRef.value);
            initGroupTree();
            deleteHostDialog.loading = false;
          }
        }
      });
      break;
    case 'editHost':
      showUpdHostPageModal.value = true;
      break;
    case 'addHost':
      showAddHostPageModal.value = true;
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
            await delGroup(currentSessionGroupIdRef.value);
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
    onDblclick() {
      routerPush({
        name: routeName('shell_terminal'),
        query: { sessionId: option.id, channelId: nanoid(), title: option.name },
        hash: '#DEMO_HASH'
      });
    },
    onContextmenu(e: MouseEvent): void {
      app.disabledDrawerMouseleave();
      e.preventDefault();
      e.stopPropagation();
      console.log(option);

      if (option.type === 'group') {
        optionsRef.value = groupDropdown;
        currentSessionGroupIdRef.value = option.id;
      } else {
        optionsRef.value = hostDropdown;
        currentSessionGroupIdRef.value = option.parentId as number;
        currentHostIdRef.value = option.id;
        console.log(currentSessionGroupIdRef.value);
      }
      showDropdownRef.value = true;
      xRef.value = e.clientX;
      yRef.value = e.clientY;
    }
  };
};
const handleDrop = ({ node, dragNode, dropPosition }: TreeDropInfo) => {
  // 拖动没有暂未使用
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
  /* dragNodeSiblings = dragNodeSiblings.sort((prev, next) => {
		return prev.localeCompare(next);
	}); */

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
      if (node.type == 'group') {
        hostGroupUpdGroup(0, dropId);
      } else {
      }
    }
  } else if (node.type == 'group') {
    hostGroupUpdGroup(node.id, dropId);
  } else {
    hostUpdGroup(node.id, dropId);
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
