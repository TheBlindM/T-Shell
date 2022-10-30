import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/shortcutCmdGroup');
}
export function getSingle(id: number) {
  return request.get(`/shortcutCmdGroup/${id}`);
}

export function add(group: any) {
  return request.post('/shortcutCmdGroup', group);
}
export function del(id: number) {
  return request.delete(`/shortcutCmdGroup/${id}`);
}
export function upd(hostGroup: any, id: number) {
  return request.put(`/shortcutCmdGroup/${id}`, hostGroup);
}

export function tree() {
  return request.get('/shortcutCmdGroup/tree');
}
export function parentTree() {
  return request.get<any>('/shortcutCmdGroup/parentTree');
}

export function partUpd() {
  return request.get<any>('/shortcutCmdGroup/parentTree');
}
