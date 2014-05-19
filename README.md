soetl
=====

A SQL-oriented ETL tool.

## コンセプト

汎用ETLはノンコーディングを目指すあまり、処理効率の高いプログラムにはなりにくい。

この課題を解決するには、SQLに処理を寄せていくことがもっとも有効ではないかと考え、
それに特化したETLを作ります。


## Modules

### Extract

任意のデータソースからデータを取得するモジュールです。
全てのデータは、ワークテーブルにロードされます。

利用可能なデータソースは、以下のとおりです。

* ファイル
* HTTP (プル)

```
(extract "/mnt/data/hoge.csv")

(extract "http://xxx/yyy/zzz" :format :csv)
```

また、利用可能なフォーマットは以下のとおりです。

* CSV
* XML (予定)
* JSON (予定)

### Transform

登録されたSQL(SELECT文)を実行します。

```clojure
(transform "SELECT TO_NUMBER(id), name FROM work_users")
```

Loadコンポーネントで処理されるためのコンポーネントです。

### Load

* テーブル
** INSERT-UPDATE
** DELETE-INSERT


* ファイル
** CSV

```clojure
(load-to :table "schema.table" :mode :insert-update)

(load-to :url "file://xxx/yyy/zzz")
```


