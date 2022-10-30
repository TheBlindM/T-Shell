<template>
  <n-spin :show="showSpin">
    <div style="width: 100%; height: 100%">
      <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
        <n-form-item label="名称" path="cmd">
          <n-input v-model:value="formValue.name" placeholder="命令" />
        </n-form-item>

        <n-form-item label="组" path="describe">
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

        <n-form-item label="命令" path="options">
          <n-space>
            <div v-for="(tag, index) in formValue.shortcutCmdImplList" style="display: inline-block">
              <n-tag
                style="user-select: none"
                type="info"
                closable
                @close="cmdImplTagClose(index)"
                @dblclick="cmdImplTagEdit(index)"
              >
                {{ `${tag.cmdTemplate}[${tag.shortcutCmdTtyTypeNameList.join(',')}]` }}
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
            :options="osTypeOptionsRef"
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
  </n-spin>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import type { FormInst } from 'naive-ui';
import { useMessage } from 'naive-ui';
import { Icon } from '@iconify/vue';
import { useAppStore } from '@/store';
import { getListById, selectTree as osTypeSelectTree } from '@/theblind_shell/service/shell/ttyType';
import { add as addCmd, getSingle, upd } from '@/theblind_shell/service/shell/shortcutCmd';
import { parentTree as shortcutCmdGroupTree } from '@/theblind_shell/service/shell/shortcutCmdGroup';
import { retrieve, retrieveCmd } from '@/theblind_shell/service/shell/retrieve';

const app = useAppStore();
app.disabledDrawerMouseleave();
const showSpin = ref(true);
interface Props {
  id: number;
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

const showOsTypeModal = ref<boolean>(false);
const osTypeDialogTitle = ref<string>('新建');
const cmdOsTypeTagListRef = ref<CmdTag[]>([]);
const osTypeFormRef = ref<FormInst | null>(null);

const isOsTypeEdit = ref<boolean>(false);
const osTypeFormBtnLoading = ref<boolean>(false);
const osTypeOptionsRef = ref<any>([]);
const currentOsTypeIdRef = ref<number>();

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
    shortcutCmdGroupIdList: [],
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
  cmdOsTypeList.forEach((val: { osTypeId: number; compatible: boolean }, idx: number) => {
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

const initOsTypeTree = () => {
  osTypeSelectTree().then(requestResult => {
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
      upd(formValue.value, props.id).then(resultData => {
        if (resultData.data != null) {
          console.log('刷新');
          message.success('成功');
          emits('changeShowModal', false);
          emits('refreshTree');
          // app.enableDrawerMouseleave();
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
      const resultData = await getListById({ osTypeIds: cmdImplFormValue.value.shortcutCmdTtyTypeIdList });
      const { data } = resultData;
      cmdImplFormValue.value.shortcutCmdTtyTypeNameList = [];
      const { shortcutCmdTtyTypeNameList } = cmdImplFormValue.value;
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

const initDate = () => {
  getSingle(props.id).then(requestResult => {
    if (requestResult.data != null) {
      formValue.value = requestResult.data;
      showSpin.value = false;
    }
  });
};
/* 初始化数据 */
initDate();
initOsTypeTree();
initShortcutCmdGroupTree();
</script>
