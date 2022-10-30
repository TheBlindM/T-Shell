import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/globalVariable');
}
export function getSingle(id: number) {
  return request.get(`/globalVariable/${id}`);
}

export function add(globalVariable: any) {
  return request.post('/globalVariable', globalVariable);
}
export function del(id: number) {
  return request.delete(`/globalVariable/${id}`);
}
export function upd(globalVariable: any, id: number) {
  return request.put(`/globalVariable/${id}`, globalVariable);
}

/** 测试代理后的请求 */
export function page(query: any) {
  const params = { pageSize: query.pageSize, page: query.page, name: query.name };
  return request.get('/globalVariable/page', { params });
}
