import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/shortcutCmd');
}
export function getSingle(id: number) {
  return request.get(`/shortcutCmd/${id}`);
}

export function add(shortcutCmd: any) {
  return request.post('/shortcutCmd', shortcutCmd);
}
export function del( id: number) {
  return request.delete(`/shortcutCmd/${id}`);
}
export function upd(shortcutCmd: any, id: number) {
  return request.put(`/shortcutCmd/${id}`, shortcutCmd);
}

export function partUpd(shortcutCmd: any, id: number) {
  return request.put(`/shortcutCmd/${id}`, shortcutCmd);
}


