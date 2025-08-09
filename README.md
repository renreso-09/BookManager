# BookManager
書籍管理API

## 環境構築
1. 以下のコマンドを実行してMySQLを起動します。

```bash
docker compose up -d
```
2. Flywayを実行してデータベースのマイグレーションを行います。

```bash
./gradlew flywayMigrate
```
3. アプリケーションを起動します。  
BookmanagerApplication.ktを実行します。
http://localhost:8080/ でAPIを確認できます。  
request.httpを実行して、各APIのリクエストを確認できます。

## API仕様
### 著者に紐づく書籍一覧を取得
- エンドポイント：GET /api/books/authors/{authorId}

レスポンスの型

```
{
  "books": [
    {
      "id": Int //書籍ID
      "title": String, //書籍タイトル
      "price": Int, //書籍価格
      "status": String, //書籍ステータス(未出版: UNPUBLISHED, 出版済: PUBLISHED)
      "author": [
        {
          "id": Int,　//著者ID
          "name": String,　//著者名
          "birthDate": String　//著者生年月日(yyyy-MM-dd)
        }
      ]
    }
  ]
}
```

### 書籍を登録
- エンドポイント：POST /api/books

リクエストの型

```
{
  "title": String, //書籍タイトル
  "price": Int, //書籍価格
  "status": String, //書籍ステータス(未出版: UNPUBLISHED, 出版済: PUBLISHED)
  "authors": [
    {
      "name": String, //著者名
      "birthDate": String //著者生年月日(yyyy-MM-dd)
    }
  ]
}
```

### 書籍を更新
- エンドポイント：PUT /api/books/{bookId}

リクエストの型

```
{
  "title": String, //書籍タイトル
  "price": Int, //書籍価格
  "status": String, //書籍ステータス(未出版: UNPUBLISHED, 出版済: PUBLISHED)
  "authors": [
    {
      "name": String, //著者名
      "birthDate": String //著者生年月日(yyyy-MM-dd)
    }
  ]
}
```