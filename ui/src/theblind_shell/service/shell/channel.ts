import {EnumContentType} from "@/enum";
import {request} from '@/service/request';


export function bindChannel(syncChannelId,sshChannelId) {
  return request.post('/syncChannel/bind', { syncChannelId,sshChannelId },{ headers: { 'Content-Type': EnumContentType.formUrlencoded } });
}

export function removeBindChannel(sshChannelId) {
	return request.post('/syncChannel/removeBind',{sshChannelId},{ headers: { 'Content-Type': EnumContentType.formUrlencoded } });
}
export function getChannelId(sshChannelId) {
	return request.get('/syncChannel',{params:{sshChannelId}});
}
