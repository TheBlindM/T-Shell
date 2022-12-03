import { invoke } from '@tauri-apps/api/tauri'
import { WebSocketClient } from '@/utils';

export class MsgWebSocket {
  webSocket: WebSocketClient;

  events: any = {};

  constructor() {
    this.webSocket = new WebSocketClient('msg');

    const localEvents = this.events;
    this.webSocket.connect({
      onError(error) {
        // 连接失败回调
        window.console.log(`\r\nError: ${error}\r\n`);
      },
      onConnect() {
        window.console.log('onConnect');
      },
      onClose() {
        // 连接关闭回调
        window.console.log('\r\nconnection closed');
      },
      onData(data) {
        const acceptMsg: Msg = JSON.parse(data);
				// eslint-disable-next-line eqeqeq
				if(acceptMsg.msgType=='OPEN_FILE'){
					invoke('openFile', { path: acceptMsg.message })
				}else {
					localEvents[`${acceptMsg.channelId}-${acceptMsg.msgType}`](acceptMsg.message);
				}

      }
    });
  }

  addMonitor(channelId: any, msgType: any, event: (arg0: any) => void) {
    this.events[`${channelId}-${msgType}`] = event;
  }

  sendJsonMessage(message: any) {
    this.webSocket.sendJsonMessage(message);
  }
}

export class Msg {
  message: string | null;

  msgType: string;

  channelId: any;

  constructor(channelId: any, message: string, msgType: string) {
    this.message = message;
    this.msgType = msgType;
    this.channelId = channelId;
  }
}
export enum MessageType {
	CMD = 'CMD',
	RETRIEVE_CMD = 'RETRIEVE_CMD'
}

export const shellWebSocket = new MsgWebSocket();
