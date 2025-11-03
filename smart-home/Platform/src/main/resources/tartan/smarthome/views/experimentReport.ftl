<!-- Experiment Report Table: AB Testing Assignment -->
<html>
<head>
    <title>Experiment Report - Tartan Smart Home</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f2f2f2; }
    </style>
</head>
<body>
<h2>Experiment Report (AB Testing)</h2>
<p>Experiment Group set through config file: <b>${configPath}</b></p>
<table>
    <thead>
        <tr>
            <th>House Name</th>
            <th>Address</th>
            <th>Group</th>
            <th>Last Week Usage (min)</th>
            <th>This Week Usage (min)</th>
            <th>Change (min)</th>
            <th>Last Week Cost (CAD)</th>
            <th>This Week Cost (CAD)</th>
            <th>Change (CAD)</th>
        </tr>
    </thead>
    <tbody>
    <#list homes as home>
        <#assign weekKeys = home.weeklyLightsOnUsage?keys>
        <#assign lastWeek = weekKeys[weekKeys?size - 1]>
        <#assign lastValue = home.weeklyLightsOnUsage[lastWeek]>
        <#assign secondLastWeek = weekKeys[weekKeys?size - 2]>
        <#assign secondLastValue = home.weeklyLightsOnUsage[secondLastWeek]>
        <#assign changeMin = (lastValue - secondLastValue) / (60*1000)>
        <#assign lastCost = (lastValue/(60*1000)) * 0.05>
        <#assign secondLastCost = (secondLastValue/(60*1000)) * 0.05>
        <#assign changeCost = lastCost - secondLastCost>
        <tr>
            <td>${home.name}</td>
            <td>${home.address}</td>
            <td>${home.groupExperiment}</td>
            <td>${(secondLastValue/(60*1000))?string["0.##"]}</td>
            <td>${(lastValue/(60*1000))?string["0.##"]}</td>
            <td>${(changeMin)?string["0.##"]}</td>
            <td>${(secondLastCost)?string["0.##"]}</td>
            <td>${(lastCost)?string["0.##"]}</td>
            <td>${(changeCost)?string["0.##"]}</td>
        </tr>
    </#list>
    </tbody>
</table>
</body>
</html>