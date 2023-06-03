import { request } from '@/service/request';

export function getAppearance() {
  return request.get<any[]>('/config/appearance');
}

export function updAppearance(appearance: any) {
	return request.put(`/config/appearance`, appearance);
}

export function getConfig() {
	return request.get<any[]>('/config');
}

export function getTerminal() {
	return request.get<any[]>('/config/terminal');
}

export function updTerminal(appearance: any) {
	return request.put(`/config/terminal`, appearance);
}
