# ブーリアンオペレーター

## Boolean AND Gate

入力:
* left\[default=false\]: RecomposedLogic::SingleChannel&lt;Boolean&gt;
* right\[default=false\]: RecomposedLogic::SingleChannel&lt;Boolean&gt;

出力: RecomposedLogic::SingleChannel&lt;Boolean&gt;

`left` 及び `right` が true なら出力を true にし、そうでなければ false にする。

## Boolean Conditional Gate

入力:
* condition\[default=false\]: RecomposedLogic::SingleChannel&lt;Boolean&gt;
* ifTrue\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;
* ifFalse\[default=0.0\]: RecomposedLogic::SingleChannel&lt;Scalar&gt;

出力: RecomposedLogic::SingleChannel&lt;Scalar&gt;

`condition` が true なら出力を `ifTrue` の値にし、そうでなければ `ifFalse` の値にする。

## Boolean NOT Gate

入力:
* value\[default=false\]: RecomposedLogic::SingleChannel&lt;Boolean&gt;

出力: RecomposedLogic::SingleChannel&lt;Boolean&gt;

`value` が true なら出力を false にし、そうでなければ true にする。
