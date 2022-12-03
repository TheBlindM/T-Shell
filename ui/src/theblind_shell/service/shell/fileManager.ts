import { EnumContentType } from '@/enum';
import { request } from '@/service/request';

export function getFileInfos(channelId: string, path: string | null) {
  return request.get<any>(`/fileManager/fileInfos/${channelId}`, { params: { path } });
}

/* export function upload(channelId: any, path: any, name, file: any) {
  return request.post(
    `/fileManager/upload/${channelId}`,
    { path, file, name },
    { headers: { 'Content-Type': EnumContentType.formData } }
  );
} */
export function upload(channelId: any, path: any, filePaths: any) {
  return request.post(`/fileManager/upload/${channelId}`, { path, filePaths });
}

export function create(channelId: any, path: any, name: any, type: any) {
  return request.post(`/fileManager/create/${channelId}`, { path, name, type });
}

export function removeFile(channelId: any, path: string) {
  return request.post(
    `/fileManager/removeFile/${channelId}`,
    { path },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}
export function removeDirectory(channelId: any, path: string) {
  return request.post(
    `/fileManager/removeDirectory/${channelId}`,
    { path },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}
export function download(channelId: any, path: any) {
  return request.post(
    `/fileManager/download/${channelId}`,
    { path },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}

export function rename(channelId: any, oldPath: any, fileName: any) {
  return request.put(
    `/fileManager/rename/${channelId}`,
    { oldPath, fileName },
    { headers: { 'Content-Type': EnumContentType.formUrlencoded } }
  );
}

export function pauseTransfer(channelId: any, transferRecordId: any) {
  return request.post(`/fileManager/pauseTransfer/${channelId}/${transferRecordId}`);
}
export function continueTransfer(channelId: any, transferRecordId: any) {
  return request.post(`/fileManager/continueTransfer/${channelId}/${transferRecordId}`);
}

export function getCompleteList(channelId: string) {
  return request.get<any>(`/fileManager/completeList/${channelId}`);
}

export function getDownloadList(channelId: string) {
  return request.get<any>(`/fileManager/downloadList/${channelId}`);
}
export function getUploadList(channelId: string) {
  return request.get<any>(`/fileManager/uploadList/${channelId}`);
}
export function deleteRecord(transferRecordId: string) {
  return request.delete<any>(`/fileManager/record/${transferRecordId}`);
}

export function openFile(channelId: string, path: string) {
  return request.get<any>(`/fileManager/openFile/${channelId}`, { params: {path} });
}

export function cancelOpenFile(taskId: string) {
	return request.delete<any>(`/fileManager/openFile`,{ params: {taskId} });
}
