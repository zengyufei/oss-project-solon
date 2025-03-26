<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="SpringBoot thymeleaf"/>
    <meta name="author" content="YiHui"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>二维码界面</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
    <script src="/jquery3.6.js"></script>
</head>
<body>
<div>
    <div class="title">请扫码登录</div>
    <img src="${qrcode}"/>
    <div id="state" style="display: none"></div>

    <script th:inline="javascript">
        let socket = new WebSocket("${subscribe}");

        socket.onopen = function (e) {
            console.log("[open] Connection webscoket");
            socket.send("My name is John");
        };

        socket.onmessage = function (event) {
            console.log(`[message] Data received from server: ${r"${event.data}"}`);
            if (event.data === "1") {
                console.log("扫码成功!")
                var stateTag = document.getElementById('state');
                stateTag.innerText = '已扫码';
                stateTag.style.display = 'block';
            } else if (event.data === "2") {
                console.log("登录成功!")
                var stateTag = document.getElementById('state');
                stateTag.innerText = '登录成功,即将跳转!';
                stateTag.style.display = 'block';
                setTimeout(()=> {
                    window.location.href = "/view/login"
                }, 3000)
            }
        };

        socket.onclose = function (event) {
            if (event.wasClean) {
                console.log(`[close] Connection closed cleanly, code=${r"${event.code}"} reason=${r"${event.reason}"}`);
            } else {
                // 例如服务器进程被杀死或网络中断
                // 在这种情况下，event.code 通常为 1006
                console.log('[close] Connection died');
            }
        };

        socket.onerror = function (error) {
            console.log(`[error] ${r"${error.message}"}`);
        };
    </script>
</div>
</body>
</html>
