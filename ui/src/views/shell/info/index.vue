<template>
  <n-card
    content-style="height:100%;display: flex;
    flex-direction: column;"
    class="h-full shadow-sm rounded-16px"
    style="user-select: none"
    @contextmenu="disabledContextMenu"
  >
    <!--		首页展示	-->
    <div
      class="pb-12px"
      style="
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        justify-content: center;
        color: rgb(24, 155, 255);
        margin-top: 5%;
        font-size: 65px;
      "
    >
      T-Shell
    </div>
    <!--	展示常用session	-->
    <div style="height: 60%">
      <div
        style="
          display: -webkit-box;
          display: -moz-box;
          display: -ms-flexbox;
          display: -webkit-flex;
          display: flex;
          flex-direction: row;
          justify-content: center;
          height: 100%;
        "
      >
        <n-card content-style="height:100%" title="常用Session" hoverable class="w-1/2">
          <n-button
            v-for="item in sessionTopList"
            :key="item.sessionId"
            type="info"
            dashed
            @dblclick="openSession(item)"
          >
            {{ item.sessionName }}
          </n-button>
        </n-card>
      </div>
    </div>
    <!--	底部	-->
    <div class="flex-center" style="flex-direction: column; margin-top: 3%; font-size: 16px">
      <div style="height: 25px; line-height: 25px">
        <n-button text style="margin-right: 5px">
          <Icon class="text-primary" icon="twemoji:flag-for-flag-china" /> </n-button
        >源于开源,拥抱开源
        <n-button text style="margin-left: 2px" @click="onGithubClick">
          <Icon class="text-primary" icon="logos:github-icon" />
        </n-button>
      </div>

      <div style="height: 25px; line-height: 25px">TheBlind@DMZ</div>
    </div>
  </n-card>
</template>

<script setup>
import { ref } from 'vue';
import { Icon } from '@iconify/vue';
import { nanoid } from 'nanoid';
import { useRouterPush } from '@/composables';
import { topList } from '@/theblind_shell/service/shell/connectionLog';
import { disabledContextMenu } from '@/utils/common/contextmenu';
import { routeName } from '../../../router';
import { toGithub } from '../../../theblind_shell/service/shell/desktop';

const { routerPush } = useRouterPush();
const sessionTopList = ref([]);

const init = () => {
  topList().then(requestResult => {
    if (requestResult.data) {
      sessionTopList.value = requestResult.data;
    }
  });
};
init();
const openSession = item => {
  routerPush({
    name: routeName('shell_terminal'),
    query: { sessionId: item.sessionId, channelId: nanoid(), title: item.sessionName },
    hash: '#DEMO_HASH'
  });
};

const onGithubClick = () => {
  toGithub();
};
</script>

<style scoped>
.n-card__content {
  height: 100%;
}
</style>
