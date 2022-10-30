<template>
  <div style="width: 100%; height: 100%">
    <n-spin :show="showSpin">
      <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
        <n-form-item label="变量名称" path="varName">
          <n-input v-model:value="formValue.varName" placeholder="变量名称" />
        </n-form-item>

        <n-form-item label="变量值" path="value">
          <n-input v-model:value="formValue.value" placeholder="值" />
        </n-form-item>
      </n-form>
    </n-spin>
  </div>
  <n-space>
    <n-button type="info" @click="confirmForm">确定</n-button>
  </n-space>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useMessage } from 'naive-ui';
import { useAppStore } from '@/store';
import { add, getSingle, upd } from '@/theblind_shell/service/shell/globalVariable';

interface Props {
  id: number;
}

const props = defineProps<Props>();

const app = useAppStore();
app.disabledDrawerMouseleave();

const rules = {
  varName: {
    required: true,
    message: '输入变量名称',
    trigger: 'blur'
  },
  value: {
    required: true,
    message: '输入值',
    trigger: 'blur'
  }
};
const showSpin = ref(true);
const formRef = ref<any>(null);
const message = useMessage();
const emits = defineEmits(['changeShowModal', 'refreshTree']);

const formValue = ref<any>({
  varName: '',
  value: ''
});

let formBtnLoading = false;

const confirmForm = (e: MouseEvent) => {
  e.preventDefault();
  if (formBtnLoading) {
    return;
  }
  formBtnLoading = true;
  formRef.value?.validate((errors: any) => {
    if (!errors) {
      upd(formValue.value, props.id).then(resultData => {
        if (resultData.data != null) {
          message.success('成功');
          emits('changeShowModal', false);
          emits('refreshTree');
          app.enableDrawerMouseleave();
        }
      });
    } else {
      message.error('请填写完整信息');
    }
    formBtnLoading = false;
  });
};

const init = () => {
  getSingle(props.id).then(requestResult => {
    if (requestResult.data != null) {
      formValue.value = requestResult.data;
      showSpin.value = false;
    }
  });
};
init();
</script>
