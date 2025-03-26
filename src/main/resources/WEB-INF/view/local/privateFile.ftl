<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
    <script src="/jquery3.6.js"></script>
    <script src="/copyText.js"></script>
    <style>
        .radio-inline {
            padding-left: 0;
            display: inline-block;
        }

        .zyf-table {
            padding-left: 0;
        }

        /* 隐藏鼠标点击这些元素时出现的光标 */
        div, span, p, table, tr, td, th {
            caret-color: transparent;
        }
    </style>
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

    <div>
        <div class="radio-inline">
            <span>排序字段</span>
        </div>
        <div class="radio-inline">
            <input type="radio" name="orderByField" id="orderByField1" value="1" checked/>
            <label for="orderByField1">大小</label>
        </div>
        <div class="radio-inline">
            <input type="radio" name="orderByField" id="orderByField2" value="0"/>
            <label for="orderByField2">上传日期</label>
        </div>
    </div>

    <div>
        <div class="radio-inline">
            <span>排序方式</span>
        </div>
        <div class="radio-inline">
            <input type="radio" name="orderByType" id="orderByType1" value="1" checked/>
            <label for="orderByType1">倒序</label>
        </div>
        <div class="radio-inline">
            <input type="radio" name="orderByType" id="orderByType2" value="0"/>
            <label for="orderByType2">正序</label>
        </div>
    </div>

    <div id="uploadPage" style="display: none;margin-top: 20px;">
        <form id="form" action="/api/upload" method="post">
            <div>
                <input id="file" name="file" type="file" value=""/>
            </div>
            <div style="margin-top: 20px;">
                <input id="submit" name="submit" type="button" value="提交"/>
            </div>
        </form>
        <div id="msg" style="margin-top: 20px;"></div>
    </div>
    <div id="downloadPage">
        <div id="fileList">
        </div>
        <div id="downloadMsg" style="margin-top: 20px;"></div>
    </div>
</div>
</body>

<script>

    function load(orderByField, orderByType) {
        $.get("/api/file/list?orderByField=" + orderByField + "&orderByType=" + orderByType, function (data) {
            // console.log(data)
            if (data.data) {
                var fileList = data.data
                var fileListDom = $("#fileList")
                fileListDom.empty()

                var tableDom = $('<table border="0" cellpadding="5" class="zyf-table"></table>')
                var titleDom = $('<tr></tr>')
                titleDom.append($('<th>文件大小</th>'))
                titleDom.append($('<th>上传日期</th>'))
                titleDom.append($('<th>文件名</th>'))
                titleDom.append($('<th>外链访问次数</th>'))
                titleDom.append($('<th>操作</th>'))
                tableDom.append(titleDom)
                for (var i = 0; i < fileList.length; i++) {
                    var file = fileList[i]

                    var trDom = $('<tr id="del_' + i + '"></tr>')

                    var sizeTdDom = $('<td></td>')
                    sizeTdDom.append('<span>' + file.sizeStr + '</span>')
                    trDom.append(sizeTdDom)

                    var dateTdDom = $('<td></td>')
                    dateTdDom.append('<span>' + file.lastModified + '</span>')
                    trDom.append(dateTdDom)

                    var urlTdDom = $('<td></td>')
                    urlTdDom.append('<a href="' + file.url + '&size=' + file.size + '">' + file.fileName + '.' +  file.extName + '</a>')
                    trDom.append(urlTdDom)

                    var wailianTdDom = $('<td></td>')
                    wailianTdDom.append('<span>' + file.visit + '</span>')
                    trDom.append(wailianTdDom)

                    var delTdDom = $('<td></td>')
                    delTdDom.append('<input type="button" style="margin-left: 10px;" value="删除" onclick="del(\'del_' + i + '\', \'' + file.fileId + '\');"/>')
                    trDom.append(delTdDom)


                    var shortTdDom = $('<td></td>')
                    shortTdDom.append('<input type="button" style="margin-left: 10px;" value="转短链" onclick="short(\'short_' + i + '\', \'' + file.url + '&size=' + file.size + '\');"/>')
                    trDom.append(shortTdDom)

                    var copyShortTdDom = $('<td></td>')
                    copyShortTdDom.append('<input type="button" style="margin-left: 10px;" value="复制短链" onclick="copy(\'short_' + i + '\');"/>')
                    trDom.append(copyShortTdDom)

                    if (file.shortUrl) {
                        var shortTextTdDom = $('<td></td>')
                        shortTextTdDom.append('<a id="short_' + i + '" href="'+file.shortUrl+'">'+file.shortUrl+'</a>')
                        trDom.append(shortTextTdDom)
                    } else {
                        var shortTextTdDom = $('<td></td>')
                        shortTextTdDom.append('<a id="short_' + i + '" style="display: none"></a>')
                        trDom.append(shortTextTdDom)
                    }

                    tableDom.append(trDom)
                }

                fileListDom.append(tableDom)
            } else {
                if (data.code === 4001014) {
                    window.location.href = "/view/login"
                } else {
                    $("#downloadMsg").text(data.description);
                }
            }
        })
    }

    $(document).ready(function () {
        var orderByField = 1
        var orderByType = 1
        load(orderByField, orderByType)

        $("#upload").click(function () {
            $("#uploadPage").show()
            $("#downloadPage").hide()
        })
        $("#download").click(function () {
            $("#uploadPage").hide()
            $("#downloadPage").show()
        })

        $("#submit").click(function () {
            $("#msg").text("");
            var form = new FormData(document.getElementById("form"));
            $.ajax({
                url: "/api/file/upload",
                type: "post",
                data: form,
                cache: false,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.data) {
                        $("#msg").text("上传成功");
                        $("#file").html('<input id="file" name="file" type="file" value=""/>');
                        load()
                        setTimeout(() => {
                            $("#download").click()
                        }, 1500)
                    } else {
                        $("#msg").text(data.description);
                    }
                },
                error: function (data) {
                    alert("上传失败")
                }
            })
        })
        $("#logout").click(function () {
            $.get("/api/logout", function (data) {
                if (data.data) {
                    window.location.href = "/view/login"
                } else {
                    $("#msg").text(data.description);
                }
            })
        })


        $("input:radio[name='orderByField']").change(function () {
            orderByField = $(this).val()
            load(orderByField, orderByType)
        })
        $("input:radio[name='orderByType']").change(function () {
            orderByType = $(this).val()
            load(orderByField, orderByType)
        })
    });

    function del(index, name) {
        if (confirm("确定删除?")) { //if语句内部判断确认框
            $.get("/api/file/del?fileName=" + name, function (data) {
                if (data.data) {
                    // console.log(index, $(index), data, data.data)
                    $("#" + index).remove()
                } else {
                    $("#msg").text(data.description);
                }
            })
        } else {
        }
    }

    function short(index, longUrl) {
        $.post("/api/add/shortUrl", {
            "longUrl": longUrl
        }, function (data) {
            if (data.data) {
                $("#" + index).show().attr("href", "http://"+data.data).html(data.data)
            } else {
                $("#msg").text(data.description);
            }
        })
    }

    function copy(index) {
        copyText($("#" + index).html())
    }


</script>
</html>
