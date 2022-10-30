import { WebSocketClient } from '@/utils';

export class MsgWebSocket {
  webSocket: WebSocketClient;

  terms: any = {};

  events: any = {};

  constructor() {
    this.webSocket = new WebSocketClient('msg');
    const terms1 = this.terms;
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
        debugger;
        console.log(acceptMsg);
        console.log(localEvents);
        localEvents[`${acceptMsg.channelId}-${acceptMsg.msgType}`](acceptMsg.message);
        /* switch (acceptShellMessage.messageType) {
          case 'CMD':
            terms1[acceptShellMessage.channelId].write(acceptShellMessage.message);
            break;
          // eslint-disable-next-line no-fallthrough
          case 'RETRIEVE_CMD':
            terms1[acceptShellMessage.channelId].retrieve(JSON.parse(acceptShellMessage.message));
            break;
          default:
        } */
      }
    });
  }

  connect(channelId: any, term: any) {
    this.terms[channelId] = term;
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

export class ShellMessage {
  channelId: string | unknown;

  message: string | null;

  messageType: string;

  constructor(channelId: string | unknown, message: string, messageType: string) {
    this.channelId = channelId;
    this.message = message;
    this.messageType = messageType;
  }
}

export enum MessageType {
  CMD = 'CMD',
  RETRIEVE_CMD = 'RETRIEVE_CMD'
}
export const shellWebSocket = new MsgWebSocket();
