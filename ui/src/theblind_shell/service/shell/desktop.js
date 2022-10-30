import { request } from '@/service/request';

export function toGithub() {
  return request.post('/desktop/toGithub');
}
