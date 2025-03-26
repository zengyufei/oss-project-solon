<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
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
    <table border="0" cellpadding="5" class="zyf-table">
        <tr>
            <th>文件大小</th>
            <th>上传日期</th>
            <th>文件名</th>
        </tr>

        <#list list as item>
            <tr>
                <td><span>${item.sizeStr}</span></td>
                <td><span>${item.lastModified}</span></td>
                <td><a href="${item.url}&size=${item.size}">${item.fileName}.${item.extName}</a></td>
            </tr>
        </#list>
    </table>
</div>
</body>
</html>
