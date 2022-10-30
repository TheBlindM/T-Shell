<!--
<template>
  <n-dropdown
    :show="showSearchDropdownRef"
    :options="searchDropdownOptions"
    key-field="value"
    label-field="label"
    style="width: 100%"
    @select="handleSearchSelect"
  >
    <n-input
      ref="cmdImplInputRef"
      v-model:value="cmdTemplateRef"
      type="textarea"
      size="small"
      :autosize="{
        minRows: 3,
        maxRows: 10
      }"
      placeholder="cmd"
      @focus="handleChange"
      @input="handleSearchInput"
    >
      <Icon icon="typcn:flash-outline" />
    </n-input>
  </n-dropdown>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useMessage } from 'naive-ui';
import { retrieve, parseShortcutCmdPlaceholders } from '@/theblind_shell/service/shell/retrieve';
import { getSingle as getHost } from '@/theblind_shell/service/shell/host';

const message = useMessage();

interface Props {
  /** hostId */
  hostId: number;
  channelId: number;
}

const props = defineProps<Props>();

const showSearchDropdownRef = ref(false);
const searchDropdownOptions = ref([]);
const cmdImplInputRef = ref<any>(null);
const cmdTemplateRef = ref<string>('');
let type = 'ALL';
let currentCmdId: any = '0';

let inputDom: { selectionStart: any } | null = null;

let osTypeId: null;

const initOsTypeId = () => {
  console.log(props.hostId);
  getHost(props.hostId).then(requestResult => {
    const host = requestResult.data;
    if (host !== null) {
      console.log(host.osType);
      osTypeId = host.osType;
    }
  });
};
initOsTypeId();

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

  const cmdTemplate = cmdTemplateRef.value;

  const { selectionStart } = inputDom;
  console.log(cmdTemplate.substring(0, selectionStart));
  const cmdSplitList1 = splitCmd(cmdTemplate.substring(0, selectionStart));
  console.log(cmdSplitList1);

  retrieve(cmdSplitList1.pop(), osTypeId).then(requestResult => {
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
  const cmdTemplate = cmdTemplateRef.value;

  switch (type) {
    case 'CMD':
      console.log(cmdTemplate);
      const oldStr = cmdTemplate.substring(0, currentCursorIndex);
      const splitCmd1 = splitCmd(oldStr);
      const v1 = splitCmd1.pop();
      const reg = `(.*)${v1}`;
      console.log(option);
      // console.log(oldStr.replace(/(.*)i/, `$1${option.label}`));
      console.log(oldStr.replace(new RegExp(reg), `$1${option.label}`));
      cmdTemplateRef.value = cmdTemplate.replace(oldStr, oldStr.replace(new RegExp(reg), `$1${option.label}`));
      currentCmdId = key;
      break;
    case 'SHORTCUT_CMD':
      parseShortcutCmdPlaceholders(option.value, osTypeId, props.channelId).then(requestResult => {
        if (requestResult.data != null) {
          cmdTemplateRef.value = requestResult.data;
        } else {
          cmdTemplateRef.value = '';
        }
      });
      break;
    default:
      break;
  }
  showSearchDropdownRef.value = false;
};
</script>

<style scoped></style>
-->
