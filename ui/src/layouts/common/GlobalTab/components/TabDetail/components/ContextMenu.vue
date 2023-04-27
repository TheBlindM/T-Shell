<template>
	<n-dropdown
		:show="dropdownVisible"
		:options="options"
		placement="bottom-start"
		:x="x"
		:y="y"
		@clickoutside="hide"
		@select="handleDropdown"
	/>
	<n-modal v-model:show="showSyncChannelModal" :show-icon="false" preset="dialog" style="width: 306px;height:200px"
					 title="频道">
		<div style="margin: 10px 0px; padding: 5px; background-color: #f2f2f2; border-radius: 6px">
			<n-scrollbar style="height: 119px" trigger="none">
				<n-space>
					<n-button
						v-for="item in syncChannels"
						:key="item"
						:class="item.title == currentSyncChannelId ? 'active' : ''"
						:style="{ backgroundColor: item.backgroundColor }"
						strong
						secondary
						type="info"
						@click="joinChannel(item.title)"
					>{{ item.title }}
					</n-button
					>
				</n-space>
			</n-scrollbar>
		</div>
	</n-modal>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue';
import type {DropdownOption} from 'naive-ui';
import {useAppStore, useTabStore} from '@/store';
import {getUrlParams, iconifyRender} from '@/utils';
import {bindChannel, getChannelId, removeBindChannel} from '@/theblind_shell/service/shell/channel';

const defaultSyncChannel = {
	A: '#C71585',
	B: '#4B0082',
	C: '#F5FFFA',
	D: '#B8860B',
	E: '#87CEFA',
	F: '#000000',
	G: '#556B2F'
};

const syncChannels = computed(() => {
	const list: any[] = [];
	Object.keys(defaultSyncChannel).forEach((key) => {
		list.push({title: key, backgroundColor: defaultSyncChannel[key]});
	})
	return list;
});

defineOptions({name: 'ContextMenu'});
const showSyncChannelModal = ref(false);

interface Props {
	/** 右键菜单可见性 */
	visible?: boolean;
	/** 当前路由路径 */
	currentPath?: string;
	/** 鼠标x坐标 */
	x: number;
	/** GlobalTabRoute */
	item: GlobalTabRoute | undefined;
	/** 鼠标y坐标 */
	y: number;
}

const props = withDefaults(defineProps<Props>(), {
	visible: false,
	currentPath: ''
});

interface Emits {
	(e: 'update:visible', visible: boolean): void;
}

let sshChannelId: any;
const currentSyncChannelId = ref(null);
const emit = defineEmits<Emits>();

const app = useAppStore();
const tab = useTabStore();

const dropdownVisible = computed({
	get() {
		return props.visible;
	},
	set(visible: boolean) {
		emit('update:visible', visible);
	}
});

function hide() {
	dropdownVisible.value = false;
}

type DropdownKey =
	'reload-current'
	| 'close-current'
	| 'close-other'
	| 'close-left'
	| 'close-right'
	| 'close-all'
	| 'join-channel';
type Option = DropdownOption & {
	key: DropdownKey;
};

const options = computed<Option[]>(() => {
	const list = [
		{
			label: '重新加载',
			key: 'reload-current',
			disabled: props.currentPath !== tab.activeTab,
			icon: iconifyRender('ant-design:reload-outlined')
		},
		{
			label: '关闭',
			key: 'close-current',
			disabled: props.currentPath === tab.homeTab.fullPath,
			icon: iconifyRender('ant-design:close-outlined')
		},
		{
			label: '关闭其他',
			key: 'close-other',
			icon: iconifyRender('ant-design:column-width-outlined')
		},
		{
			label: '关闭左侧',
			key: 'close-left',
			icon: iconifyRender('mdi:format-horizontal-align-left')
		},
		{
			label: '关闭右侧',
			key: 'close-right',
			icon: iconifyRender('mdi:format-horizontal-align-right')
		},
		{
			label: '关闭所有',
			key: 'close-all',
			icon: iconifyRender('ant-design:line-outlined')
		}
	];

	if (props.item?.meta.isTty) {
		list.push({
			label: '加入同步频道',
			key: 'join-channel',
			icon: iconifyRender('ic:round-sync')
		});
	}
	return list;
});

const actionMap = new Map<DropdownKey, () => void>([
	[
		'reload-current',
		() => {
			app.reloadPage();
		}
	],
	[
		'close-current',
		() => {
			tab.removeTab(props.currentPath);
		}
	],
	[
		'close-other',
		() => {
			tab.clearTab([props.currentPath]);
		}
	],
	[
		'close-left',
		() => {
			tab.clearLeftTab(props.currentPath);
		}
	],
	[
		'close-right',
		() => {
			tab.clearRightTab(props.currentPath);
		}
	],
	[
		'close-all',
		() => {
			tab.clearAllTab();
		}
	],
	[
		'join-channel',
		async () => {
			const urlParams = getUrlParams(props.currentPath);
			const result = await getChannelId(urlParams.channelId);
			sshChannelId = urlParams.channelId;
			currentSyncChannelId.value = result.data;
			showSyncChannelModal.value = true;
		}
	]
]);

const joinChannel = (syncChannelId: string | null) => {
	if (syncChannelId === currentSyncChannelId.value) {
		removeBindChannel(sshChannelId);
		currentSyncChannelId.value = null;
		props.item.meta.syncChannel = null;
	} else {
		bindChannel(syncChannelId, sshChannelId).then(() => {
			props.item.meta.syncChannel = {name: syncChannelId, backgroundColor: defaultSyncChannel[syncChannelId]};
			currentSyncChannelId.value = syncChannelId;
		});
	}
	showSyncChannelModal.value = false;
};

function handleDropdown(optionKey: string) {
	const key = optionKey as DropdownKey;
	const actionFunc = actionMap.get(key);
	if (actionFunc) {
		actionFunc();
	}
	hide();
}
</script>

<style scoped>
.active {
	border: 3px #2d8cf0 solid;
}
</style>
