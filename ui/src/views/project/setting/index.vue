<template>
	<div class="content">
		<div class="nav">
			<n-space vertical>
				<n-button v-for="item in buttons" style="width: 100%"    :class="{ active: item.active }"
									@click="togglePay(item)">
					<template #icon>
						<Icon :icon="item.icon" />
					</template>
					{{ item.title }}
				</n-button>
			</n-space>
		</div>
		<n-scrollbar style="max-height: 520px" trigger="none">
		<div class="tab-content">
			<template v-if="key==='应用'">
				<DarkMode/>
				<ThemeColorSelect/>
			</template>
			<template v-else-if="key==='外观'">
      <Appearance/>
			</template>
			<template v-else-if="key==='终端'">
      <Terminal/>
			</template>

		</div>
	</n-scrollbar>
	</div>

</template>

<script setup lang="jsx">
import {ref} from "vue";
import { Icon } from '@iconify/vue';
import DarkMode from '@/views/project/setting/components/DarkMode/index.vue';
import ThemeColorSelect from '@/views/project/setting/components/ThemeColorSelect/index.vue';
import Appearance from '@/views/project/setting/components/Appearance/index.vue';
import Terminal from '@/views/project/setting/components/Terminal/index.vue';

const buttons=ref([{title:'应用',icon:'ep:setting',active:true},{title:'外观',icon:'uil:swatchbook',active:false},{title:'终端',icon:'mingcute:terminal-box-line',active:false}])
const key=ref("应用");
const togglePay = (item) => {
	if (!item.active) {
		item.active = true;
		key.value = item.title;
		console.log(key.value)
		let value = buttons.value;
		value.forEach((v)=>{
			if (v.title !== item.title) {
				v.active=false;
			}
		})
	}
};




</script>
<style>

</style>

<style scoped>
.active{

	color: #4fadff !important;
	background-color: rgba(0,89,165,0.34902) !important;
}
.content{
	display: flex;
	min-height: 0;
	flex: 1 0 0;
}
.nav{
	padding: 20px 10px;
	width: 222px;
	flex: none;
	overflow-y: auto;
	flex-wrap: nowrap;
}
.tab-content{
	flex: auto;
	padding: 20px 30px;
	overflow-y: auto;
}

</style>
