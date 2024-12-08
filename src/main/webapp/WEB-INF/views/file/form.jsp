<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
				<h1>첨부파일 업로드 폼</h1>
				
				<form class="border bg-light p-3" method="post" action="save" enctype="multipart/form-data">
					<div class="form-group mb-3">
						<label class="form-label">제목</label>
						<input type="text" class="form-control" name="title" />
					</div>
					<div class="form-group mb-3">
						<label class="form-label">설명</label>
						<textarea class="form-control" name="description" rows="3"></textarea>
					</div>
					<div class="form-group mb-3">
						<label class="form-label">업로드 파일</label>
						<input type="file" class="form-control" name="upfile" />
					</div>
					<div class="text-end">
						<button type="submit" class="btn btn-primary btn-sm">업로드</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" ></script>
</body>
</html>