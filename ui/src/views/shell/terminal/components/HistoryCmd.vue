<template>
  <div style="height: 100%; width: 100%" @contextmenu="disabledContextMenu">
    <div>
      <n-date-picker
        v-model:value="range"
        type="daterange"
        value-format="yyyy.MM.dd"
        clearable
        @update:value="onDateChange"
      />
    </div>
    <div style="margin: 10px 0px; padding: 5px; background-color: #f2f2f2; border-radius: 6px">
      <n-scrollbar style="height: 300px" trigger="none">
        <n-space>
          <n-button v-for="item in dataRef" :key="item" strong secondary type="info">{{ item.cmdText }}</n-button>
        </n-space>
      </n-scrollbar>
    </div>
    <div>
      <n-pagination v-model:page="page" :page-count="pageCount" simple />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { getPage } from '@/theblind_shell/service/shell/historyCmd';
import { disabledContextMenu } from '@/utils/common/contextmenu';

interface Props {
  sessionId: any;
  sessionType: any;
}

const props = defineProps<Props>();
const page = ref(1);
const pageCount = ref(0);
const dataRef = ref<any>([]);
const range = ref<[number, number]>([Date.now(), Date.now()]);

const initPage = (startDate: any, endDate: any) => {
  getPage(page.value - 1, 30, {
    sessionId: props.sessionId,
    sessionType: props.sessionType,
    startDate,
    endDate
  }).then(requestResult => {
    if (requestResult.data != null) {
      dataRef.value = requestResult.data.list;
      pageCount.value = requestResult.data.count;
    }
  });
};
watch(
  page,
  () => {
    initPage(range.value[0], range.value[1]);
  },
  { immediate: false }
);
const onDateChange = v => {
  console.log(v);
  initPage(v[0], v[1]);
};

initPage(range.value[0], range.value[1]);
</script>

<style scoped></style>
