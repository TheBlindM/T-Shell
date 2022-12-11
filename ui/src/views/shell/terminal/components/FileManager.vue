<template>
  <div class="cursor-pointer" draggable="true" @mousewheel="onScrollFunc" @contextmenu="disabledContextMenu">
    <!-- 头部	-->
    <div
      id="fileManagerHead"
      style="
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        align-content: stretch;
        justify-content: flex-start;
        align-items: center;
      "
    >
      <div style="display: inline-block; width: 18%">
        <n-badge :value="transferTaskCount.downloadCount+transferTaskCount.uploadCount" style="top: -3px">
          <n-button style="display: inline-block" @click="clickTransfer">
            <Icon icon="iconoir:data-transfer-both" />
          </n-button>
        </n-badge>

        <n-button-group style="margin-left: 20px">
          <n-button :disabled="disabledHistoryBack" @click="historyBack">
            <Icon icon="mi:arrow-left" />
          </n-button>
          <n-button :disabled="disabledHistoryForward" @click="historyForward">
            <Icon icon="mi:arrow-right" />
          </n-button>
        </n-button-group>
      </div>
      <div style="display: inline-block; width: 45%">
        <n-scrollbar x-scrollable trigger="none">
          <n-breadcrumb separator=">">
            <n-breadcrumb-item v-for="(item, idx) in pathSplitList" :key="item" @click="breadcrumbClick(idx)"
              >{{ item }}
            </n-breadcrumb-item>
          </n-breadcrumb>
        </n-scrollbar>
      </div>
      <div style="display: inline-block; width: 25%; margin-left: 30px">
        <n-input placeholder="搜索(开发中)" />
      </div>
    </div>
    <!--	展示栏 -->
    <div id="test" style="display: inline-block; width: 100%; margin-top: 5px">
      <n-data-table
        draggable="false"
        size="small"
        :row-props="rowProps"
        :bordered="false"
        min-height="480"
        max-height="480"
        :columns="columns"
        :data="currentFileInfos"
        :loading="enableTableLoading"
        :row-class-name="rowClassName"
        @drop="dragdrop"
        @dragover="dragover"
        @contextmenu="handleContextMenu"
      />
      <n-dropdown
        placement="bottom-start"
        :on-clickoutside="onClickoutside"
        trigger="manual"
        :x="xRef"
        :y="yRef"
        :options="options"
        :show="showDropdown"
        :show-arrow="true"
        @select="handleSelect"
      />

      <n-drawer
        v-model:show="activeDrawer"
        height="91%"
        show-mask="transparent"
        placement="bottom"
        :trap-focus="false"
        :block-scroll="false"
        to="#test"
      >
        <n-drawer-content>
          <n-tabs type="segment" :on-update:value="startIntervalProgressTask">


            <n-tab-pane name="upload" :tab="tabTitleFormat('上传',transferTaskCount.uploadCount)">
              <div v-for="item in uploadProgressList" :key="item.transferRecordId">
                <div
                  style="
                    display: flex;
                    flex-direction: row;
                    flex-wrap: nowrap;
                    align-content: stretch;
                    justify-content: flex-start;
                    align-items: center;
                  "
                >
                  <div style="width: 30%">
                    <n-ellipsis>{{ item.fileName }}</n-ellipsis>
                  </div>
                  <div style="width: 50%; margin-left: 10px">
                    <n-progress type="line" :percentage="item.percent" :indicator-placement="'inside'" processing />
                  </div>
                  <div>
                    <n-button-group style="margin-left: 20px">
                      <n-button @click="clickPause(item)">
                        <Icon v-if="item.status === 'PAUSE'" icon="icon-park:play-two" />
                        <Icon v-else icon="icon-park:pause-one" />
                      </n-button>
                      <n-button>
                        <Icon icon="icon-park:folder-open" />
                      </n-button>
                      <n-button>
                        <Icon icon="icon-park:delete-three" />
                      </n-button>
                    </n-button-group>
                  </div>
                </div>
              </div>
            </n-tab-pane>
            <n-tab-pane name="download" :tab="tabTitleFormat('下载',transferTaskCount.downloadCount)">
              <div v-for="item in downloadProgressList" :key="item.transferRecordId">
                <div
                  style="
                    display: flex;
                    flex-direction: row;
                    flex-wrap: nowrap;
                    align-content: stretch;
                    justify-content: flex-start;
                    align-items: center;
                  "
                >
                  <div style="width: 30%">
                    <n-ellipsis>{{ item.fileName }}</n-ellipsis>
                  </div>
                  <div style="width: 50%; margin-left: 10px">
                    <n-progress type="line" :percentage="item.percent" :indicator-placement="'inside'" processing />
                  </div>
                  <div>
                    <n-button-group style="margin-left: 20px">
                      <n-button @click="clickPause(item)">
                        <template v-if="item.status === 'PAUSE'">
                          <Icon icon="icon-park:play-two" />
                        </template>

                        <Icon v-else icon="icon-park:pause-one" />
                      </n-button>
                      <n-button @click="clickDelete(item)">
                        <Icon icon="icon-park:delete-three" />
                      </n-button>
                    </n-button-group>
                  </div>
                </div>
              </div>
            </n-tab-pane>
            <n-tab-pane name="complete" :tab="tabTitleFormat('传输完成',transferTaskCount.completeCount)">
              <n-list hoverable clickable>
                <n-list-item v-for="item in completeList" :key="item.id">
                  <div
                    style="
                      display: flex;
                      flex-direction: row;
                      flex-wrap: nowrap;
                      align-content: stretch;
                      justify-content: flex-start;
                      align-items: center;
                    "
                  >
                    <div style="width: 38%">
                      <n-ellipsis>{{ item.fileName }}</n-ellipsis>
                    </div>
                    <div style="width: 20%; margin-left: 10px; color: #999">{{ item.createTime }}</div>
                    <div style="width: 20%; margin-left: 10px">
                      <n-tag v-if="item.operate === 'GET'">
                        <template #icon>
                          <Icon icon="icon-park:inbox-download-r" />
                        </template>
                        下载成功
                      </n-tag>
                      <n-tag v-else>
                        <template #icon>
                          <Icon icon="icon-park:inbox-upload-r" />
                        </template>
                        上传成功
                      </n-tag>
                      <!--                      <Icon v-if="item.operate === 'GET'" icon="icon-park:inbox-download-r" />
											<Icon v-else icon="icon-park:inbox-upload-r" />
											<span>{{ item.operate === 'GET' ? '下载成功' : '上传成功' }}</span>-->
                    </div>
                    <div>
                      <n-button-group style="margin-left: 20px">
                        <!--                        <n-button>
                          <Icon icon="icon-park:folder-open" />
                        </n-button>
                        <n-button>
                          <Icon icon="icon-park:folder-open" />
                        </n-button>-->
                        <n-button>
                          <Icon icon="icon-park:delete-three" @click="clickDelete(item)" />
                        </n-button>
                      </n-button-group>
                    </div>
                  </div>
                </n-list-item>
              </n-list>
            </n-tab-pane>
          </n-tabs>
        </n-drawer-content>
      </n-drawer>
    </div>

    <n-modal v-model:show="showModal" show-icon="false" preset="dialog" :title="modalTitle">
      <div style="width: 100%; height: 100%">
        <n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
          <n-form-item label="名称" path="fileName">
            <n-input v-model:value="formValue.fileName" placeholder="输入名称" />
          </n-form-item>
        </n-form>
      </div>
      <n-space>
        <n-button type="info" @click="confirmForm">确定</n-button>
      </n-space>
    </n-modal>
  </div>
</template>

<script setup lang="jsx">
import { h, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import {useMessage, useNotification} from 'naive-ui';
import { Icon } from '@iconify/vue';
import { listen } from '@tauri-apps/api/event';
import {
	continueTransfer,
	create,
	download,
	getCompleteList,
	getDownloadList,
	getFileInfos,
	getUploadList,
	pauseTransfer,
	removeDirectory,
	removeFile,
	rename,
	upload,
	deleteRecord, getTransferTaskCount, cancelOpenFile
} from '@/theblind_shell/service/shell/fileManager';
import { disabledContextMenu } from '@/utils/common/contextmenu';
import {shellWebSocket} from "@/utils/shell/msgWebSocket";
import {openFile} from "../../../../theblind_shell/service/shell/fileManager";


let webSocket = shellWebSocket;
const notification = useNotification();
const transferTaskCount=ref({uploadCount:0,downloadCount:0,completeCount:0});

const enableTableLoading = ref(false);
const props = defineProps({ channelId: String });
const currentDirectory = ref(null);
const currentFileInfos = ref([]);
const defaultRootDirectory = '此电脑';
const pathSplitList = ref([defaultRootDirectory]);
const historyPath = ref([]);
let currentHistoryPathIndex = -1;
const disabledHistoryBack = ref(true);
const disabledHistoryForward = ref(true);
const activeDrawer = ref(false);
const uploadProgressList = ref(new Map());
const downloadProgressList = ref(new Map());
const showModal = ref(false);
const modalTitle = ref(null);

const blankSpaceOptions = [
  {
    label: '新建文件夹',
    key: 'createDirectory'
  },
  {
    label: '新建文件',
    key: 'createFile'
  }
];
const fileOptions = [
  {
    label: '新建文件夹',
    key: 'createDirectory'
  },
  {
    label: '新建文件',
    key: 'createFile'
  },
  {
    label: '重命名',
    key: 'rename'
  },
  {
    label: '下载',
    key: 'download'
  },
  {
    label: () => h('span', { style: { color: 'red' } }, '删除'),
    key: 'deleteFile'
  }
];

const directoryOptions = [
  {
    label: '新建文件夹',
    key: 'createDirectory'
  },
  {
    label: '新建文件',
    key: 'createFile'
  },
  {
    label: '重命名',
    key: 'rename'
  },
  {
    label: () => h('span', { style: { color: 'red' } }, '删除'),
    key: 'deleteDirectory'
  }
];

const completeList = ref([]);
let timer;
let pauseStatus = false;
const message = useMessage();

const showDropdown = ref(false);
const xRef = ref(0);
const yRef = ref(0);
const options = ref([]);
let currentSelectPath = null;

const rules = {
  fileName: {
    required: true,
    message: '输入名称',
    trigger: 'blur'
  }
};
const formRef = ref(null);
const formValue = ref({
  fileName: null
});

let formBtnLoading = false;
let modalOperate = null;

const confirmForm = e => {
  e.preventDefault();
  if (formBtnLoading) {
    return;
  }
  formBtnLoading = true;
  formRef.value?.validate(errors => {
    if (!errors) {
      switch (modalOperate) {
        case 'createDirectory':
          create(props.channelId, currentDirectory.value, formValue.value.fileName, 'DIRECTORY').then(resultData => {
            if (resultData.error == null) {
              message.success('成功');
              showModal.value = false;
              loadFileInfos(currentDirectory.value);
            }
          });
          break;
        case 'createFile':
          create(props.channelId, currentDirectory.value, formValue.value.fileName, 'FILE').then(resultData => {
            if (resultData.error == null) {
              message.success('成功');
              showModal.value = false;
              loadFileInfos(currentDirectory.value);
            }
          });
          break;
        case 'rename':
          rename(props.channelId, currentSelectPath, formValue.value.fileName).then(resultData => {
            if (resultData.error == null) {
              message.success('成功');
              showModal.value = false;
              loadFileInfos(currentDirectory.value);
            }
          });
          break;
        default:
      }
    } else {
      message.error('请填写完整信息');
    }
    formBtnLoading = false;
  });
};

const handleContextMenu = e => {
  e.preventDefault();
  showDropdown.value = false;
  nextTick().then(() => {
    options.value = blankSpaceOptions;
    showDropdown.value = true;
    xRef.value = e.clientX;
    yRef.value = e.clientY;
  });
};

/**
 * @param item.transferRecordId  传输记录 ID
 * @param item.status  传输状态
 */
const clickPause = item => {
  pauseStatus = true;
  switch (item.status) {
    case 'PAUSE':
      continueTransfer(props.channelId, item.transferRecordId).then(() => {
        pauseStatus = false;
      });
      break;
    case 'PROCESS':
      pauseTransfer(props.channelId, item.transferRecordId).then(() => {
        pauseStatus = false;
      });
      break;
    default:
      break;
  }
};

const clickDelete = item => {
  const id = item.transferRecordId !== undefined ? item.transferRecordId : item.id;
  deleteRecord(id).then(requestResult => {
    if (requestResult.data) {
      message.success('记录已删除');
    }
  });
};

const initProgress = value => {
  if (pauseStatus) {
    return;
  }
  switch (value) {
    case 'upload':
      getUploadList(props.channelId).then(requestResult => {
        if (requestResult.data) {
          uploadProgressList.value = requestResult.data;
        }
      });
      break;
    case 'download':
      getDownloadList(props.channelId).then(requestResult => {
        if (requestResult.data) {
          downloadProgressList.value = requestResult.data;
        }
      });
      break;
    case 'complete':
      getCompleteList(props.channelId).then(requestResult => {
        if (requestResult.data) {
          completeList.value = requestResult.data;
        }
      });
      break;
    default:
  }
};
const startIntervalProgressTask = value => {
  if (timer) {
    clearInterval(timer);
  }
  timer = setInterval(() => {
    initProgress(value);
  }, 1000);
};

listen('tauri://file-drop', async event => {
  console.log(event);
  await upload(props.channelId, currentDirectory.value, event.payload);
});

watch(
  activeDrawer,
  () => {
    if (!activeDrawer.value && timer) {
      clearInterval(timer);
    }
  },
  { immediate: true }
);

const splitPath = path => {
  let result = [];
  if (path.indexOf('/') !== -1) {
    result = path.split('/');
    result[0] = defaultRootDirectory;
  } else if (path.indexOf('\\') !== -1) {
    result = path.split('\\');
  } else {
    result[0] = defaultRootDirectory;
  }
  pathSplitList.value = result;
  console.log(`splitPath  ${pathSplitList.value}`);
};
const jumpPath = path => {
  currentDirectory.value = path;
  splitPath(path);
  if (path === defaultRootDirectory) {
    path = null;
  }
  loadFileInfos(path);
};

const addHistoryPath = path => {
  if (currentHistoryPathIndex === 0) {
    clearHistoryPath();
    disabledHistoryForward.value = true;
  }
  if (historyPath.value.length === 0) {
    historyPath.value.push('此电脑');
  }
  historyPath.value.push(path);
  disabledHistoryBack.value = false;
};
const breadcrumbClick = idx => {
  console.log(`idx ${idx}`);
  const list = pathSplitList.value;
  if (list.length === 1) {
    return;
  }
  let queryPath = '';
  list.forEach((pathItem, index) => {
    if (index === 0 || index > idx) {
      return;
    }
    queryPath += `/${pathItem}`;
  });

  console.log(`queryPath 列表${queryPath}`);
  jumpPath(queryPath);
  addHistoryPath(queryPath);
};
const clearHistoryPath = () => {
  currentHistoryPathIndex = -1;
  historyPath.value = [];
};

const historyForward = () => {
  const end = historyPath.value.length - 1;
  currentHistoryPathIndex += 1;
  if (currentHistoryPathIndex === end) {
    disabledHistoryBack.value = false;
    disabledHistoryForward.value = true;
  }

  jumpPath(historyPath.value[currentHistoryPathIndex]);
};
const historyBack = () => {
  if (currentHistoryPathIndex === -1) {
    currentHistoryPathIndex = historyPath.value.length - 1;
  }
  currentHistoryPathIndex -= 1;
  jumpPath(historyPath.value[currentHistoryPathIndex]);
  if (currentHistoryPathIndex === 0) {
    disabledHistoryBack.value = true;
    disabledHistoryForward.value = false;
  }
};

const rowProps = row => {
  return {
    style: 'cursor: pointer;',
    onDblclick: () => {
      if (row.type === 'DIRECTORY') {
        jumpPath(row.path);
        addHistoryPath(row.path);
      } else if (row.type === 'FILE') {
				openFile(props.channelId,row.path);
			}
    },
    onContextmenu: e => {
      e.preventDefault();
      e.stopPropagation();
      showDropdown.value = false;
      currentSelectPath = row.path;
      switch (row.type) {
        case 'DIRECTORY':
          options.value = directoryOptions;
          break;
        default:
          options.value = fileOptions;
      }
      nextTick().then(() => {
        showDropdown.value = true;
        xRef.value = e.clientX;
        yRef.value = e.clientY;
      });
    }
  };
};

const onClickoutside = () => {
  showDropdown.value = false;
};

const nodeProps = option => {
  return {
    onContextmenu(e) {
      e.preventDefault();
      e.stopPropagation();
      options.value = directoryOptions;

      showDropdown.value = true;
      xRef.value = e.clientX;
      yRef.value = e.clientY;
    }
  };
};

const columns = [
  {
    title: '名称',
    key: 'name',
    sorter(rowA, rowB) {
      return rowA.name.localeCompare(rowB.name);
    },
    render(row) {
      let icon;
      if (row.type === 'DIRECTORY') {
        icon = 'icon-park:folder-close';
      } else {
        icon = 'ph:file-bold';
      }
      // return row.name;
      return (
        <div style="display: flex;align-items: center;justify-content: flex-start;">
          <Icon icon={icon} />
          <span style="margin-left: 5px;">{row.name}</span>
        </div>
      );
    }
  },
  {
    title: '修改日期',
    key: 'modifyDate',
    sorter(rowA, rowB) {
      return rowA.modifyDate.localeCompare(rowB.modifyDate);
    }
  },
  {
    title: '类型',
    key: 'type',
    sorter(rowA, rowB) {
      return rowA.type.localeCompare(rowB.type);
    }
  },
  {
    title: '大小',
    key: 'size',
    render(row) {
      return bytesToSize(row.size);
    },
    sorter(rowA, rowB) {
      return parseInt(rowA.size, 10) - parseInt(rowB.size, 10);
    }
  }
];

const rowClassName = row => {
  return 'cursor-pointer';
};

function bytesToSize(bytes) {
  // eslint-disable-next-line no-restricted-globals
  if (isNaN(bytes)) {
    return '';
  }
  const symbols = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  let exp = Math.floor(Math.log(bytes) / Math.log(2));
  if (exp < 1) {
    exp = 0;
  }
  const i = Math.floor(exp / 10);
  bytes /= 2 ** (10 * i);

  if (bytes.toString().length > bytes.toFixed(2).toString().length) {
    bytes = bytes.toFixed(2);
  }
  return `${bytes} ${symbols[i]}`;
}

function loadFileInfos(path) {
  enableTableLoading.value = true;
  getFileInfos(props.channelId, path).then(resultData => {
    if (resultData.data != null) {
      currentFileInfos.value = resultData.data;
      /* if (currentDirectory.value == null) {
				if (resultData.data.length !== 0) {
					const fileInfo = resultData.data[0];
					splitPath(fileInfo.path);
				}
			} */
      enableTableLoading.value = false;
      console.log(resultData.data);
    }
  });
}

const dragdrop = event => {
  const datas = event.dataTransfer.files;
  console.log(datas[0]);
  // eslint-disable-next-line no-restricted-syntax
  for (const item of datas) {
    console.log(item);
    upload(props.channelId, currentDirectory.value, item.name, item);
  }

  event.stopPropagation();
  event.preventDefault();
};
const dragover = event => {
  console.log('dragover');
  event.stopPropagation();
  event.preventDefault();
};
const onScrollFunc = event => {
  if (showDropdown.value) {
    event.preventDefault();
  }
};
const handleSelect = key => {
  showDropdown.value = false;
  switch (key) {
    case 'createDirectory':
      modalTitle.value = '新建文件夹';
      showModal.value = true;
      formValue.value.fileName = null;
      modalOperate = key;
      break;
    case 'createFile':
      modalTitle.value = '新建文件';
      showModal.value = true;
      formValue.value.fileName = null;
      modalOperate = key;
      break;
    case 'rename':
      modalTitle.value = '重命名';
      showModal.value = true;
      formValue.value.fileName = null;
      modalOperate = key;
      break;
    case 'download':
      download(props.channelId, currentSelectPath);
      break;
    case 'deleteFile':
      removeFile(props.channelId, currentSelectPath).then(() => {
        loadFileInfos(currentDirectory.value);
      });
      break;
    case 'deleteDirectory':
      removeDirectory(props.channelId, currentSelectPath).then(() => {
        loadFileInfos(currentDirectory.value);
      });
      break;
    default:
  }
};

const refreshTransferTaskCount = () => {
	getTransferTaskCount(props.channelId).then((requestResult)=>{
		if (requestResult.data) {
			transferTaskCount.value=requestResult.data;
		}
	});

}
let refreshTransferTaskCountTime;

const init = () => {

	console.log("fileManger init");
  loadFileInfos(currentDirectory.value);

	refreshTransferTaskCountTime = setInterval(() => {
		refreshTransferTaskCount();
	}, 500);
	webSocket?.addMonitor(props.channelId, 'TRANSFER_COMPLETE', message => {
		window.console.info(`TRANSFER_COMPLETE 接收 ${message}`);
		const msg = JSON.parse(message);

		notification.create({
			content: ()=>{
				return `${msg.operate==='GET'?'下载':'上传'}完成。 路径：${msg.writePath}`;
			},
			duration: 8000,
			meta: msg.fileName,
			keepAliveOnHover:true,
			closable:true
		});
	});


};

const clickTransfer = () => {
  activeDrawer.value = !activeDrawer.value;
  initProgress('upload');
};

const tabTitleFormat=(prefix,count)=>{
	return count==0?prefix:`${prefix}(${count})`
}

init();

onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer);
  }

	if (refreshTransferTaskCountTime) {
    clearInterval(refreshTransferTaskCountTime);
  }
});
</script>

<style>
#fileManagerHead .n-breadcrumb-item__separator {
  margin: 0 0 !important;
}
</style>
<style>
.cursor-pointer {
  cursor: pointer;
  user-select: none;
}
</style>
