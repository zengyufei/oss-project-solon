<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="SpringBoot thymeleaf"/>
    <meta name="author" content="YiHui"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>扫码登录确认界面</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
    <script src="/jquery3.6.js"></script>
</head>
<body>
<div>
    <input type="button" value="确认登录" onclick="window.location.href='/api/login/confirm?id=${id}'">
    <input type="button" value="取消登录" onclick="window.location.href=window.location.href">
</div>
</body>
</html>
