<template>
  <n-divider title-placement="center">外观</n-divider>
  <n-form :model="formParams">
    <n-form-item label="字体">
      <n-input-group>
        <n-select v-model:value="formParams.fontFamily" filterable :options="fontFamilyItems" @change="handleChange" />
        <n-input-number v-model:value="formParams.fontSize" placeholder="size" @change="handleChange" />
      </n-input-group>
    </n-form-item>
    <n-form-item label="正常字重">
      <n-input v-model:value="formParams.fontWeight" @change="handleChange" />
    </n-form-item>
    <n-form-item label="粗体字重">
      <n-input v-model:value="formParams.fontWeightBold" @change="handleChange" />
    </n-form-item>
    <n-form-item label="启用连结字">
      <n-switch v-model:value="formParams.enableFontLigatures" @change="handleChange" />
    </n-form-item>
    <n-form-item label="光标样式">
      <n-radio-group v-model:value="formParams.cursorShape" @change="handleChange">
        <n-radio-button value="block" label="█" />
        <n-radio-button value="bar" label="|" />
        <n-radio-button value="underline" label="▁" />
      </n-radio-group>
    </n-form-item>
    <n-form-item label="光标闪烁">
      <n-switch v-model:value="formParams.cursorBlink" @change="handleChange" />
    </n-form-item>
  </n-form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { getAppearance, updAppearance } from '@/theblind_shell/service/shell/config';

const fontFamily = [
  'Arial',
  'Arial Black',
  'Bahnschrift Light',
  'Bahnschrift Light Condensed',
  'Bahnschrift Light SemiCondensed',
  'Bahnschrift SemiLight',
  'Bahnschrift SemiLight Condensed',
  'Bahnschrift SemiLight SemiCondensed',
  'Bahnschrift',
  'Bahnschrift Condensed',
  'Bahnschrift SemiCondensed',
  'Bahnschrift SemiBold',
  'Bahnschrift SemiBold Condensed',
  'Bahnschrift SemiBold SemiCondensed',
  'Calibri Light',
  'Calibri',
  'Cambria',
  'Cambria Math',
  'Candara Light',
  'Candara',
  'Comic Sans MS',
  'Consolas',
  'Constantia',
  'Corbel Light',
  'Corbel',
  'Courier New',
  'Ebrima',
  'Franklin Gothic Medium',
  'Gabriola',
  'Gadugi',
  'Georgia',
  'HoloLens MDL2 Assets',
  'Impact',
  'Ink Free',
  'Javanese Text',
  'Leelawadee UI Semilight',
  'Leelawadee UI',
  'Lucida Console',
  'Lucida Sans Unicode',
  'Malgun Gothic Semilight',
  'Malgun Gothic',
  'Microsoft Himalaya',
  'Microsoft JhengHei Light',
  'Microsoft JhengHei',
  'Microsoft JhengHei UI Light',
  'Microsoft JhengHei UI',
  'Microsoft New Tai Lue',
  'Microsoft PhagsPa',
  'Microsoft Sans Serif',
  'Microsoft Tai Le',
  '微软雅黑 Light',
  '微软雅黑',
  'Microsoft YaHei UI Light',
  'Microsoft YaHei UI',
  'Microsoft Yi Baiti',
  'MingLiU-ExtB',
  'PMingLiU-ExtB',
  'MingLiU_HKSCS-ExtB',
  'Mongolian Baiti',
  'MS Gothic',
  'MS UI Gothic',
  'MS PGothic',
  'MV Boli',
  'Myanmar Text',
  'Nirmala UI Semilight',
  'Nirmala UI',
  'Palatino Linotype',
  'Segoe MDL2 Assets',
  'Segoe Print',
  'Segoe Script',
  'Segoe UI Light',
  'Segoe UI Semilight',
  'Segoe UI',
  'Segoe UI Semibold',
  'Segoe UI Black',
  'Segoe UI Emoji',
  'Segoe UI Historic',
  'Segoe UI Symbol',
  '宋体',
  '新宋体',
  'SimSun-ExtB',
  'Sitka Small',
  'Sitka Text',
  'Sitka Subheading',
  'Sitka Heading',
  'Sitka Display',
  'Sitka Banner',
  'Sylfaen',
  'Symbol',
  'Tahoma',
  'Times New Roman',
  'Trebuchet MS',
  'Verdana',
  'Webdings',
  'Wingdings',
  'Yu Gothic Light',
  'Yu Gothic',
  'Yu Gothic Medium',
  'Yu Gothic UI Light',
  'Yu Gothic UI Semilight',
  'Yu Gothic UI',
  'Yu Gothic UI Semibold',
  '等线 Light',
  '等线',
  '仿宋',
  '楷体',
  '黑体',
  'DejaVu Math TeX Gyre',
  'MT Extra',
  'Marlett'
];

const fontFamilyItems = computed(() => {
  return fontFamily.map(value => {
    return { label: value, value };
  });
});

// const defaultValueRef = () => ({
// 	fontFamily: undefined,
// 	fontSize: undefined,
// 	fontWeight: undefined,
// 	enableFontLigatures: undefined,
// 	cursorShape: undefined,
// 	cursorBlink: undefined,
// 	fontWeightBold: undefined,
// });
const formParams = ref({
  fontFamily: undefined,
  fontSize: undefined,
  fontWeight: undefined,
  enableFontLigatures: undefined,
  cursorShape: undefined,
  cursorBlink: undefined,
  fontWeightBold: undefined
});

const handleChange = (value) => {
	setTimeout(()=>{
		updAppearance(formParams.value);
	},100);
 return value;
};

const init = () => {
  getAppearance().then(resultData => {
    if (resultData.data != null) {
      formParams.value = resultData.data;
    }
  });
};
init();
</script>
<style scoped></style>
