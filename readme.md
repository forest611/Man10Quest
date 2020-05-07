# Man10Quest

## config

### フォルダ構成

~~~
/plugins/Man10Quest
├── config.yml
├── sample1
│   ├── sample_quest1.yml
│   ├── sample_quest2.yml
│   └── setting.yml
└── sample2
~~~

### setting.yml

- ``name`` クエストタイプの識別名(ex:quest_newbie)**重複しないようにしてください**
- ``title`` クエストタイプの名前(ex:お初クエスト)
- ``material`` インベントリで表示するアイテム([Material一覧](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html))
- ``damage`` custom model dataの値(1.12ではダメージ値)
- ``rank``　表示上の推奨ランク(loreに書かれます)
- ``hide`` 裏クエストにするかどうか(def:false)
- ``daily`` デイリークエストにするかどうか(def:false)
- ``number`` 表示順

### questの書き方

ファイル名は自由ですが、yaml形式にしてください。

- ``name`` クエストの識別名(ex:quest1) **重複しないようにしてください**
- ``title`` クエストの名前
- ``lore`` クエストの説明欄
- ``description`` クエストの説明(クエスト開始時に表示される)
- ``material`` インベントリで表示するアイテム
- ``damage`` custom model dataの値
- ``rank`` 表示上の推奨ランク
- ``hide`` 裏クエストにするかどうか(def:false)
- ``finishMsg`` クリアしたときのメッセージ
- ``replcaTitle`` クリアしたときにもらえるカードの表示名
- ``msg`` 指定メッセージを入力したらクリア(list形式、任意)
- ``cmd`` 指定コマンド実行でクリア(list形式、一部入力も可、任意)
- ``once`` 一度だけクエストをプレイできる(def:true)
- ``unlock`` 指定クエストクリアまでロック(list、指定クエストの``name``を入力)
- ``daily`` デイリークエスト

