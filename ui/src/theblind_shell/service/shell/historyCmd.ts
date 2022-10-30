import { request } from '@/service/request';

/** 测试代理后的请求 */
export function getPage(
  page: any,
  size: any,
  param: { sessionId: any; sessionType: any; startDate: any; endDate: any }
) {
  return request.post('/historyCmd/getPage', { page, size, param });
}
export function add(history: any) {
  return request.post(`/historyCmd`, history);
}
