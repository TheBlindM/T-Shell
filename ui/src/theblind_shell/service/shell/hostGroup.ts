import { EnumContentType } from '@/enum';
import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/host');
}
export function getSingle(id: number) {
  return request.get(`/sessionGroup/${id}`);
}

export function add(group: any) {
  return request.post('/sessionGroup', group);
}
export function del(id: number) {
  return request.delete(`/sessionGroup/${id}`);
}
export function upd(hostGroup: any, id: number) {
  return request.put(`/sessionGroup/${id}`, hostGroup);
}

export function updGroup(groupId: any, id: number) {
  return request.patch(
    `/sessionGroup/${id}/group`,
    { groupId },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}

export function tree() {
  return request.get('/sessionGroup/tree');
}
