<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="ko">
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Bootstrap demo</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" >
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="col-12">
				<h1>첨부파일 목록</h1>
				
				<table class="table">
					<colgroup>
						<col width="10%">
						<col width="15%">
						<col width="15%">
						<col width="30%">
						<col width="30%">
					</colgroup>
					<thead>
						<tr>
							<th>번호</th>
							<th>제목</th>
							<th>폴더</th>
							<th>파일명</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="file" items="${files }">
							<tr>
								<td>${file.id }</td>
								<td>${file.title }</td>
								<td>${file.folder }</td>
								<td>
									${file.filename }
									<a href="download?id=${file.id }" class="btn btn-outline-secondary btn-sm">다운로드</a>
								</td>
								<td>
									<img src="https://2404-images.s3.ap-northeast-2.amazonaws.com/${file.folder }/${file.filename}" class="img-thumbnail"/>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				
				<div class="text-end">
					<a href="form" class="btn btn-outline-primary btn-sm">등록</a>
				</div>
			</div>
		</div>
	</div>
	
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" ></script>
</body>
</html>