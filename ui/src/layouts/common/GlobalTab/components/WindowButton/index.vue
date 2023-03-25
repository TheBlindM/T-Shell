<template>
	<hover-container class="w-64px h-full" tooltip-content="缩小" placement="bottom-end" @click="minimize">
		<Icon icon="clarity:window-min-line" class="text-18px"/>
	</hover-container>
	<hover-container v-if="!isMaximized" class="w-64px h-full" tooltip-content="最大化" placement="bottom-end" @click="maximize">
		<Icon icon="typcn:arrow-maximise" class="text-18px"/>
	</hover-container>
	<hover-container v-else class="w-64px h-full" tooltip-content="还原" placement="bottom-end" @click="maximize">
		<Icon icon="material-symbols:close-fullscreen" class="text-18px"/>
	</hover-container>
	<hover-container class="w-64px h-full" tooltip-content="关闭" placement="bottom-end" @click="close">
		<Icon icon="material-symbols:close" class="text-18px"/>
	</hover-container>
</template>

<script setup lang="ts">
import {appWindow} from "@tauri-apps/api/window";
import { Icon } from '@iconify/vue';
import {useAppStore} from '@/store';
import {useLoading} from '@/hooks';
import {ref} from "vue";


defineOptions({name: 'ReloadButton'});

const isMaximized=ref(false);

const app = useAppStore();
const {loading, startLoading, endLoading} = useLoading();


const minimize = () => {
	appWindow.minimize();
}

const maximize = async () => {
	await appWindow.toggleMaximize();
	isMaximized.value=await appWindow.isMaximized();
}

const close = () => {
	appWindow.close();
}


function handleRefresh() {
	startLoading();
	app.reloadPage();
	setTimeout(() => {
		endLoading();
	}, 1000);
}
</script>

<style scoped></style>
