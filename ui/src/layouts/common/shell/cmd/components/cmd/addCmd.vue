<!--
<template>
  <div style="width: 100%; height: 100%">
    <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
      <n-form-item label="命令" path="cmd">
        <n-input v-model:value="formValue.cmdText" placeholder="命令" />
      </n-form-item>

      <n-form-item label="描述" path="describe">
        <n-input v-model:value="formValue.describe" placeholder="描述" />
      </n-form-item>

      <n-form-item v-if="props.parentId === 0" label="操作系统 (不填默认兼容所有系统)" path="osTypeName">
        <n-tree-select
          v-model:value="formValue.cmdOsTypeIdList"
          multiple
          :options="osTypeOptionsRef"
          label-field="name"
          key-field="id"
          children-field="children"
        />
        &lt;!&ndash;
  <n-space><div v-for="tag in cmdOsTypeTagListRef" :key="tag" style="display: inline-block">
            <n-tag
              style="user-select: none"
              :type="tag.compatible ? 'info' : 'error'"
              closable
              @close="osTypeTagClose(tag.osTypeId)"
              @dblclick="osTypeTagEdit(tag.osTypeId)"
            >
              {{ tag.osTypeName }}
            </n-tag>
          </div>

          <n-button
            size="small"
            @click="
              showOsTypeModal = true;
              isOsTypeEdit = false;
              osTypeFormValue = getDefaultOsTypeValue();
            "
            >新增
          </n-button>
           </n-space>&ndash;&gt;
      </n-form-item>

      <n-form-item label="选项" path="options">
        <n-space>
          <div v-for="(tag, index) in formValue.cmdOptionList" :key="index" style="display: inline-block">
            <n-tag
              style="user-select: none"
              type="info"
              closable
              @close="optionTagClose(index)"
              @dblclick="optionTagEdit(index)"
            >
              {{ tag.optionNameList }}
            </n-tag>
          </div>

          <n-button
            size="small"
            @click="
              showOptionModal = true;
              isOptionEdit = false;
              optionFormValue = getDefaultOptionValue();
            "
            >新增
          </n-button>
        </n-space>
      </n-form-item>

      <n-form-item label="参数" path="parameters">
        <n-space>
          <div v-for="(tag, index) in formValue.cmdParameterList" :key="index" style="display: inline-block">
            <n-tag
              style="user-select: none"
              type="info"
              closable
              @close="parameterTagClose(index)"
              @dblclick="parameterTagEdit(index)"
            >
              {{ tag.description }}
            </n-tag>
          </div>

          <n-button
            size="small"
            @click="
              showParameterModal = true;
              isParameterEdit = false;
              parameterFormValue = getDefaultParameterValue();
            "
            >新增
          </n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </div>
  <n-space>
    &lt;!&ndash;		<n-button @click="() => (showAddPageModal = false)">取消</n-button>&ndash;&gt;
    <n-button type="info" @click="confirmForm">确定</n-button>
  </n-space>

  &lt;!&ndash; osType	&ndash;&gt;
  <n-modal
    v-model:show="showOsTypeModal"
    v-model:title="osTypeDialogTitle"
    :show-icon="false"
    preset="dialog"
    style="width: 420px"
  >
    <n-form
      ref="osTypeFormRef"
      :model="osTypeFormValue"
      :rules="osTypeRules"
      label-placement="left"
      :label-width="80"
      class="py-4"
    >
      <n-form-item label="操作系统" path="desc">
        <n-tree-select
          v-model:value="osTypeFormValue.osTypeId"
          :options="osTypeOptionsRef"
          label-field="name"
          key-field="id"
          children-field="children"
        />
      </n-form-item>

      <n-form-item label="是否兼容" path="normal">
        <n-switch v-model:value="osTypeFormValue.compatible" />
      </n-form-item>
    </n-form>

    <template #action>
      <n-space>
        <n-button @click="() => (showOsTypeModal = false)">取消</n-button>
        <n-button type="info" :loading="osTypeFormBtnLoading" @click="confirmOsTypeForm">确定</n-button>
      </n-space>
    </template>
  </n-modal>

  &lt;!&ndash;	option &ndash;&gt;
  <n-modal
    v-model:show="showOptionModal"
    v-model:title="optionDialogTitle"
    :show-icon="false"
    preset="dialog"
    style="width: 420px"
  >
    <n-form
      ref="optionFormRef"
      :model="optionFormValue"
      :rules="optionRules"
      label-placement="left"
      :label-width="80"
      class="py-4"
    >
      <n-form-item label="命令选项" path="optionNameList">
        <n-dynamic-tags v-model:value="optionFormValue.optionNameList" />
      </n-form-item>

      <n-form-item label="描述" path="description">
        <n-input v-model:value="optionFormValue.description" />
      </n-form-item>
    </n-form>

    <template #action>
      <n-space>
        <n-button @click="() => (showOptionModal = false)">取消</n-button>
        <n-button type="info" :loading="optionFormBtnLoading" @click="confirmOptionForm">确定</n-button>
      </n-space>
    </template>
  </n-modal>

  &lt;!&ndash; parameter	&ndash;&gt;
  <n-modal
    v-model:show="showParameterModal"
    v-model:title="parameterDialogTitle"
    :show-icon="false"
    preset="dialog"
    style="width: 420px"
  >
    <n-form
      ref="parameterFormRef"
      :model="parameterFormValue"
      :rules="parameterRules"
      label-placement="left"
      :label-width="80"
      class="py-4"
    >
      <n-form-item label="下标" path="index">
        <n-input-number v-model:value="parameterFormValue.index" />
      </n-form-item>

      <n-form-item label="描述" path="description">
        <n-input v-model:value="parameterFormValue.description" />
      </n-form-item>
    </n-form>

    <template #action>
      <n-space>
        <n-button @click="() => (showParameterModal = false)">取消</n-button>
        <n-button type="info" :loading="parameterFormBtnLoading" @click="confirmParameterForm">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { FormInst } from 'naive-ui';
import { useMessage } from 'naive-ui';
import { useAppStore } from '@/store';
import { getListById, selectTree as osTypeSelectTree } from '@/theblind_shell/service/shell/ttyType';
import { add as addCmd } from '@/theblind_shell/service/shell/cmd';

const app = useAppStore();
app.disabledDrawerMouseleave();

interface Props {
  parentId: number;
}

const rules = {
  osTypeId: {
    required: true,
    message: '选择父级',
    trigger: 'blur'
  }
};

const osTypeRules = {
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
const parameterRules = {
  index: {
    required: true,
    message: '输入下标',
    type: 'number',
    trigger: ['blur', 'input']
  },
  description: {
    required: true,
    message: '输入描述',
    trigger: 'blur'
  }
};

const optionRules = {
  optionNameList: {
    trigger: ['change'],
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    validator(row: any, value: string[]) {
      if (value.length <= 0) return new Error('请添加选项');
      if (new Set(value).size !== value.length) return new Error('存在重复项');
      return true;
    }
  },
  description: {
    required: true,
    message: '输入描述',
    trigger: 'blur'
  }
};
const formRef = ref<FormInst | null>(null);

const message = useMessage();
const props = defineProps<Props>();
const emits = defineEmits(['changeShowModal', 'refreshTree']);

const showOsTypeModal = ref<boolean>(false);
const osTypeDialogTitle = ref<string>('新建');
const cmdOsTypeTagListRef = ref<CmdTag[]>([]);
const osTypeFormRef = ref<FormInst | null>(null);

const isOsTypeEdit = ref<boolean>(false);
const osTypeFormBtnLoading = ref<boolean>(false);
const osTypeOptionsRef = ref<any>([]);
const currentOsTypeIdRef = ref<number>();

/* option */
const optionFormRef = ref<FormInst | null>(null);
const showOptionModal = ref<boolean>(false);
const optionDialogTitle = ref<string>('新建');
const isOptionEdit = ref<boolean>(false);
const optionFormBtnLoading = ref<boolean>(false);
const optionFormValue = ref<any>();
let currentOptionIdx = 0;
/* parameter */

const parameterFormRef = ref<FormInst | null>(null);
const showParameterModal = ref<boolean>(false);
const parameterDialogTitle = ref<string>('新建');
const isParameterEdit = ref<boolean>(false);
const parameterFormBtnLoading = ref<boolean>(false);
const parameterFormValue = ref<any>();
let currentParameterIdx = 0;

const getDefaultCmdValue = () => {
  return {
    parentCmdId: props.parentId,
    cmdText: '',
    describe: '',
    cmdOsTypeIdList: [],
    cmdOptionList: [],
    cmdParameterList: []
  };
};

const getDefaultOsTypeValue = () => {
  return {
    osTypeId: null,
    compatible: true
  };
};

const formValue = ref<any>(getDefaultCmdValue());

const osTypeFormValue = ref<any>(getDefaultOsTypeValue());

let formBtnLoading = false;

const refreshCmdOsTypeTagList = () => {
  const { cmdOsTypeList } = formValue.value;

  if (cmdOsTypeList.length === 0) {
    cmdOsTypeTagListRef.value = [];
    return;
  }
  const ids: number[] = [];
  const map: Map<number, boolean> = new Map();
  cmdOsTypeList.forEach((val: { osTypeId: number; compatible: boolean }) => {
    ids.push(val.osTypeId);
    map.set(val.osTypeId, val.compatible);
  });
  getListById({ osTypeIds: ids }).then(resultData => {
    const { data } = resultData;

    const cmdOsTypeTagList: { osTypeId: number; compatible: any; osTypeName: string }[] = [];
    data?.forEach((val: { id: number; compatible: boolean; osTypeName: string }) => {
      cmdOsTypeTagList.push({
        osTypeId: val.id,
        compatible: map.get(val.id),
        osTypeName: val.osTypeName
      });
    });
    cmdOsTypeTagListRef.value = cmdOsTypeTagList;
  });
  /*  cmdOsTypeList.forEach((val: { osTypeId: number; compatible: boolean }) => {
		getOsTypeSingle(getRealId(val.osTypeId)).then(resultData => {
			const { data } = resultData;
			cmdOsTypeTagList.push({ osTypeId: val.osTypeId, compatible: data.compatible, osTypeName: data.osTypeName });
		});
	}); */
};

const osTypeTagClose = (osTypeId: number) => {
  const { cmdOsTypeList } = formValue.value;
  let index = null;
  cmdOsTypeList.forEach((cmdOsType: { osTypeId: any; compatible: boolean }, idx: number) => {
    if (cmdOsType.osTypeId === osTypeId) {
      index = idx;
    }
  });
  if (index !== null) {
    cmdOsTypeList.splice(index, 1);
    refreshCmdOsTypeTagList();
  }
};

const osTypeTagEdit = (osTypeId: number) => {
  showOsTypeModal.value = true;
  isOsTypeEdit.value = true;
  osTypeDialogTitle.value = '编辑';
  currentOsTypeIdRef.value = osTypeId;
  const { cmdOsTypeList } = formValue.value;

  cmdOsTypeList.forEach((cmdOsType: { osTypeId: any; compatible: boolean }) => {
    if (cmdOsType.osTypeId === osTypeId) {
      const defaultOsTypeValue = getDefaultOsTypeValue();
      defaultOsTypeValue.osTypeId = cmdOsType.osTypeId;
      defaultOsTypeValue.compatible = cmdOsType.compatible;
      osTypeFormValue.value = defaultOsTypeValue;
    }
  });
};

const initOsTypeTree = () => {
  osTypeSelectTree().then(requestResult => {
    debugger;
    if (requestResult.data != null) {
      osTypeOptionsRef.value = requestResult.data;
    }
  });
};

const confirmForm = (e: MouseEvent) => {
  e.preventDefault();
  if (formBtnLoading) {
    return;
  }
  formBtnLoading = true;
  formRef.value?.validate(errors => {
    if (!errors) {
      addCmd(formValue.value).then(resultData => {
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

function confirmOsTypeForm(e: MouseEvent) {
  e.preventDefault();
  osTypeFormBtnLoading.value = true;

  osTypeFormRef.value?.validate(errors => {
    if (!errors) {
      if (isOsTypeEdit.value) {
        const { cmdOsTypeList } = formValue.value;
        const { osTypeId, compatible } = osTypeFormValue.value;
        const currentOsTypeId = currentOsTypeIdRef.value;
        formValue.value.cmdOsTypeList = cmdOsTypeList.map((cmdOsType: { osTypeId: any; compatible: boolean }) => {
          if (cmdOsType.osTypeId === currentOsTypeId) {
            const osTypeValue = getDefaultOsTypeValue();
            osTypeValue.osTypeId = osTypeId;
            osTypeValue.compatible = compatible;
            return osTypeValue;
            // const key = Object.prototype.hasOwnProperty.call(cmdOsType, 'osTypeId') ? cmdOsType.osTypeId : osTypeId;
          }
          return cmdOsType;
        });
      } else {
        formValue.value.cmdOsTypeList.push(osTypeFormValue.value);
      }

      refreshCmdOsTypeTagList();
      message.success('成功');
      showOsTypeModal.value = false;
    } else {
      message.error('请填写完整信息');
    }
    osTypeFormBtnLoading.value = false;
  });
}

/* options */

const getDefaultOptionValue = () => {
  return {
    optionNameList: [],
    description: ''
  };
};

const optionTagClose = (index: number) => {
  formValue.value.cmdOptionList.splice(index, 1);
};

const optionTagEdit = (idx: number) => {
  isOptionEdit.value = true;
  const { optionNameList, description } = formValue.value.cmdOptionList[idx];
  optionFormValue.value = { optionNameList, description };
  showOptionModal.value = true;
  optionDialogTitle.value = '编辑';
  currentOptionIdx = idx;
};

function confirmOptionForm(e: MouseEvent) {
  e.preventDefault();
  optionFormBtnLoading.value = true;

  optionFormRef.value?.validate(errors => {
    if (!errors) {
      if (isOptionEdit.value) {
        formValue.value.cmdOptionList[currentOptionIdx] = optionFormValue.value;
      } else {
        formValue.value.cmdOptionList.push(optionFormValue.value);
      }
      showOptionModal.value = false;
    } else {
      message.error('请填写完整信息');
    }
    optionFormBtnLoading.value = false;
  });
}

/* parameter */
const getDefaultParameterValue = () => {
  return {
    index: formValue.value.cmdParameterList.length,
    description: null
  };
};
const parameterTagClose = (index: number) => {
  formValue.value.cmdParameterList.splice(index, 1);
};

const parameterTagEdit = (idx: number) => {
  isParameterEdit.value = true;
  const { index, description } = formValue.value.cmdParameterList[idx];
  parameterFormValue.value = { index, description };
  showParameterModal.value = true;
  parameterDialogTitle.value = '编辑';
  currentParameterIdx = idx;
};

function confirmParameterForm(e: MouseEvent) {
  e.preventDefault();
  parameterFormBtnLoading.value = true;

  parameterFormRef.value?.validate(errors => {
    if (!errors) {
      if (isParameterEdit.value) {
        formValue.value.cmdParameterList[currentParameterIdx] = parameterFormValue.value;
      } else {
        formValue.value.cmdParameterList.push(parameterFormValue.value);
      }
      formValue.value.cmdParameterList.sort((a: any, b: any) => {
        return a.index - b.index;
      });

      showParameterModal.value = false;
    } else {
      message.error('请填写完整信息');
    }
    parameterFormBtnLoading.value = false;
  });
}

/* 初始化数据 */
initOsTypeTree();
</script>
-->
