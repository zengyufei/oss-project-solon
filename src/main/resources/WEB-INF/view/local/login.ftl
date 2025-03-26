<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
    <script src="/jquery3.6.js"></script>
</head>
<body>
<div>
    <h1>${title}</h1>
</div>
<div>
    <form id="form" action="/api/login" method="post" >
        <div>
            <span>账户:</span>
            <input id="username" name="username" type="text" value=""/>
        </div>
        <div>
            <span>密码:</span>
            <input id="password" name="password" type="password" value=""/>
        </div>
        <div>
            <input id="submit" name="submit" type="button" value="提交"/>
        </div>
    </form>
    <input id="github" name="github" type="button" value="github登录"/>
    <div>
        <span id="errorMsg" style="display: none;"></span>
    </div>
</div>
</body>
<script>
    $(document).ready(function() {
        $("#submit").click(function() {
            $("#errorMsg").html("");
            $("form").submit();
            $.ajax({
                type: "POST",   //提交的方法
                url:"/api/login", //提交的地址
                data: $('#form').serialize(),// 序列化表单值
                async: false,
                error: function(request) {  //失败的话
                    alert("Connection error");
                },
                success: function(data) {  //成功
                    console.log(data)
                    if (data.code === 200) {
                        let valData = data.data;
                        if (valData) {
                            console.log(valData)
                            var tokenName = valData.tokenName
                            var tokenValue = valData.tokenValue
                            localStorage.setItem("tokenName", tokenName);
                            localStorage.setItem("tokenValue", tokenValue);
                            if (valData.isLogin) {
                                window.location.href= "/view/private/file/list"
                            }
                        } else {
                            $("#errorMsg").show();
                            $("#errorMsg").html(data.description);
                        }
                    } else {
                            $("#errorMsg").show();
                            $("#errorMsg").html(data.description);
                    }

                }
            });
        });


        $("#github").click(function() {
            console.log("test");
            const url = "https://github.com/login/oauth/authorize?client_id=7916a4aeac302bc69489&allow_signup=true&scope="
            window.location.href = url // 直接跳转
        });
    });
</script>
</html>
