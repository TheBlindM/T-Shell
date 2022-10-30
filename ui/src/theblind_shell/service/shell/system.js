import { request } from '@/service/request';

export function shutdown() {
  return request.post('/system/shutdown');
}
