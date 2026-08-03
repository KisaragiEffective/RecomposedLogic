# ワイヤリング

RecomposedLogicの各ブロックはワイヤーを結線することによりデータをやり取りする。

## Pin Cable

通行データ型: RecomposedLogic::SingleChannel&lt;?&gt;

1ピン同士で装置を結ぶ。

## Bus Cable

通行データ型: RecomposedLogic::GroupedChannel16&lt;? extends Tuple&gt;

16ピン同士で装置を結ぶ。
