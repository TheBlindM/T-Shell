<!--
<template>
  <n-spin :show="showSpin">
    <div style="width: 100%; height: 100%">
      <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
        <n-form-item label="系统名称" path="osTypeName">
          <n-input v-model:value="formValue.osTypeName" placeholder="输入系统名称" />
        </n-form-item>

        <n-form-item label="是否为父级" path="osTypeName">
          <n-switch v-model:value="formValue.parent" placeholder="是否为父级" />
        </n-form-item>
      </n-form>
    </div>
    <n-space>
      &lt;!&ndash;		<n-button @click="() => (showAddPageModal = false)">取消</n-button>&ndash;&gt;
      <n-button type="info" @click="confirmForm">确定</n-button>
    </n-space>
  </n-spin>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { FormInst } from 'naive-ui';
import { useMessage } from 'naive-ui';
import { useAppStore } from '@/store';
import { add as addOsType, tree as osTypeTree, getSingle } from '@/theblind_shell/service/shell/ttyType';

const app = useAppStore();
app.disabledDrawerMouseleave();
interface Props {
  osTypeId: number;
}

const rules = {
  parentId: {
    required: true,
    message: '选择父级',
    trigger: 'blur'
  },
  osTypeName: {
    required: true,
    message: '输入主机名称',
    trigger: 'blur'
  }
};
const formRef = ref<FormInst | null>(null);
const message = useMessage();
const props = defineProps<Props>();
const emits = defineEmits(['changeShowModal', 'refreshTree']);

const showSpin = ref(true);
const init = () => {
  getSingle(props.osTypeId).then(data => {
    formValue.value = data.data;
    showSpin.value = false;
  });
};

onMounted(() => {
  init();
});

const formValue = ref({
  osTypeName: '',
  parentId: props.parentId,
  parent: false
});

let formBtnLoading = false;

const confirmForm = (e: MouseEvent) => {
  e.preventDefault();
  if (formBtnLoading) {
    return;
  }
  formBtnLoading = true;
  formRef.value?.validate(errors => {
    if (!errors) {
      addOsType(formValue.value).then(resultData => {
        if (resultData.data != null) {
          console.log('刷新');
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
</script>
-->
