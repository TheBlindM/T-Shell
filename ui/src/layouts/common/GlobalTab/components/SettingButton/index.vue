<template>
	<hover-container class="w-64px h-full" tooltip-content="设置" placement="bottom-end" @click="toSettingView">
		<Icon icon="icon-park-outline:setting-two" class="text-18px"/>
	</hover-container>
</template>

<script setup lang="ts">
import {Icon} from "@iconify/vue";
import {routeName} from "@/router";
import {useRouterPush} from "@/composables";

defineOptions({name: 'ProjectSettingButton'});

const {routerPush} = useRouterPush();

const toSettingView = () => {
	routerPush({
		name: routeName('project_setting'),
		hash: '#DEMO_HASH'
	});
}


</script>

<style scoped></style>
