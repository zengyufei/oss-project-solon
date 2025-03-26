<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" href="data:image/ico;base64,aWNv">
</head>
<body>
<div>
    <h1>${title}</h1>
</div>
<div>
    <ul>
<#list list as item>
        <li>
            <a href="${item.url}">${item.name}</a>
        </li>
</#list>
    </ul>
</div>
</body>
</html>
