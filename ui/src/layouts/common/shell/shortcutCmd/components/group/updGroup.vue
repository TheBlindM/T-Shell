<template>
  <div style="width: 100%; height: 100%">
    <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
      <n-form-item label="组名" path="groupName">
        <n-input v-model:value="formValue.groupName" placeholder="输入主机名称" />
      </n-form-item>
    </n-form>
  </div>
  <n-space>
    <!--		<n-button @click="() => (showAddPageModal = false)">取消</n-button>-->
    <n-button type="info" @click="confirmForm">确定</n-button>
  </n-space>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { FormInst, useMessage } from 'naive-ui';
import { useAppStore } from '@/store';
import { upd, getSingle ,parentTree} from '@/theblind_shell/service/shell/shortcutCmdGroup';
import RequestResult = Service.RequestResult;

const app = useAppStore();
app.disabledDrawerMouseleave();
interface Props {
  /** hostGroupId */
  hostGroupId: number;
}

const rules = {
  groupName: {
    required: true,
    message: '输入主机名称',
    trigger: 'blur'
  }
};
const formRef = ref<FormInst | null>(null);
const message = useMessage();
const props = defineProps<Props>();
const emits = defineEmits(['changeShowModal','refreshTree']);

const formValue = ref({
  groupName: '',
  parentId: null,
  id: null
});

const init = () => {
  getSingle(props.hostGroupId).then(data => {
    formValue.value = data.data;
  });


};

let formBtnLoading = false;

const confirmForm = (e: MouseEvent) => {
  e.preventDefault();
  if (formBtnLoading) {
    return;
  }
  formBtnLoading = true;
  formRef.value?.validate(errors => {
    if (!errors) {
      upd(formValue.value, props.hostGroupId).then((requestResult: RequestResult) => {
        if (requestResult.data != null) {
          emits('refreshTree');
          message.success('成功');
          app.enableDrawerMouseleave();
          emits('changeShowModal', false);
        }
      });
    } else {
      message.error('请填写完整信息');
    }
    formBtnLoading = false;
  });
};

init();
</script>
