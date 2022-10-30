<!--
<template>
  <div style="padding: 5px 10px; height: 100%">
    &lt;!&ndash;		<div style="margin: 0 auto">&ndash;&gt;
    <n-space style="width: 260px">
      <n-input v-model:value="patternRef" placeholder="搜索" />
      <n-button
        strong
        secondary
        @click="
          showAddOsTypePageModal = true;
          currentOsTypeIdRef = 0;
        "
      >
        <Icon class="text-primary" icon="material-symbols:add" />
      </n-button>
      &lt;!&ndash;		</div>&ndash;&gt;
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
      @drop="handleDrop"
    />
    &lt;!&ndash;	@drop="handleDrop"
		:node-props="nodeProps"&ndash;&gt;
    &lt;!&ndash; 又键菜单 &ndash;&gt;
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

    <n-modal v-model:show="showAddOsTypePageModal" :show-icon="false" preset="dialog" title="新建系统">
      <AddOsType
        :parent-id="currentOsTypeIdRef"
        @change-show-modal="changeAddGroupShowModal"
        @refreshTree="initGroupTree"
      />
    </n-modal>
    <n-modal v-model:show="showUpdOsTypePageModal" :show-icon="false" preset="dialog" title="更新系统">
      <UpdOsType
        :os-type-id="getRealId(currentOsTypeIdRef)"
        @change-show-modal="changeUpdGroupShowModal"
        @refreshTree="initGroupTree"
      />
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
import { routeName } from '@/router';
import { getHostGroupId } from '@/service';
import { useAppStore, useTabStore } from '@/store';
import { useRouterPush } from '@/composables';
import { tree, del as delOsType, partUpd as partUpdOsType } from '@/theblind_shell/service/shell/ttyType';
import { AddOsType, UpdOsType } from './components/index';

const dialog = useDialog();
const app = useAppStore();
const router = useRouter();
const { routerPush } = useRouterPush();

const showAddOsTypePageModal = ref(false);
const showUpdOsTypePageModal = ref(false);

const currentOsTypeIdRef = ref(0);

const changeAddGroupShowModal = (value: boolean) => {
  showAddOsTypePageModal.value = value;
};

const changeUpdGroupShowModal = (value: boolean) => {
  showUpdOsTypePageModal.value = value;
};

/* 获取真实id */
const getRealId = (id: number) => {
  return id < 0 ? id * -1 : id;
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
const parentOsTypeDropdown = [
  {
    label: '删除',
    key: 'deleteOsType'
  },
  {
    label: '编辑',
    key: 'editOsType'
  },
  {
    label: '新增操作系统',
    key: 'addOsType'
  }
];

const osTypeDropdown = [
  {
    label: '删除',
    key: 'deleteOsType'
  },
  {
    label: '编辑',
    key: 'editOsType'
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
    case 'editOsType':
      showUpdOsTypePageModal.value = true;
      break;
    case 'deleteOsType':
      const deleteOsTypeDialog = dialog.warning({
        title: '删除',
        content: '请确认删除吗?',
        positiveText: '确认',
        onPositiveClick: async () => {
          if (deleteOsTypeDialog) {
            deleteOsTypeDialog.loading = true;
            await delOsType(getRealId(currentOsTypeIdRef.value));
            initGroupTree();
            deleteOsTypeDialog.loading = false;
          }
        }
      });
      break;
    case 'addOsType':
      showAddOsTypePageModal.value = true;
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
        optionsRef.value = parentOsTypeDropdown;
      } else {
        optionsRef.value = osTypeDropdown;
      }
      currentOsTypeIdRef.value = option.id;
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
      partUpdOsType({ parentId: 0 }, getRealId(dropId));
    }
  } else {
    partUpdOsType({ parentId: node.id }, getRealId(dropId));
  }
};

init();
</script>
-->
