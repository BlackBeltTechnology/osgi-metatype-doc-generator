<html>
<head>
    <title>Configuration parameters - ${project} - ${ocd.id}</title>
    <style>
        body {background-color: white; color: #3b4151;font-family: Open Sans,sans-serif;}
        table {text-align: left;}
        table td {padding-right: 10px;font-size: 14px;vertical-align: top;}
        table td.key {font-size: 16px;font-weight: 700;padding-right: 30px;}
        table td.key span.required {font-size: 10px;color:red;}
        table td.type {text-transform: lowercase;}
        table td.default {font-size: 14px;color: #89bf04;}
        table th {padding-bottom: 10px; font-size: 12px;text-align: left;}
        div.panel {border: 1px solid #61affe;padding: 10px;border-radius: 10px;background-color: #fafafa;display: inline-block;}
        div.panel .title {padding: 5px 5px 15px 5px; border-bottom: 1px solid #61affe;}
        div.panel .title span {font-size: 12px;}
        div.panel .params {padding: 20px 20px 20px 5px;}
    </style>
</head>
<body>
    <div class="panel">
        <div class="title">
            <h4>Configuration parameters - ${ocd.name}</h4>
            <span>OCD id: <b>${ocd.id}</b></span><br/>
            <span>Project: <b>${project}</b></span>
        </div>
        <div class="params">
            <table>
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>Type</th>
                        <th>Name</th>
                        <th>Default value</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                <#list ocd.ADS as ad>
                    <tr>
                        <td class="key">
                            ${ad.id}
                            <#if !(ad.required??) || ad.required==true>
                            <span class="required"> * required</span>
                            </#if>
                        </td>
                        <td class="type">${ad.type}</td>
                        <td>${ad.name}</td>
                        <td class="default">${ad.default!""}</td>
                        <td>${ad.description!"---n/a---"}</td>
                    </tr>
                </#list>
                </tbody>
             </table>
        </div>
    <div>
</body>
</html>
