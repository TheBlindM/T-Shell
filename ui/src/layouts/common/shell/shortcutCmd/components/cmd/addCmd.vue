<template>
  <div style="width: 100%; height: 100%">
    <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
      <n-form-item label="名称" path="name">
        <n-input v-model:value="formValue.name" placeholder="命令" />
      </n-form-item>

      <n-form-item label="组" path="group">
        <n-tree-select
          v-model:value="formValue.shortcutCmdGroupIdList"
          multiple
          :default-expand-all="true"
          :options="shortcutCmdGroupTreeRef"
          label-field="name"
          key-field="id"
          children-field="children"
        />
      </n-form-item>

      <n-form-item label="命令" path="cmd">
        <n-space>
          <div
            v-for="(tag, index) in formValue.shortcutCmdImplList"
            :key="tag.cmdTemplate"
            style="display: inline-block"
          >
            <n-tag
              style="user-select: none; "
              type="info"
              closable
              @close="cmdImplTagClose(index)"
              @dblclick="cmdImplTagEdit(index)"
            >
							<n-ellipsis style="max-width: 100px">
								{{tag.cmdTemplate}}
							</n-ellipsis>
              {{ `[${tag.shortcutCmdTtyTypeNameList.join(',')}]` }}
            </n-tag>
          </div>

          <n-button
            size="small"
            @click="
              showCmdImplModal = true;
              isCmdImplEdit = false;
              cmdImplFormValue = getDefaultCmdImpl();
            "
            >新增
          </n-button>
        </n-space>
      </n-form-item>
    </n-form>
  </div>
  <n-space>
    <!--		<n-button @click="() => (showAddPageModal = false)">取消</n-button>-->
    <n-button type="info" @click="confirmForm">确定</n-button>
  </n-space>

  <!--	命令实现 -->
  <n-modal
    v-model:show="showCmdImplModal"
    v-model:title="cmdImplDialogTitle"
    :show-icon="false"
    preset="dialog"
    style="width: 420px"
  >
    <n-form
      ref="cmdImplFormRef"
      :model="cmdImplFormValue"
      :rules="cmdImplRules"
      label-placement="left"
      :label-width="80"
      class="py-4"
    >
      <n-form-item label="命令">
        <!--        <input @blur="blurHandle" />-->
        <n-dropdown
          :show="showSearchDropdownRef"
          :options="searchDropdownOptions"
          key-field="value"
          label-field="label"
          style="width: 290px; max-width: 290px"
          @select="handleSearchSelect"
        >
          <n-input
            ref="cmdImplInputRef"
            v-model:value="cmdImplFormValue.cmdTemplate"
            type="textarea"
            size="small"
            :autosize="{
              minRows: 3,
              maxRows: 10
            }"
            placeholder="cmd"
          >
            <!--            @input="handleSearchInput"
            @focus="handleChange"-->
            <Icon icon="typcn:flash-outline" />
          </n-input>
        </n-dropdown>
      </n-form-item>

      <n-form-item label="操作系统 (不填默认兼容所有系统)" path="osTypeName">
        <n-tree-select
          v-model:value="cmdImplFormValue.shortcutCmdTtyTypeIdList"
          multiple
          :options="ttyTypeOptionsRef"
          label-field="name"
          key-field="id"
          children-field="children"
        />
      </n-form-item>
    </n-form>

    <template #action>
      <n-space>
        <n-button
          @click="
            () => {
              showCmdImplModal = false;
            }
          "
          >取消</n-button
        >
        <n-button type="info" :loading="cmdImplFormBtnLoading" @click="confirmCmdImplForm">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { FormInst } from 'naive-ui';
import { useMessage } from 'naive-ui';
import { Icon } from '@iconify/vue';
import { useAppStore } from '@/store';
import { getListById, selectTree as ttyTypeSelectTree } from '@/theblind_shell/service/shell/ttyType';
import { add as addCmd } from '@/theblind_shell/service/shell/shortcutCmd';
import { parentTree as shortcutCmdGroupTree } from '@/theblind_shell/service/shell/shortcutCmdGroup';
import { retrieveCmd } from '@/theblind_shell/service/shell/retrieve';

const app = useAppStore();
app.disabledDrawerMouseleave();

interface Props {
  groupId: number;
}

const rules = {
  osTypeId: {
    required: true,
    message: '选择父级',
    trigger: 'blur'
  }
};

const cmdImplRules = {
  optionNameList: {
    trigger: ['change'],
    validator(rule: unknown, value: string[]) {
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

const ttyTypeOptionsRef = ref<any>([]);

/* cmdImpl */
const cmdImplFormRef = ref<FormInst | null>(null);
const showCmdImplModal = ref<boolean>(false);
const cmdImplDialogTitle = ref<string>('新建');
const isCmdImplEdit = ref<boolean>(false);
const cmdImplFormBtnLoading = ref<boolean>(false);
const cmdImplFormValue = ref<any>();
let currentCmdImplIdx = 0;
const shortcutCmdGroupTreeRef = ref<any>([]);

const getDefaultCmdValue = () => {
  return {
    name: '',
    shortcutCmdGroupIdList: [props.groupId],
    shortcutCmdImplList: []
  };
};

const initShortcutCmdGroupTree = () => {
  shortcutCmdGroupTree().then(resultData => {
    if (resultData.data !== null) {
      shortcutCmdGroupTreeRef.value = resultData.data;
    }
  });
};

const formValue = ref<any>(getDefaultCmdValue());

let formBtnLoading = false;

const initTtyTypeTree = () => {
  ttyTypeSelectTree().then(requestResult => {
    if (requestResult.data != null) {
      ttyTypeOptionsRef.value = requestResult.data;
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

/* CmdImpl */

const getDefaultCmdImpl = () => {
  return {
    cmdTemplate: '',
    shortcutCmdTtyTypeIdList: [],
    shortcutCmdTtyTypeNameList: []
  };
};

const handleOptionNameTagCreate = (optionName: string) => {
  if (cmdImplFormValue.value.optionNameList.includes(optionName)) {
    message.error('该参数已存在请勿重复添加');
    return null;
  }
  return optionName;
};

const cmdImplTagClose = (index: number) => {
  formValue.value.shortcutCmdImplList.splice(index, 1);
};

const cmdImplTagEdit = (idx: number) => {
  isCmdImplEdit.value = true;
  const { cmdTemplate, shortcutCmdTtyTypeIdList, shortcutCmdTtyTypeNameList } =
    formValue.value.shortcutCmdImplList[idx];
  cmdImplFormValue.value = { cmdTemplate, shortcutCmdTtyTypeIdList, shortcutCmdTtyTypeNameList };
  showCmdImplModal.value = true;
  cmdImplDialogTitle.value = '编辑';
  currentCmdImplIdx = idx;
};

function confirmCmdImplForm(e: MouseEvent) {
  e.preventDefault();
  cmdImplFormBtnLoading.value = true;
  cmdImplFormRef.value?.validate(async errors => {
    if (!errors) {
      const resultData = await getListById({ ttyTypeIds: cmdImplFormValue.value.shortcutCmdTtyTypeIdList });
      const { data } = resultData;
      cmdImplFormValue.value.shortcutCmdTtyTypeNameList = [];
      const { shortcutCmdTtyTypeNameList } = cmdImplFormValue.value;
      // shortcutCmdTtyTypeNameList = [];
      data?.forEach((val: { id: number; compatible: boolean; osTypeName: string }) => {
        shortcutCmdTtyTypeNameList.push(val.osTypeName);
      });
      if (isCmdImplEdit.value) {
        formValue.value.shortcutCmdImplList[currentCmdImplIdx] = cmdImplFormValue.value;
      } else {
        formValue.value.shortcutCmdImplList.push(cmdImplFormValue.value);
      }
      console.log(cmdImplFormRef.value);
      console.log(formValue.value);

      showCmdImplModal.value = false;
    } else {
      message.error('请填写完整信息');
    }
    cmdImplFormBtnLoading.value = false;
  });
}

/* 检索 */
const showSearchDropdownRef = ref(false);
const searchDropdownOptions = ref([]);
const cmdImplInputRef = ref<any>(null);
let type = 'ALL';
let currentCmdId: any = '0';

const blurHandle = e => {
  console.log('blur');
  console.log(e);
};
let inputDom: { selectionStart: any } | null = null;
const handleSearchInput = (cmd: string) => {
  message.info(`[Event input]: ${cmd}`);
  // 分割命令
  const cmdSplitList = cmd.trim().split(/\s+/);

  console.log(cmdSplitList);
  if (cmd === '') {
    searchDropdownOptions.value = [];
    showSearchDropdownRef.value = false;
    currentCmdId = '0';
    return;
  }
  const cmdImplInput = cmdImplInputRef.value;
  console.log(cmdImplInput);
  console.log(inputDom.selectionStart);

  const { cmdTemplate } = cmdImplFormValue.value;

  const { selectionStart } = inputDom;
  console.log(cmdTemplate.substring(0, selectionStart));
  const cmdSplitList1 = splitCmd(cmdTemplate.substring(0, selectionStart));
  console.log(cmdSplitList1);

  retrieveCmd(cmdSplitList1.pop(), currentCmdId, null).then(requestResult => {
    if (requestResult.data !== null) {
      searchDropdownOptions.value = requestResult.data;
      showSearchDropdownRef.value = true;
    }
  });
};

const handleChange = (e1, e2) => {
  console.log(e1);
  console.log(e2);
  inputDom = e1.target;
};
const splitCmd = cmd => {
  return cmd.trim().split(/\s+/);
};

const getCurrentCursorIndex = () => {
  return inputDom?.selectionStart;
};

const handleSearchSelect = (key: string | number, option: any) => {
  debugger;
  message.info(String(key));
  message.info(option.type);
  type = option.type;
  const currentCursorIndex = getCurrentCursorIndex();
  const { cmdTemplate } = cmdImplFormValue.value;
  console.log(cmdTemplate);
  const oldStr = cmdTemplate.substring(0, currentCursorIndex);
  const splitCmd1 = splitCmd(oldStr);
  const v1 = splitCmd1.pop();

  const reg = `(.*)${v1}`;
  console.log(option);
  // console.log(oldStr.replace(/(.*)i/, `$1${option.label}`));
  console.log(oldStr.replace(new RegExp(reg), `$1${option.label}`));
  cmdImplFormValue.value.cmdTemplate = cmdTemplate.replace(
    oldStr,
    oldStr.replace(new RegExp(reg), `$1${option.label}`)
  );

  switch (type) {
    case 'CMD':
      currentCmdId = key;
      break;
    default:
      break;
  }
  showSearchDropdownRef.value = false;
};
/* 初始化数据 */
initTtyTypeTree();
initShortcutCmdGroupTree();
</script>
