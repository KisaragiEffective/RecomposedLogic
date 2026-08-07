# マルチローダー固有の制約

## common プロジェクト

* ローダーに依存しないビジネスロジックを置く
* `net.minecraft.*` に依存するビジネスロジックを置く
    * 例: ブロックの定義
    * 例: ブロックエンティティの定義
* ユニットテストを置く

## fabric プロジェクト

* Fabric Loaderに依存するロジックを置く
* Fabric [GameTest](../tests/game-test.md)に依存するテストを置く

## forge プロジェクト

* Forge Mod Loaderに依存するロジックを置く
* Forge [GameTest](../tests/game-test.md)に依存するテストを置く

## neoforge プロジェクト

* NeoForge Loaderに依存するロジックを置く
* NeoForge [GameTest](../tests/game-test.md)に依存するテストを置く
