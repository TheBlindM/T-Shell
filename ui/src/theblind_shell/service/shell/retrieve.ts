import { request } from '@/service/request';

/** 测试代理后的请求 */
export function retrieve(term: any, channelId: number | unknown, skipVerify: any) {
  return request.get<any>('/retrieve', { params: { term, channelId, skipVerify } });
}

export function retrieveCmd(term: any, currentCmdId: any, sessionId: any) {
  return request.get<any>('/retrieve/cmd', { params: { term, currentCmdId, sessionId } });
}

export function parseShortcutCmdPlaceholders(id: number, channelId: string | unknown) {
  return request.get<any>('/retrieve/parseShortcutCmdPlaceholders', { params: { id, channelId } });
}

export function getMatchItems(id: number, channelId: string | unknown) {
  return request.get<any>('/retrieve/matchItems', { params: { id, channelId } });
}

export function parseTemplate(id: number, channelId: string | unknown, items: any) {
  return request.post('/retrieve/parseTemplate', { id, channelId, items });
}
