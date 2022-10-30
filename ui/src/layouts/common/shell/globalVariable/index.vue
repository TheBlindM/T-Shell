<template>
  <div style="padding: 5px 10px; height: 100%">
    <n-space style="width: 260px; margin-bottom: 10px">
      <n-input v-model:value="pageParams.name" placeholder="搜索" @input="handleInput" />
      <n-button strong secondary @click="showAddPageModal = true">
        <Icon class="text-primary" icon="material-symbols:add" />
      </n-button>
    </n-space>
    <n-data-table
      :bordered="false"
      min-height="400"
      max-height="400"
      :columns="columns"
      :data="pageData"
      :pagination="pagination"
      :remote="true"
    />
  </div>

  <n-modal
    v-model:show="showAddPageModal"
    :show-icon="false"
    preset="dialog"
    title="新建"
    :on-after-leave="onAfterLeave"
    @contextmenu="disabledContextMenu"
  >
    <AddGlobaVariable @change-show-modal="changeAddPageShowModal" @refreshTree="reloadTable" />
  </n-modal>

  <n-modal
    v-model:show="showUpdPageModal"
    :show-icon="false"
    preset="dialog"
    title="更新"
    :on-after-leave="onAfterLeave"
    @contextmenu="disabledContextMenu"
  >
    <UpdGlobaVariable :id="currentId" @change-show-modal="changeUpdPageShowModal" @refreshTree="reloadTable" />
  </n-modal>
</template>

<script setup lang="tsx">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import type { DataTableColumns } from 'naive-ui';
import { useDialog } from 'naive-ui';
import { Icon } from '@iconify/vue';
import { useAppStore } from '@/store';
import { useRouterPush } from '@/composables';
import { page as globalVariablePage, del as delGlobalVariable } from '@/theblind_shell/service/shell/globalVariable';
import AddGlobaVariable from '@/layouts/common/shell/globalVariable/components/addGlobaVariable.vue';
import UpdGlobaVariable from '@/layouts/common/shell/globalVariable/components/updGlobaVariable.vue';
import { disabledContextMenu } from '@/utils/common/contextmenu';

const dialog = useDialog();
const app = useAppStore();
const router = useRouter();
const { routerPush } = useRouterPush();
const currentId = ref();

const showAddPageModal = ref<boolean>(false);
const showUpdPageModal = ref<boolean>(false);

const changeAddPageShowModal = (value: boolean) => {
  showAddPageModal.value = value;
};

const onAfterLeave = () => {
  app.enableDrawerMouseleave();
};

const deleteHandle = (id: number) => {
  const deleteDialog = dialog.warning({
    title: '删除',
    content: '请确认删除吗?',
    positiveText: '确认',
    onPositiveClick: async () => {
      if (deleteDialog) {
        deleteDialog.loading = true;
        await delGlobalVariable(id);
        reloadTable();
        deleteDialog.loading = false;
      }
    }
  });
};

const changeUpdPageShowModal = (value: boolean) => {
  showUpdPageModal.value = value;
};

const openUpdPage = id => {
  currentId.value = id;
  changeUpdPageShowModal(true);
};
const columns: DataTableColumns = [
  {
    title: '名称',
    key: 'varName'
  },
  {
    title: '值',
    key: 'value',
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '操作',
    key: 'action',
    render(record) {
      return (
        <n-space>
          <n-button size="tiny" secondary strong>
            <Icon class="text-primary" icon="icon-park:edit" onClick={openUpdPage.bind(this, record.id)} />
          </n-button>
          <n-button size="tiny" secondary strong onClick={deleteHandle.bind(this, record.id)}>
            <Icon class="text-primary" icon="icon-park:delete-key" />
          </n-button>
        </n-space>
      );
    }
  }
];
const reloadTable = () => {
  globalVariablePage(pageParams.value).then(resultData => {
    if (resultData.data != null) {
      pageData.value = resultData.data.data;
      pagination.value.page = resultData.data.page;
      pagination.value.pageCount = resultData.data.pageCount;
      pagination.value.pageSize = resultData.data.pageSize;
    }
  });
};

const pageData = ref<any>([]);

const pageParams = ref<any>({
  pageSize: 10,
  page: 1,
  name: null
});
const pagination = ref({
  pageSize: 10,
  page: 1,
  pageCount: 1,
  onUpdatePage: (page: number) => {
    pageParams.value.page = page;
    reloadTable();
    console.log(page);
  },
  onUpdatePageSize: (pageSize: number) => {
    pageParams.value.pageSize = pageSize;
  }
});

const handleInput = () => {
  reloadTable();
};

reloadTable();
</script>
