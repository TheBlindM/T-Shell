<template>
  <div class="h-full shadow-sm rounded-16px" @contextmenu="disabledContextMenu" @mouseleave="hideDropdownMenu">
    <div id="container" style="height: 95.7%">
      <div
        :id="`terminal${channelId}`"
        class="h-full"
        @contextmenu="handleContextRightClickMenu"
        @scroll="onScroll"
      ></div>
      <n-dropdown
        :flip="false"
        key-field="value"
        :keyboard="true"
        label-field="label"
        style="width: 100%"
        placement="right-start"
        :x="x"
        :y="y"
        :options="searchDropdownOptions"
        :show="showSearchDropdownRef"
        :render-icon="renderDropdownIcon"
        :on-clickoutside="onSearchDropdownClickoutside"
        @contextmenu="disabledContextMenu"
        @select="handleSearchSelect"
      />

      <n-dropdown
        placement="bottom-start"
        trigger="manual"
        :x="rightClickMenuX"
        :y="rightClickMenuY"
        :options="rightClickOptions"
        :show="showRightClickMenuRef"
        :on-clickoutside="onRightClickMenuClickoutside"
        @select="handleRightClickMenuSelect"
      />
    </div>
    <!--		position: relative; top: -28px	-->
    <dark-mode-container style="height: 26px; position: relative; bottom: -1%">
      <n-button-group>
        <n-button size="tiny" style="height: 24px" @click="active = true"> 历史命令</n-button>
        <n-button size="tiny" style="height: 24px" @click="activeFileManager = true"> 文件管理</n-button>
      </n-button-group>
    </dark-mode-container>

    <n-modal
      v-model:show="active"
      transform-origin="center"
      style="height: 450px; width: 600px"
      preset="dialog"
      title="历史命令"
      size="huge"
      :show-icon="false"
      aria-modal="true"
      :on-after-leave="terminalFocus()"
      @contextmenu="disabledContextMenu"
    >
      <HistoryCmd :session-id="sessionId" session-type="0" />
    </n-modal>
    <n-modal
      v-model:show="activeFileManager"
      transform-origin="center"
      style="height: 620px; width: 900px"
      preset="dialog"
      size="huge"
      :show-icon="false"
      aria-modal="true"
      :on-after-leave="terminalFocus()"
      @contextmenu="disabledContextMenu"
    >
      <FileManager :channel-id="channelId" />
    </n-modal>
    <n-modal
      v-model:show="activePlaceholder"
      transform-origin="center"
      style="max-height: 320px; width: 400px"
      preset="dialog"
      size="huge"
      :show-icon="false"
      aria-modal="true"
      @contextmenu="disabledContextMenu"
    >
      <div class="wh-full mt-30px">
        <n-scrollbar style="max-height: 300px" trigger="none">
          <n-input-group v-for="(val, key) in placeholderItems" :key="key" class="mb-20px">
            <n-button type="primary" ghost> {{ key }}:</n-button>
            <n-input v-model:value="placeholderItems[key]" />
          </n-input-group>
          <!--          <div v-for="(val, key) in placeholderItems" :key="key" style="display: flex; flex-direction: row">
							<n-tag type="info" style="width: 120px" size="large" class="mr-5px"> {{ key }}:</n-tag>
							<n-input v-model:value="placeholderItems[key]"></n-input>
						</div>-->
        </n-scrollbar>
      </div>
      <div class="w-full">
        <n-button @click="onPlaceholderClick">确定</n-button>
      </div>
    </n-modal>

    <n-modal
      v-model:show="showKeyboardInteractiveModal"
      transform-origin="center"
      style="max-height: 320px; width: 400px"
      preset="dialog"
      size="huge"
      :show-icon="false"
      aria-modal="true"
    >
      <div class="wh-full mt-30px">
        <n-input v-model:value="keyboardInteractive.username" placeholder="输入用户名" />
        <n-input v-model:value="keyboardInteractive.pwd" type="password" placeholder="输入密码" />
      </div>
      <div class="w-full">
        <n-button class="w-full" @click="onKeyboardInteractiveClick">确定</n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="jsx">
import { h, nextTick, onActivated, onBeforeMount, onDeactivated, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useNotification } from 'naive-ui';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { Icon } from '@iconify/vue';
import { useTabStore, useAppStore } from '@/store';
import 'xterm/css/xterm.css';
import { MessageType, Msg, shellWebSocket } from '@/utils/shell/msgWebSocket';
import { close, localInitConnect, resize, sshInitConnect } from '@/theblind_shell/service/shell/terminal';
import { add as addHistoryCmd } from '@/theblind_shell/service/shell/historyCmd';
import { getMatchItems, parseTemplate } from '@/theblind_shell/service/shell/retrieve';
import HistoryCmd from '@/views/shell/terminal/components/HistoryCmd.vue';
import FileManager from '@/views/shell/terminal/components/FileManager.vue';
import { disabledContextMenu } from '@/utils/common/contextmenu';
import { cancelOpenFile } from '@/theblind_shell/service/shell/fileManager';
import { getSingle as getSshSession } from '@/theblind_shell/service/shell/host';

window.console.info('---------------setup-------------');
const app = useAppStore();
const route = useRoute();
const tabStore = useTabStore();
const notification = useNotification();
const active = ref(false);
const activeFileManager = ref(false);
const { fullPath } = route;
const { sessionId } = route.query;
const { channelId } = route.query;

let cmdTemplate = '';
let type = 'ALL';

const x = ref(0);
const y = ref(0);

let resizeTimeout;
let term;
let fitAddon = null;
let webSocket;

let currentOptionValue;

const terminalFocus = () => {
  term?.focus();
};

/* 展示快捷命令中 匹配的item */
const activePlaceholder = ref(false);
const placeholderItems = ref();

const onPlaceholderClick = () => {
  parseTemplate(currentOptionValue, channelId, placeholderItems.value);
  activePlaceholder.value = false;
};

// 右键菜单
const showRightClickMenuRef = ref(false);
const rightClickMenuX = ref(0);
const rightClickMenuY = ref(0);
const rightClickOptions = [
  {
    label: '复制',
    key: 'copy'
  },
  {
    label: '粘贴',
    key: 'paste'
  }
];

const handleRightClickMenuSelect = key => {
  showRightClickMenuRef.value = false;
  switch (key) {
    case 'copy':
      navigator.clipboard.writeText(term.getSelection());
      break;
    case 'paste':
      navigator.clipboard.readText().then(clipText => {
        webSocket?.sendJsonMessage(new Msg(channelId, clipText, MessageType.CMD));
        terminalFocus();
      });
      break;
    default:
  }
};
const handleContextRightClickMenu = e => {
  e.preventDefault();
  showRightClickMenuRef.value = false;
  nextTick().then(() => {
    showRightClickMenuRef.value = true;
    rightClickMenuX.value = e.clientX;
    rightClickMenuY.value = e.clientY;
  });
};

/* 下拉菜单 */
const showSearchDropdownRef = ref(false);
const searchDropdownOptions = ref([]);
let isInMenuSelection = false;

const getCurrentCursorIndex = () => {
  return term.buffer.normal.cursorX;
};

const getCurrentCursorRow = () => {
  window.console.log(`term.buffer.normal.viewportY:${term.buffer.normal.viewportY}`);
  window.console.log(`erm.buffer.normal.cursorY:${term.buffer.normal.cursorY}`);
  return term.buffer.normal.viewportY + term.buffer.normal.cursorY;
};
const splitCmd = cmd => {
  return cmd.trim().split(/\s+/);
};
let currentSelectCmd = null;
const handleSearchInput = async (cmd, selectionStart, skipVerify) => {
  window.console.info('handleSearchInput-----------start');
  window.console.info(`[Event input]: ${cmd}`);
  if (cmd.trim() === '') {
    window.console.info('当前 cmd 为空');
    showSearchDropdownRef.value = false;
    // searchDropdownOptions.value = [];
    return;
  }
  // 分割命令
  /*  const cmdSplitList1 = splitCmd(cmd.trim().substring(0, selectionStart));

	const retrievedCommand = cmdSplitList1.pop();
	if (retrievedCommand === currentSelectCmd) {
	} */
  const retrievedCommand = cmd.trim().substring(0, selectionStart);
  webSocket?.sendJsonMessage(
    new Msg(channelId, JSON.stringify({ term: retrievedCommand, skipVerify }), MessageType.RETRIEVE_CMD)
  );
};

const renderDropdownIcon = option => {
  let icon;
  switch (option.type) {
    case 'SHORTCUT_CMD':
      icon = 'icon-park:lightning';
      break;
    case 'HISTORY_CMD':
    case 'CMD':
      // icon = 'octicon:command-palette-24';
      icon = 'icon-park:terminal';
      break;
    default:
  }
  window.console.info(icon);
  /* class: 'text-primary' */
  return h(Icon, { icon });
};
const onSearchDropdownClickoutside = () => {
  showSearchDropdownRef.value = false;
};

const onRightClickMenuClickoutside = () => {
  showRightClickMenuRef.value = false;
};

let cmdStartIndex = 0;
/**
 * 处理下拉选择
 */
const handleSearchSelect = (key, option) => {
  type = option.type;
  currentOptionValue = option.value;
  switch (type) {
    case 'CMD': {
      const currentCursorIndex = getCurrentCursorIndex();
      const oldStr = cmdTemplate.substring(0, currentCursorIndex - cmdStartIndex);
      const splitCmd1 = splitCmd(oldStr);
      const v1 = splitCmd1.pop();
      const reg = `^(.*?)${v1}`;
      currentSelectCmd = option.label.replace(new RegExp(reg), '');

      webSocket?.sendJsonMessage(new Msg(channelId, currentSelectCmd, MessageType.CMD));
      break;
    }
    case 'SHORTCUT_CMD':
      getMatchItems(option.value, channelId).then(requestResult => {
        if (requestResult.data != null) {
          placeholderItems.value = requestResult.data;
          if (Object.keys(requestResult.data).length !== 0) {
            activePlaceholder.value = true;
          }
        }
      });
      break;
    case 'HISTORY_CMD': {
      const reg2 = `^(.*?)${cmdTemplate}`;
      webSocket?.sendJsonMessage(new Msg(channelId, option.label.replace(new RegExp(reg2), ''), MessageType.CMD));
      break;
    }
    default:
      break;
  }

  showSearchDropdownRef.value = false;
  isInMenuSelection = false;
};

async function initConnect() {
  const { cols, rows } = term;
  let connectState = false;
  const width = document.getElementById('container')?.offsetWidth;
  const height = document.getElementById('container')?.offsetHeight;
  webSocket?.addMonitor(channelId, 'CMD', message => {
    window.console.info(`CMD 接收 ${term}`);
    term.write(message);
  });

  webSocket?.addMonitor(channelId, 'RETRIEVE_CMD', message => {
    window.console.info(`RETRIEVE_CMD 接收`);
    term.retrieve(JSON.parse(message));
  });
  if (sessionId === undefined) {
    window.console.info('local');
    const resultData = await localInitConnect(channelId, cols, rows, width, height);
    if (resultData.error == null) {
      connectState = true;
    }
  } else {
    window.console.info('ssh');
    const sshSessionResult = await getSshSession(sessionId);
		console.log(sshSessionResult)
    let resultData = null;
    if (sshSessionResult.data.authType === 'KEYBOARD_INTERACTIVE') {
			console.log(sshSessionResult.data.authType)
			showKeyboardInteractiveModal.value=true;


    } else {
      resultData = await sshInitConnect(sessionId, channelId, cols, rows, width, height, null, null);
    }

    if (resultData.error == null) {
      connectState = true;
    }
  }
  return connectState;
}

const getDropdownMenuHeight = () => {
  const menuItemHeight = 34;
  window.console.info(getDropdownMenuHeight);
  window.console.info(searchDropdownOptions.value.length);
  return searchDropdownOptions.value.length * menuItemHeight + 10;
};

/**
 *@param e.toElement 鼠标到的元素
 */
const hideDropdownMenu = e => {
  // 进入 下拉菜单
  if (e?.toElement?.className.indexOf('n-dropdown') !== -1) {
    return;
  }
  isInMenuSelection = false;
  showSearchDropdownRef.value = false;
  searchDropdownOptions.value = [];
};

onBeforeMount(() => {
  tabStore.setActiveTabTitle(String(route.query.title));
});

onActivated(() => {
  window.console.info('onActivated');
  terminalFocus();
  fitAddon.fit();
});
const notificationMap = new Map();
const onScroll = e => {
  window.console.log(`onScroll:${e}`);
};

/* 键盘交互 */
const showKeyboardInteractiveModal=ref(false);
const keyboardInteractive = ref({username:undefined,pwd:undefined});
// 连接状态
let connectState = false;
const onKeyboardInteractiveClick = () => {
	const { cols, rows } = term;
	const width = document.getElementById('container')?.offsetWidth;
	const height = document.getElementById('container')?.offsetHeight;

		sshInitConnect(sessionId, channelId, cols, rows, width, height,keyboardInteractive.value.username ,keyboardInteractive.value.pwd).then(resultData=>{
			if (resultData.error == null) {
				connectState = true;
				showKeyboardInteractiveModal.value=false;
			}
		});

}


onMounted(() => {
  webSocket = shellWebSocket;

  window.console.info('onMounted');

  webSocket?.addMonitor(channelId, 'OPEN_FILE_PROGRESS', message => {
    window.console.info(`OPEN_FILE_PROGRESS 接收 ${message}`);
    const msg = JSON.parse(message);
    const { transferRecordId } = msg;
    if (notificationMap.has(transferRecordId)) {
      const dom = notificationMap.get(transferRecordId);
      if (msg.status === 'COMPLETE') {
        dom.destroy();
        notificationMap.delete(transferRecordId);
        return;
      }
      dom.content = `正在打开中:${msg.percent}%`;
    } else {
      const info = notification.create({
        content: () => {
          return `正在打开中:${msg.percent}%`;
        },
        meta: msg.fileName,
        avatar: () => {
          return <Icon class="text-primary" icon="eos-icons:bubble-loading" />;
        },
        onClose: () => {
          cancelOpenFile(transferRecordId);
        }
      });
      notificationMap.set(transferRecordId, info);
    }
  });

  const fontSize = 18;
  term = new Terminal({
    rendererType: 'canvas',
    name: `terminal${channelId}`,
    rightClickSelectsWord: true,
    scrollback: 800,
    disableStdin: false,
    cursorStyle: 'block',
    cursorBlink: true,
    tabStopWidth: 8,
    screenKeys: true,
    allowProposedApi: true,
    fontSize, // 字体大小
    theme: {
      foreground: '#f8ecec', // 字体
      background: '#060101' // 背景色
      // cursor: "help",//设置光标
    }
  });

  fitAddon = new FitAddon();
  term.loadAddon(fitAddon);

  async function resizeScreen() {
    window.console.info('调整屏幕大小');
    if (resizeTimeout) {
      clearTimeout(resizeTimeout);
    }
    resizeTimeout = setTimeout(() => {
      resizeTimeout = null;
      fitAddon.fit();
      term.scrollToBottom();
    }, 100);
  }

  window.addEventListener('resize', resizeScreen);

  // 第一次进入
  let isEnterForTheFirstTime = true;
  let currentRenderText = '';
  // 是否在输入数据中
  let isOnInputData = false;

  let currentCursorRow;
  // 是否处于 命令输入中
  let isInCommandInput = null;

  /* term  监听渲染时  事件值(item)包含渲染区域的开始行和结束行  */
  term.onRender(item => {
    window.console.info('onRender');
    window.console.info(`${JSON.stringify(item)}`);
    window.console.info(`isOnInputData:${isOnInputData},isEnterForTheFirstTime:${isEnterForTheFirstTime}`);

    // 在输入命令中
    // 第一次进入或者 不在输入时  或者 不是命令输入
    // 当前渲染行不是光标所在行
    if (isEnterForTheFirstTime || !isOnInputData || item.start !== term.buffer.normal.cursorY) {
      return;
    }
    window.console.info(`当前行${getCurrentCursorRow()}`);
    window.console.info(term?.buffer.normal.getLine(getCurrentCursorRow())?.translateToString(true));
    window.console.info(term?.buffer);
    cmdTemplate = term?.buffer.normal
      .getLine(getCurrentCursorRow())
      ?.translateToString(true, cmdStartIndex, getCurrentCursorIndex());
    window.console.info(`当前 渲染命令${cmdTemplate}`);
    // 防止应重复渲染而导致下拉菜单重复出现
    if (currentRenderText === cmdTemplate) {
      return;
    }

    window.console.info(`isInCommandInput:${isInCommandInput}`);
    if (isInCommandInput !== null && !isInCommandInput) {
      return;
    }
    let skipVerify = false;
    if (currentCursorRow === getCurrentCursorRow()) {
      skipVerify = true;
    }

    const currentCursorIndex = getCurrentCursorIndex();
    currentCursorRow = getCurrentCursorRow();
    currentRenderText = cmdTemplate;

    handleSearchInput(cmdTemplate, currentCursorIndex - cmdStartIndex, skipVerify);
    //  isOnInputData = false;
  });
  term.onResize(pty => {
    if (!connectState) return;
    const containerElement = document.getElementById('container');
    resize(channelId, pty.cols, pty.rows, containerElement?.offsetWidth, containerElement?.offsetHeight);
  });
  /**
   * @param data {Object,null}
   * @param data.isInCommandInput {Object}
   */
  term.retrieve = data => {
    if (data !== null) {
      window.console.info('------------retrieve-------------');
      window.console.info(data);
      isInCommandInput = data.isInCommandInput;
      const { retrieves } = data;

      if (retrieves.length !== 0) {
        searchDropdownOptions.value = retrieves;
        refreshSearchDropdownPosition();
        showSearchDropdownRef.value = true;
      } else {
        showSearchDropdownRef.value = false;
      }
    }
  };

  function refreshSearchDropdownPosition() {
    const element = document.getElementById(`terminal${channelId}`);
    window.console.info('------------------------menu-------------------------');

    x.value = term.textarea.offsetLeft + element.offsetLeft;
    let ypoint = term.textarea.offsetTop + element.offsetTop + fontSize;
    const dropdownMenuHeight = getDropdownMenuHeight();
    if (ypoint + dropdownMenuHeight > element.offsetHeight) {
      ypoint = term.textarea.offsetTop + element.offsetTop - fontSize / 2 - dropdownMenuHeight;
    }
    y.value = ypoint;
  }
  const onNewLineHandle = () => {
    isEnterForTheFirstTime = true;
    if (cmdTemplate !== null && cmdTemplate.trim().length !== 0) {
      addHistoryCmd({ cmdText: cmdTemplate, sessionId, sessionType: 0 });
    }
    if (showSearchDropdownRef.value) {
      showSearchDropdownRef.value = false;
    }
    cmdTemplate = null;
    isInCommandInput = null;
    isOnInputData = false;
  };

  const onUpAndDownKeysHandle = () => {
    if (showSearchDropdownRef.value) {
      return;
    }
    isOnInputData = false;
  };

  term.onData(data => {
    if (isEnterForTheFirstTime) {
      cmdStartIndex = term.buffer.normal.cursorX;
      isEnterForTheFirstTime = false;
    }
    window.console.info(`onData 接收 ${data}`);
    switch (data) {
      case '\r':
      case '\n':
      case '\r\n':
      case '13': // 回车
        onNewLineHandle();
        break;
      case '38':
      case '40':
      case '\u001bOB':
      case '\u001bOA':
        onUpAndDownKeysHandle();
        break;
      default:
        isOnInputData = true;
    }

    webSocket?.sendJsonMessage(new Msg(channelId, data, MessageType.CMD));
  });
  watch(
    () => showSearchDropdownRef.value,
    () => {
      if (!showSearchDropdownRef.value) {
        isInMenuSelection = false;
      }
    },
    { immediate: true }
  );

  term.attachCustomKeyEventHandler(e => {
    const { keyCode, ctrlKey } = e;
    // 38,40 上下
    const moveKey = [38, 40];
    const menuKeyCodes = [38, 40, 13, 27];
    const cKeyCode = 67;

    // ctrl+c 事件
    if (ctrlKey && keyCode === cKeyCode) {
      isEnterForTheFirstTime = true;
      if (showSearchDropdownRef.value) {
        showSearchDropdownRef.value = false;
      }
      showRightClickMenuRef.value = false;
      cmdTemplate = null;
      isInCommandInput = null;
      isOnInputData = false;
      return true;
    }

    if (moveKey.includes(keyCode) && showSearchDropdownRef.value) {
      isInMenuSelection = true;
    }

    if (isInMenuSelection && menuKeyCodes.includes(keyCode)) {
      if (keyCode === 27) {
        showSearchDropdownRef.value = false;
      }
      return false;
    }
    window.console.info(`term 处理 该按键`);
    return true;
  });
  term.open(document.getElementById(`terminal${channelId}`));
  fitAddon.fit();

  initConnect().then(data => {
    connectState = data;
  });
});

onDeactivated(() => {
  window.console.info(`onDeactivated${fullPath}`);
  if (!tabStore.includeTab(fullPath)) {
    window.console.info(`close${channelId}`);
    close(channelId);
    term.dispose();
  }
  showSearchDropdownRef.value = false;
  showRightClickMenuRef.value = false;
});

// 监听 侧边栏固定
watch(
  () => app.mixSiderFixed,
  () => {
    if (fitAddon) {
      setTimeout(() => {
        fitAddon.fit();
      }, 250);
    }
  }
);
</script>

<style>
/*.terminal-container {
  !* this is important *!
  overflow: hidden;
}

.xterm .xterm-viewport {
  !* see : https://github.com/xtermjs/xterm.js/issues/3564#issuecomment-1004417440 *!
  width: initial !important;
}*/
</style>
