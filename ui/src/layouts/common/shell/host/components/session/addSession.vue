<template>
	<div style="width: 100%; height: 100%">
		<n-form ref="formRef" :label-width="80" :model="formValue" :rules="rules">
			<n-form-item label="名称" path="sessionName">
				<n-input v-model:value="formValue.sessionName" placeholder="输入主机名称"/>
			</n-form-item>
			<n-form-item label="ip" path="ip">
				<n-input v-model:value="formValue.ip" placeholder="输入主机ip"/>
			</n-form-item>
			<n-form-item label="端口" path="port">
				<n-input-number v-model:value="formValue.port" placeholder="输入指定端口"/>
			</n-form-item>
			<n-form-item label="认证方式" path="authType">
				<n-select v-model:value="formValue.authType" :options="authTypeOptions" placeholder="选择认证方式"/>
			</n-form-item>

			<template v-if="formValue.authType=='PWD'">
				<n-form-item label="用户名" path="username">
					<n-input  v-model:value="formValue.username" placeholder="输入用户名"/>
				</n-form-item>
				<n-form-item label="密码" path="pwd">
					<n-input v-model:value="formValue.pwd" type="password" show-password-on="mousedown" placeholder="密码"/>
				</n-form-item>
			</template>

			<template  v-else-if="formValue.authType=='PUBLIC_KEY'">
				<n-form-item label="用户名" path="username">
					<n-input  v-model:value="formValue.username" placeholder="输入用户名"/>
				</n-form-item>
				<n-form-item label="私钥" >

						<n-input-group>
							<n-input :readonly="true" style="width: 100%" placeholder="文件路径" v-model:value="formValue.privateKeyFile" />
							<n-button @click="onClick"> 选择</n-button>
						</n-input-group>
				</n-form-item>
				<n-form-item label="passphrase">
					<n-input v-model:value="formValue.passphrase" type="password" show-password-on="mousedown" placeholder="密码"/>
				</n-form-item>
			</template>

			<n-form-item label="操作系统" path="ttyTypeId">
				<n-tree-select
					v-model:value="formValue.ttyTypeId"
					:options="options"
					label-field="name"
					key-field="id"
					children-field="children"
				/>
			</n-form-item>
		</n-form>
	</div>
	<n-space>
		<!--		<n-button @click="() => (showAddPageModal = false)">取消</n-button>-->
		<n-button type="info" :loading="formBtnLoading" @click="confirmForm">确定</n-button>
	</n-space>
</template>

<script setup lang="ts">
import {ref} from 'vue';
import {useMessage} from 'naive-ui';
import { open } from '@tauri-apps/api/dialog';
import {useAppStore} from '@/store';
import {add} from '@/theblind_shell/service/shell/host';
import {selectTree} from '@/theblind_shell/service/shell/ttyType';
import RequestResult = Service.RequestResult;

interface Props {
	/** hostGroupId */
	sessionGroupId: number;
}

const onClick = () => {
	open({
		title: 'Hello',
		message: 'World!',
		buttons: [
			{
				text: 'OK',
				primary: true,
				callback: () => console.log('OK button clicked')
			}
		]
	}).then(result => {
		// 处理用户选择的文件路径
		formValue.value.privateKeyFile=result;
	});
}



const app = useAppStore();
app.disabledDrawerMouseleave();

const formRef = ref<any>(null);
const message = useMessage();
const props = defineProps<Props>();
const emits = defineEmits(['changeShowModal', 'refreshTree']);

let formBtnLoading = false;

const confirmForm = (e: { preventDefault: () => void }) => {
	e.preventDefault();
	formBtnLoading = true;
	formRef.value?.validate((errors: any) => {
		if (!errors) {
			add(formRef.value.model).then((requestResult: RequestResult) => {
				if (requestResult.data != null) {
					app.enableDrawerMouseleave();
					emits('refreshTree');
					message.success('成功');
					emits('changeShowModal', false);
				}
			});
		} else {
			message.error('请填写完整信息');
		}
		formBtnLoading = false;
	});
};

const formValue = ref({
	sessionGroupId: props.sessionGroupId,
	sessionName: null,
	username: null,
	ip: null,
	port: 22,
	pwd: null,
	ttyTypeId: 4,
	authType: 'PWD',
	passphrase:null,
	privateKeyFile:null
});
const rules = {
	sessionName: {
		required: true,
		message: '输入主机名称',
		trigger: 'blur'
	},
	ip: {
		required: true,
		message: '输入主机ip',
		trigger: ['input', 'blur']
	},
	port: {
		type: 'number',
		required: true,
		trigger: ['blur', 'input'],
		message: '请输入端口'
	},
	ttyTypeId: {
		required: true,
		message: '请选择操作系统',
		validator(row: any, value: string) {
			if (value == null) return new Error('请选择项');
			return true;
		}
	}
};
const options = ref([]);

const initTtyTypeTree = () => {
	selectTree().then((requestResult: RequestResult) => {
		if (requestResult.data != null) {
			options.value = requestResult.data;
		}
	});
};
initTtyTypeTree();
const authTypeOptions = [
	{
		label: "密码",
		value: 'PWD'
	},
	{
		label: "公私钥",
		value: 'PUBLIC_KEY'
	},
	{
		label: "键盘交互",
		value: 'KEYBOARD_INTERACTIVE'
	}
];
</script>
