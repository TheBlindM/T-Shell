import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/cmd');
}

export function getListById(params: { ids: string }) {
  return request.get('/listById', { params });
}
export function getSingle(id: number) {
	return request.get<any>(`/cmd/${id}`);
}

export function add(group: any) {
  return request.post('/cmd', group);
}
export function del(id: number) {
  return request.delete(`/cmd/${id}`);
}
export function upd(cmd: any, id: number) {
  return request.put(`/cmd/${id}`, cmd);
}

export function partUpd(cmd: any, id: number) {
  return request.patch(`/cmd/${id}`, cmd);
}

export function tree() {
  return request.get('/cmd/tree');
}

