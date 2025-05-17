<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Home</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="card">
            <div class="card-header">
                <h3>Welcome ${user.username}!</h3>
            </div>
            <div class="card-body">
                <p>You are successfully logged in.</p>
                <p>Your email: ${user.email}</p>
                <a href="upload" class="btn btn-danger">Upload</a>
                <a href="logout" class="btn btn-danger">Logout</a>
            </div>
        </div>
    </div>
</body>
</html>