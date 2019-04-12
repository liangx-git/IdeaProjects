function ws(){
    //判断当前浏览器是否支持webSocket
    if (!window.WebSocket){
        alert("当前浏览器不支持webSocket");
        return;
    }
    var webSocketPath = "ws://localhost:8080/kafka/monitor";
    socket = new WebSocket(webSocketPath);

    //建立WebSocket连接后的回调函数
    socket.onopen = function(ev){
       //TODO
    }

    //收到服务器数据后的回调函数
    socket.onmessage = function(ev){
        var data = event.data;
        //TODO
    }

    //断开连接回调函数
    socket.onclose = function (ev) {
        //TODO
    }
}