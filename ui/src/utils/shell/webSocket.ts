export class WebSocketClient {
  path: string;

  socket: WebSocket | undefined;

  constructor(path: string) {
    this.path = path;
  }

  // eslint-disable-next-line class-methods-use-this
  getWebSocketUrl() {
    const protocol = 'ws://';
    /*    if (window.location.protocol === 'https:') {
      protocol = 'wss://';
    } */
    return `${protocol}/127.0.0.1:10218/${this.path}`;
  }

  connect(params: {
    onError: (arg0: string) => void;
    onConnect: () => void;
    onData: (arg0: any) => void;
    onClose: () => void;
  }) {
    this.socket = new WebSocket(this.getWebSocketUrl());

    this.socket.onopen = () => {
      params.onConnect();
    };

    this.socket.onmessage = evt => {
      const data = evt.data.toString();
      params.onData(data);
    };

    this.socket.onclose = () => {
      params.onClose();
    };
  }

  sendMessage(message: string) {
    this.socket?.send(message);
  }

  sendJsonMessage(message: any) {
    this.sendMessage(JSON.stringify(message));
  }
}
