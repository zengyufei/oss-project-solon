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
    <div>
        <input id="upload" type="button" value="上传">
        <input id="download" type="button" value="下载">
        <input id="logout" type="button" style="float: right" value="退出登录">
    </div>
    <div id="uploadPage" style="display: none;">
        <form id="form" action="/api/upload" method="post" >
            <div>
                <input id="file" name="file" type="file" value=""/>
            </div>
            <div>
                <input id="submit" name="submit" type="button" value="提交"/>
            </div>
        </form>
        <div id="msg"></div>
    </div>
    <div id="downloadPage">
        <ul id="fileList">
        </ul>
    </div>
</div>
</body>

<script>
    $(document).ready(function() {
       $.get("/api/jar/list", function(data){
            // console.log(data)
            if (data.data) {
                var fileList = data.data
                for (var i=0;i<fileList.length;i++) {
                    var file = fileList[i]


                    var dom = $('<li id="del_'+i+'"></li>')
                    dom.append('<a href="'+file.url+'">'+file.fileName+'</a>')
                    dom.append('<input type="button" value="删除" onclick="del(\'del_'+i+'\', \''+file.fileName+'\');"/>')
                    $("#fileList").append(dom)
                }
            } else {
                $("#msg").text(data.description);
            }
       })

       $("#upload").click(function() {
            $("#uploadPage").show()
            $("#downloadPage").hide()
       })
       $("#download").click(function() {
            $("#uploadPage").hide()
            $("#downloadPage").show()
       })

       $("#submit").click(function() {
            $("#msg").text("");
            var form = new FormData(document.getElementById("form"));
            $.ajax({
                url:"/api/file/upload",
                type:"post",
                data:form,
                cache: false,
                processData: false,
                contentType: false,
                success:function(data){
                    if (data.data) {
                        $("#msg").text("上传成功");
                        $("#file").html('<input id="file" name="file" type="file" value=""/>');
                    } else {
                        $("#msg").text(data.description);
                    }
                },
                error:function(data){
                    alert("上传失败")
                }
            })
       })
       $("#logout").click(function() {
             $.get("/api/logout", function(data){
                if (data.data) {
                    window.location.href= "/view/login"
                } else {
                    $("#msg").text(data.description);
                }
            })
       })
    });

    function del(index, name) {
        $.get("/api/file/del?fileName=" + name, function(data){
            if (data.data) {
                // console.log(index, $(index), data, data.data)
                $("#" + index).remove()
            } else {
                $("#msg").text(data.description);
            }
        })
    }


</script>
</html>
