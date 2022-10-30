import { request } from '@/service/request';

export function getListById(params: any) {
  return request.get<any[]>('/ttyType/listById', { params });
}
export function getSingle(id: number) {
  return request.get<any>(`/ttyType/${id}`);
}

export function selectTree() {
  return request.get('/ttyType/selectTree');
}
