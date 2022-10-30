import { request } from '@/service/request';

export function topList() {
  return request.get<any>('/connectionLog/topList');
}
