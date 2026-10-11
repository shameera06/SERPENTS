$wc = New-Object System.Net.WebClient
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "=========================================="
Write-Host "1. Testing Staff Login..."
Write-Host "=========================================="
$loginRes = Invoke-WebRequest -Uri "http://localhost:8000/admin/login" -Method POST -Body @{
    email = "staff@example.com"
    password = "ChangeMe123!"
} -SessionVariable "staffSession" -MaximumRedirection 0 -ErrorAction SilentlyContinue -UseBasicParsing

Write-Host "Staff Login Response Status:" $loginRes.StatusCode
Write-Host "Redirect Location:" $loginRes.Headers["Location"]

Write-Host "`n=========================================="
Write-Host "2. Testing AI Chatbot for Reptile #1 (Blacky - King Snake)..."
Write-Host "=========================================="
$chatBody = '{"message":"What temperature and humidity does Blacky need?"}'
$chatRes = Invoke-WebRequest -Uri "http://localhost:8000/admin/reptiles/1/chat" -Method POST -Body $chatBody -ContentType "application/json" -WebSession $staffSession -UseBasicParsing

Write-Host "Chat Response Status:" $chatRes.StatusCode
Write-Host "Chat Output:" $chatRes.Content

Write-Host "`n=========================================="
Write-Host "3. Testing AI Chatbot for Reptile #9 (Spider - Python Snake with weight alert)..."
Write-Host "=========================================="
$chatBody2 = '{"message":"Analyze Spider weight and health alert"}'
$chatRes2 = Invoke-WebRequest -Uri "http://localhost:8000/admin/reptiles/9/chat" -Method POST -Body $chatBody2 -ContentType "application/json" -WebSession $staffSession -UseBasicParsing

Write-Host "Chat Response Status:" $chatRes2.StatusCode
Write-Host "Chat Output:" $chatRes2.Content

Write-Host "`n=========================================="
Write-Host "4. Testing PDF Download for Reptile #1..."
Write-Host "=========================================="
$pdfRes = Invoke-WebRequest -Uri "http://localhost:8000/admin/reptiles/1/pdf/download" -Method GET -WebSession $staffSession -UseBasicParsing

Write-Host "PDF Download Status:" $pdfRes.StatusCode
Write-Host "Content-Type:" $pdfRes.Headers["Content-Type"]
Write-Host "Content-Disposition:" $pdfRes.Headers["Content-Disposition"]
Write-Host "PDF Bytes Count:" $pdfRes.RawContentLength

Write-Host "`n=========================================="
Write-Host "5. Testing CNN Disease Prediction Image Upload..."
Write-Host "=========================================="
# Prepare sample image upload
$imgPath = "src\main\resources\static\assets\img\elements\1.jpg"
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"

$bodyLines = (
    "--$boundary",
    "Content-Disposition: form-data; name=`"imageFile`"; filename=`"respiratory_infection_test.jpg`"",
    "Content-Type: image/jpeg$LF",
    [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString([System.IO.File]::ReadAllBytes($imgPath)),
    "--$boundary",
    "Content-Disposition: form-data; name=`"speciesHint`"$LF",
    "Python",
    "--$boundary",
    "Content-Disposition: form-data; name=`"userNotes`"$LF",
    "Excessive bubbling mucus around glottis with audible wheezing and stargazing posture",
    "--$boundary--$LF"
) -join $LF

$uploadRes = Invoke-WebRequest -Uri "http://localhost:8000/admin/disease-predictions" -Method POST -ContentType "multipart/form-data; boundary=$boundary" -Body ([System.Text.Encoding]::GetEncoding("iso-8859-1").GetBytes($bodyLines)) -WebSession $staffSession -MaximumRedirection 0 -ErrorAction SilentlyContinue -UseBasicParsing

Write-Host "Prediction Upload Status:" $uploadRes.StatusCode
Write-Host "Redirect Location (Prediction Details):" $uploadRes.Headers["Location"]

if ($uploadRes.Headers["Location"]) {
    $predDetails = Invoke-WebRequest -Uri ("http://localhost:8000" + $uploadRes.Headers["Location"]) -Method GET -WebSession $staffSession -UseBasicParsing
    Write-Host "Prediction Details Page Status:" $predDetails.StatusCode
}

Write-Host "`nALL TESTS COMPLETED!"
