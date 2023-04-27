export function getUrlParams(url:string) {
	// 通过 ? 分割获取后面的参数字符串
	const urlStr = url.split('?')[1];
	// 创建空对象存储参数
	const obj:any = {};
	// 再通过 & 将每一个参数单独分割出来
	const paramsArr = urlStr.split('&');
	for (let i = 0, len = paramsArr.length; i < len; i++) {
		// 再通过 = 将每一个参数分割为 key:value 的形式
		const arr = paramsArr[i].split('=');
		// eslint-disable-next-line prefer-destructuring
		obj[arr[0]] = arr[1];
	}
	return obj;
}
