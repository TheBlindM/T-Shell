import { EnumContentType } from '@/enum';
import { request } from '@/service/request';

/** 测试代理后的请求 */
export function get() {
  return request.get('/sshSession');
}
export function getSingle(id: number | unknown) {
  return request.get(`/sshSession/${id}`);
}

export function addGroup(group: any) {
  return request.post('/sessionGroup', group);
}
export function add(sshSession: any) {
  return request.post('/sshSession', sshSession);
}

export function upd(sshSession: any, sshSessionId: number) {
  return request.put(`/sshSession/${sshSessionId}`, sshSession);
}

export function updGroup(groupId: any, id: number) {
  return request.patch(
    `/sshSession/${id}/group`,
    { groupId },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}

export function getHostGroupId(sshSessionId: any) {
  return request.get(`/sshSession/sshSessionGroup/${sshSessionId}`);
}

export function del(sshSessionId: number) {
  return request.delete(`/sshSession/${sshSessionId}`);
}
