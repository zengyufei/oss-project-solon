<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>授权回调页面</title>
  <script type="text/javascript">
    /**
     * 获取url参数
     * @param url
     * @param variable
     */
    function getQueryVariable(
      url,
      variable
    ) {
      // 对url进行截取
      url = url.substring(url.indexOf("?"), url.length);
      const query = url.substring(1);
      const vars = query.split("&");
      for (let i = 0; i < vars.length; i++) {
        const pair = vars[i].split("=");
        if (pair[0] === variable) {
          return pair[1];
        }
      }
      return -1;
    }
    window.onload = () => {
      // 获取url中的授权码
      const code = getQueryVariable(window.location.href, "code");
      // 将授权码放进本地存储中
      if (code !== -1) {
        localStorage.setItem("authCode", code);
      }else {
        localStorage.setItem("authCode", "");
      }
      // 关闭页面
      window.close();
    }
  </script>
</head>
<body>

</body>
</html>
