# Read Only Memory

RecomposedLogic において、ROMとは純粋なプログラムを格納することができる領域である。

## ROM assembling

ROMを焼き込むには、以下の工程を踏む。

1. Circuit Origin をクラフト
2. Circuit Scanner をクラフト
3. レンチでCircuit Originを右クリックしてペアリングモード
4. 同じレンチでCircuit Scannerを右クリックしてペアリング完了
5. ROM Programmerをクラフト
6. レンチでCircuit Scannerを右クリックしてペアリングモード
7. レンチでROM Programmerを右クリックしてペアリング完了
8. Tiny Memoryをクラフト
9. ROM ProgrammerへTiny Memoryを投入

## block: External Port

ROM の入出力面を定義する。

カテゴリ:
* 外部ポートカテゴリ

入出力面:
* port\[default=$arbitrary\]: RecomposedLogic::SingleChannel&lt;'pattern&gt;

`port` は外界との信号を入出力する。

プロパティ
* direction\[default=incoming\]: 信号の向きを定義する。
* pattern\[default=$pattern-of(any)\]: 信号のパターンを固定する。`any` モードの場合はパススルーし、非 `any`モードの場合は異なるパターンが流されたときにポートパターンエラーにする。

direction については、右クリックで出るメニューから切り替えられる。
pattern については、右クリックで出るメニューから切り替えるか、Pin Cableが繋がったときの自動推論で切り替えることができる。

## block: Circuit Origin

ROM の基準点を定義する。右クリックメニューで矩形範囲を拡張・縮小・平行移動することができる。

## item: tiny memory

ROM 領域が27立法ブロックまでのROM焼きに使用できるメモリーアイテム。

## block: ROM Programmer

右クリックでインベントリを開く。

Circuit Origin / Circuit Scannerがペアリングされている状態で、メモリーアイテムを置いて、レッドストーン信号を入力すると作業が開始される。
作業ルールについては後述セクションを参照。

## ROM作業ルール

1. Circuit Originの参照領域の体積がメモリーアイテムの最大領域体積よりも大きい場合は容量超過エラーで終了。
2. Circuit Originの参照領域において、参照領域の最も外側のブロックの集合に外接するブロックにおいて、1つの面につき外部ポートカテゴリのブロックが2個以上ある場合はポート数過剰エラーで終了。
3. Circuit Originの参照領域において、サポートされていないブロックはすべて空気ブロックと同様に扱われる。 \
    サポートされているブロックは、以下のとおりである:
    * 各種定数ゲート
    * 各種スカラーゲート
    * 各種ブーリアンゲート
    * Pin Cable
    * ROM起動ゲート
    * レッドストーンブロック
4. Circuit Originの参照領域において、コード変換バックエンドで致命的失敗が発生した場合は作成失敗エラーで終了。
5. コード変換バックエンドから成功が報告された後、作成成功リザルトで終了。メモリーアイテムの個数を1減らし、焼き込み済みのアイテムを出力アイテムスロットに搬出。

## block: ROM Powering Gate

入力:
* rom: Minecraft::ItemSlot
* pins: *dynamic*

出力:
* pins: *dynamic*

`rom` にROM焼きしたROMを入れると、ブロックがピンチャネルを受け付けるようになる。このときの入出力はROMの定義に完全に依存する。

