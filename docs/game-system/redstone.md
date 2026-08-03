# レッドストーン連携

## Redstone Power Importer
* 入力: Minecraft::Redstone
* 出力: RecomposedLogic::SingleChannel&lt;Scalar&gt;

レッドストーン信号を受け取り、それを1ピンのスカラー信号として出力する。

## Redstone Power Exporter

* 入力: RecomposedLogic::SingleChannel&lt;Scalar&gt;
* 出力: Minecraft::Redstone

1ピンのスカラー信号を受け取り、それをレッドストーン信号として出力する。

以下は、範囲処理及び端数処理に関する規則。
* スカラー信号が0.0未満のときはRedstone Strength 0を出力する。
* スカラー信号が15.0よりも大きいときはRedstone Strength 15を出力する。
* スカラー信号の小数部分は切り捨てる。
