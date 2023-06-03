<template>
  <n-divider title-placement="center">终端配置</n-divider>
  <n-form :model="formParams">
    <n-form-item label="可回滚行数">
      <n-input-number v-model:value="formParams.scrollbackLines" @change="handleChange" />
    </n-form-item>
    <n-form-item label="词分割符">
      <n-input v-model:value="formParams.wordSeparator" @change="handleChange" />
    </n-form-item>
    <n-form-item label="选择时复制">
      <n-switch v-model:value="formParams.copyOnSelect" @change="handleChange" />
    </n-form-item>
  </n-form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import {getAppearance, getTerminal, updAppearance, updTerminal} from '@/theblind_shell/service/shell/config';


const formParams = ref({
	scrollbackLines: undefined,
	wordSeparator: undefined,
	copyOnSelect: undefined,
});

const handleChange = (value) => {
	setTimeout(()=>{
		updTerminal(formParams.value);
	},100);
 return value;
};

const init = () => {
  getTerminal().then(resultData => {
    if (resultData.data != null) {
      formParams.value = resultData.data;
    }
  });
};
init();
</script>
<style scoped></style>
