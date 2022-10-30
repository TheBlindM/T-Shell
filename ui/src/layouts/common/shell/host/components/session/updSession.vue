<template>
  <n-spin :show="showSpin">
    <div style="width: 100%; height: 100%">
      <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
        <n-form-item label="名称" path="sessionName">
          <n-input v-model:value="formValue.sessionName" placeholder="输入主机名称" />
        </n-form-item>
        <n-form-item label="ip" path="ip">
          <n-input v-model:value="formValue.ip" placeholder="输入主机ip" />
        </n-form-item>
        <n-form-item label="端口" path="port">
          <n-input-number v-model:value="formValue.port" placeholder="输入指定端口" />
        </n-form-item>
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formValue.username" placeholder="输入用户名" />
        </n-form-item>
        <n-form-item label="密码" path="pwd">
          <n-input v-model:value="formValue.pwd" type="password" show-password-on="mousedown" placeholder="密码" />
        </n-form-item>
        <n-form-item label="操作系统" path="ttyTypeId">
          <n-tree-select
            v-model:value="formValue.ttyTypeId"
            :options="options"
            label-field="name"
            key-field="id"
            children-field="children"
          />
        </n-form-item>
      </n-form>
    </div>
    <n-space>
      <!--		<n-button @click="() => (showAddPageModal = false)">取消</n-button>-->
      <n-button type="info" :loading="formBtnLoading" @click="confirmForm">确定</n-button>
    </n-space>
  </n-spin>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useMessage } from 'naive-ui';
import { useAppStore } from '@/store';
import { upd, getSingle } from '@/theblind_shell/service/shell/host';
import { selectTree } from '@/theblind_shell/service/shell/ttyType';
import RequestResult = Service.RequestResult;

interface Props {
  hostId: number;
}

const app = useAppStore();
app.disabledDrawerMouseleave();
const formRef = ref<any>(null);
const message = useMessage();
const props = defineProps<Props>();
const emits = defineEmits(['changeShowModal', 'refreshTree']);
const formValue = ref<any>({
  sessionGroupId: '',
  sessionName: '',
  username: '',
  ip: '',
  port: 22,
  pwd: '',
  ttyTypeId: ''
});

const rules = {
  sessionName: {
    required: true,
    message: '输入主机名称',
    trigger: 'blur'
  },
  ip: {
    required: true,
    message: '输入主机ip',
    trigger: ['input', 'blur']
  },
  port: {
    type: 'number',
    required: true,
    trigger: ['blur', 'input'],
    message: '请输入端口'
  },
  username: {
    required: true,
    message: '输入用户名',
    trigger: ['input']
  },
  pwd: {
    required: true,
    message: '输入密码',
    trigger: ['input']
  }
};
/* validator(row: any, value: string[]) {
      if (value.length <= 0) return new Error('请添加选项');
      if (new Set(value).size !== value.length) return new Error('存在重复项');
      return true;
    } */

const options = ref([]);

const showSpin = ref(true);
const init = () => {
  getSingle(props.hostId).then(data => {
    formValue.value = data.data;
    showSpin.value = false;
  });
};

onMounted(() => {
  init();
});
let formBtnLoading = false;
const confirmForm = (e: { preventDefault: () => void }) => {
  e.preventDefault();
  formBtnLoading = true;
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      upd(formRef.value.model, props.hostId).then((requestResult: RequestResult) => {
        if (requestResult.data != null) {
          app.enableDrawerMouseleave();
          emits('refreshTree');
          message.success('成功');
          emits('changeShowModal', false);
        }
      });
    } else {
      message.error('请填写完整信息');
    }
    formBtnLoading = false;
  });
};

const initTtyTypeTree = () => {
  selectTree().then((requestResult: RequestResult) => {
    if (requestResult.data != null) {
      options.value = requestResult.data;
    }
  });
};

initTtyTypeTree();
</script>
