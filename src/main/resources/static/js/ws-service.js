class QuizWebSocketService {
    constructor() {
        this.socket = null;
        this.connected = false;
        this.queue = [];
        this.subscriptions = new Map();
    }

    connect() {
        if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
            return Promise.resolve();
        }

        const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
        this.socket = new WebSocket(`${protocol}://${window.location.host}/ws-game`);

        return new Promise((resolve, reject) => {
            this.socket.onopen = () => {
                this.socket.send('CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\0');
            };

            this.socket.onmessage = (event) => {
                const frame = this.parseFrame(event.data);
                if (frame.command === 'CONNECTED') {
                    this.connected = true;
                    this.queue.splice(0).forEach(item => this.socket.send(item));
                    resolve();
                    return;
                }
                if (frame.command === 'MESSAGE') {
                    const destination = frame.headers.destination;
                    const handler = this.subscriptions.get(destination);
                    if (handler) handler(JSON.parse(frame.body || 'null'));
                }
            };

            this.socket.onerror = reject;
            this.socket.onclose = () => {
                this.connected = false;
            };
        });
    }

    async subscribe(destination, handler) {
        await this.connect();
        this.subscriptions.set(destination, handler);
        this.sendFrame(`SUBSCRIBE\nid:${destination}\ndestination:${destination}\n\n\0`);
    }

    async publish(destination, payload) {
        await this.connect();
        this.sendFrame(`SEND\ndestination:${destination}\ncontent-type:application/json\n\n${JSON.stringify(payload)}\0`);
    }

    sendFrame(frame) {
        if (this.connected) this.socket.send(frame);
        else this.queue.push(frame);
    }

    parseFrame(raw) {
        const data = raw.replace(/\0$/, '');
        const [head, ...bodyParts] = data.split('\n\n');
        const lines = head.split('\n');
        const command = lines.shift();
        const headers = {};
        lines.forEach(line => {
            const index = line.indexOf(':');
            if (index > -1) headers[line.slice(0, index)] = line.slice(index + 1);
        });
        return { command, headers, body: bodyParts.join('\n\n') };
    }
}

window.quizWs = new QuizWebSocketService();
