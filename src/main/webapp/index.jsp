<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>rmall商城</title>
</head>
<body>
<h1>欢迎来到rmall商城..</h1>
<h2>Tomcat 1!服务器界面</h2>
<br/>
<h2>Tomcat 1!服务器界面</h2>
<hr/>
<br/>
    <div>
        <h3>上传一个文件</h3>
        <form action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
            <input type="file" name="upload_file" />
            <input type="submit" value="上传" />
        </form>
    </div>
    <hr>
    <div>
        <h3>富文本的上传</h3>
        <form action="/manage/product/richTextUpload.do" method="post" enctype="multipart/form-data">
            <input type="file" name="upload_file" />
            <input type="submit" value="上传" />
        </form>
    </div>
</body>
</html>
