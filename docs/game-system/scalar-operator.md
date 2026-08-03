# スカラーオペレーター

## Scalar Constant

入力: なし
出力: RecomposedLogic::SingleChannel&lt;Scalar&gt;

スカラー値を生成することができる。

## Scalar Additive Operator

入力:
* left\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;
* right\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;
出力: RecomposedLogic::SingleChannel&lt;Scalar&gt;

`left` と `right` からスカラー値を読み取り、両方の強度を足した強度を出力する。

## Scalar Comparator

入力:
* left\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;
* right\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;
* operator\[default=ComparatorOperator::Equals\]: RecomposedLogic::ComparatorOperator
出力: RecomposedLogic::SingleChannel&lt;Boolean&gt;

`left` と `right` からスカラー値を読み取り、オペレーターで比較した結果を出力する。

`operator` は右クリックメニューから表示・設定することができる。

UI画面は `left` `operator` `right` のような表示にし、両ピンの現在の入力値・現在の出力値がわかるようにする。

## Enum ComparatorOperator

* Equals
* NotEquals
* GreaterThan
* GreaterOrEqual
* LessThan
* LessThanOrEqual
