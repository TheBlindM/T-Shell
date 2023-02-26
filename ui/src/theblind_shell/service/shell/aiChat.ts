import { request } from '@/service/request';

export function getAiChat(question: any) {
  return request.get<any[]>('/ai/chat', { params:{question} });
}
