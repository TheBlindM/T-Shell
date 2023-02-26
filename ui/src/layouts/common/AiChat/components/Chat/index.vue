<template>
  <div class="chatAppBody">
    <div class="chatBox">
			<template v-for="chatInfo in chatList">
      <div  class="chatRow" :class=" chatInfo.type == 'people'?'chatRowMe':'' ">
        <div v-if="chatInfo.type == 'ai'" class="chatMsgContent">
          <div class="chatUsername">小T</div>
          <div class="chatContent">{{ chatInfo.message }}</div>
        </div>
        <div v-else class="chatContent">{{ chatInfo.message }}</div>
      </div>
			</template>
    </div>

    <div class="chatBottom">
      <n-input
        placeholder="请输入问题"
        type="textarea"
        size="small"
				v-model:value="question"
				@keydown.enter="onEnter"
        :autosize="{
          minRows: 3,
          maxRows: 5
        }"
				:disabled="!inputActive"
      ></n-input>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAppStore } from '@/store';
import {getAiChat} from "@/theblind_shell/service/shell/aiChat";

defineOptions({ name: 'Chat' });

const question=ref(null);
const app = useAppStore();
const chatList = ref([
  {
    message: '你好，我是小T。有问题欢迎问我',
    type: 'ai'
  },
]);

const inputActive=ref(true);

const onEnter = (e) => {
	if (question.value.trim() == '') {
		return;
	}

	//禁止输入
	inputActive.value=false;
	const questionVal=question.value;
	question.value=null;
	chatList.value.push({
		message: questionVal,
		type: 'people'
	})

	chatList.value.push({
		message: "小T正在思考中...",
		type: 'ai'
	})
	getAiChat(questionVal).then(resultData => {
		if (resultData.data != null) {
			chatList.value[chatList.value.length-1].message= resultData.data
		}
		inputActive.value=true;
	})
}

const onlyAllowText = (value) => {
	return value.trim()==''? false: true;
}




</script>

<style lang="scss" scoped>
.n-scrollbar-content .n-drawer-body-content-wrapper{
	height: 100% !important;
}
.chatAppBody {
  display: flex;
  flex-direction: column;
  height: 86.8vh;
  background-color: #f1f5f8;
}
.chatTitle {
  background: #fff;
}
.chatBox {
	height: 100%;
  flex: 1;
  padding: 0 5px;
	overflow-y: scroll;
}
.chatBottom {
  background: #fff;
}
.chatRow {
  display: flex;
  align-items: flex-end;
  margin: 5px 0px;
}
.chatAvatar {
  margin-right: 5px;
  flex-shrink: 0;
}
.chatUsername {
  font-size: 12px;
  white-space: nowrap;
  color: #999;
  margin-bottom: 2px;
}
.chatContent {
  border-radius: 10px 10px 10px 0px;
  padding: 10px;
  background-color: rgb(255, 255, 255);
  box-shadow: 0 5px 30px rgb(50 50 93 / 8%), 0 1px 3px rgb(0 0 0 / 5%);
  font-size: 14px;
  word-break: break-all;
  line-height: 21px;
}
.chatRowMe {
  justify-content: flex-end;
}
.chatRowMe .chatContent {
  border-radius: 10px 10px 0px 10px;
}
</style>