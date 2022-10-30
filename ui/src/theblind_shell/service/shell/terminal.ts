import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/host');
}

export function sshInitConnect(
  sessionId: any,
  channelId: string,
  columns: number,
  lines: number,
  width: number,
  height: number
) {
  return request.post('/terminal/sshInitConnect', {
    sessionId,
    channelId,
    ttySize: { columns, lines, width, height }
  });
}

export function localInitConnect(channelId: any, columns: any, lines: any, width: any, height: any) {
  return request.post('/terminal/localInitConnect', { channelId, ttySize: { columns, lines, width, height } });
}

export function close(channelId: any) {
  return request.delete(`/terminal/close/${channelId}`);
}

export function resize(channelId: any, columns: any, lines: any, width: any, height: any) {
  return request.put(`/terminal/resize/${channelId}`, { channelId, columns, lines, width, height });
}
export function isInCommandInput(channelId: string) {
  return request.post(`/terminal/isInCommandInput/${channelId}`, { channelId });
}

export function getFileInfos(channelId: string, path1: string | null) {
  return request.get<any>(`/terminal/fileInfos/${channelId}`, { params: { path: path1 } });
}

export function getSessionCount(channelId: string) {
  return request.get<any>(`/terminal/sessionCount/${channelId}`);
}
