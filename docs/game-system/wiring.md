# ワイヤリング

RecomposedLogicの各ブロックはワイヤーを結線することによりデータをやり取りする。

## (item) Pin Cable

**無色の**Pin Cable Blockを置くためのアイテム。後述。

## (item) *Dyed* Pin Cable
(*Dyed*のところにはMinecraft標準16色の名前が入る)

**染色済みの**Pin Cable Blockを置くためのアイテム。後述。

なお、複数色状態のPin Cable Blockに同じ色の*Dyed* Pin Cableを設置しようとしても何も起こらず、アイテムも消費しない。

単色状態のPin Cable Blockに同じ色の*Dyed* Pin Cableを使った場合は、選択した面の隣接位置が置換可能であれば、そこへ同色のPin Cable Blockを設置する。これは通常のケーブル延長操作として扱う。

## Pin Cable Block

通行データ型: RecomposedLogic::SingleChannel&lt;?&gt;

各染色ケーブルは、それぞれ独立した
RecomposedLogic::SingleChannel&lt;?&gt;として扱う。

### 概要

Pin Cable Blockは、1ピン同士で装置を結ぶ細いケーブルである。

通常のケーブルはブロック座標軸（X/Y/Z）のいずれかに平行な**直線ケーブル**として存在し、曲がりや分岐が必要になった場合のみ**内部ジャンクションブロック**へ遷移する。

### ケーブルの種類

#### 無色ケーブル

- 無色ケーブル同士は接続する。
- 単一色のみを持つ染色ケーブルと接続する。
- 複数色を保持した染色ケーブルとは接続しない。
- 無色ケーブルを介して異なる色のネットワークが接続されることを許可する。
- 同一ブロック内へ染色ケーブルと重ねることはできない。

#### 染色ケーブル

Minecraft標準16色それぞれに対応する。

- 同色の染色ケーブルと接続する。
- 無色ケーブルと接続できる（ただし単色状態のみ）。
- 異なる色同士は直接接続しない。
- アイテムとしてのみ存在し、実際にはどれを置いても同じブロックになる (後述)。

### 重ね合わせ

染色ケーブルは同一ブロック内へ重ねて配置できる。

- 1ブロックにつき各色1本まで配置可能。
- 最大16色を同一ブロックへ保持できる。
- 同じ色を複数本保持することはできない。
- 無色ケーブルは同一ブロックへ保持できない。

重ね合わせはBlockEntityで管理する。

### 接続

#### 単色状態

染色ケーブルが1色だけ存在する場合は通常のケーブルとして扱う。

```

赤 ─ 赤
赤 ─ 無色

```

は接続する。

```

赤 ─ 青

```

は接続しない。

---

#### 複数色状態

複数色を保持したブロックは、

- 同じ色同士のみ接続する。
- 無色ケーブルとは接続しない。
- 無色ケーブルとの色ごとの接続候補判定は行わず、
  常に非接続として扱う。

例

```

[赤 青] ─ [青 黄]

```

では青のみ接続する。

```

無色 ─ [赤 青]

```

は接続しない。

複数色状態と無色ケーブルとの相互接続はChannel Selector Block/Channel Injector Blockによって行う。

### モデル

Pin Cableは`2 × 2 × 16`の直方体モデルを使用する。

通常状態では原木と同様に

- X軸
- Y軸
- Z軸

の3方向へ回転する。

複数色状態では保持している各色のケーブルを同時に描画する。

各色はブロック断面内の固有位置へ描画し、互いに重ならないようにする。

単色のColored Pin CableがUndyed Pin CableまたはUndyed Internal Junction Blockへ接続する場合は、中心の無色ピンと色固有スロットを結ぶ曲線アダプタとして描画する。アダプタは接続方向の6方向すべてへ回転し、連続するColored Pin Cable側へ2モデル単位だけ延長する。

### データ管理

#### BlockState

保持する情報は最小限とする。

- 軸

色ごとの接続情報は保持しない。

#### BlockEntity

染色済みケーブルの保持色集合を管理する。接続方向は保持せず、周囲のブロック状態から都度再計算する。

- 保持中の色

#### 描画

BlockEntityRenderer を使う。通常のBS分岐では手に負いきれない。

## Undyed Internal Junction Block

(※クリエタブ入手不可能ブロック)

管理用の内部ブロック。Pin Cableは直線のみ表現できるが、以下を表現するためのブロック。

- 曲がる
- 分岐する

なお、*Dyed* Pin Cable同士を短絡するときに上記が起こる場合も含む。

Undyed Internal Junction Blockに接続された全方向は、 色にかかわらず同一ネットワークとして扱う。

それ以外はDyed Internal Junction Blockと同様のため、下記を参照せよ。

## Dyed Internal Junction Block

(※クリエタブ入手不可能ブロック)

管理用の内部ブロック。 *Dyed* Pin Cableは直線のみ表現できるが、以下を表現するためのブロック。

- いずれかの色が曲がる
- いずれかの色が分岐する
- 異なる色の立体交差

axisプロパティは持たない。
ジャンクションが不要になった場合はaxisを再計算し、自動的にPin Cable Blockへ戻す。

判定はブロック更新時・設置時・破壊時に再計算する。

Dyed Internal Junction Blockでは、同じ色で接続された全方向を同一ネットワークとして扱い、異なる色同士は接続しない。

隣接する染色ケーブルまたはDyed Internal Junction Blockが保持する色は、Dyed Internal Junction Blockへ取り込まれる。取り込まれた色は、その隣接ブロックとの接続が当該色だけの終端であっても保持する。これにより、複数色ケーブルがカーブまたは分岐する地点では、各色が対応するピンへ接続できる。

以下に状態の例を示す。
* `-` 及び `|`: 便宜的にRed Pin Cable
* `=` 及び `:`: 便宜的にBlue Pin Cable
* `J`: Internal Junction Block

1. 通常状態
    ```
    -------------------
    ```
2. 曲がり角はジャンクションになる
    ```
    ---------J
             |
             |
    ```
3. 十字路はジャンクションになる
    ```
            |
    --------J------
            |
    ```
4. 違う色が立体交差するときジャンクションになる
   ```
            |
   =========J=======
            |
   ```
5. 異なる色の経路が同一ブロックを共有し、
   片方がその地点で終端する場合
   ```
   =========J=======
            |
            |
   ```

## Bus Cable

通行データ型: RecomposedLogic::GroupedChannel16&lt;? extends Tuple&gt;

16ピン同士で装置を結ぶ。

## コンパイル時の扱い

[ROM](./rom.md)へコンパイルする組み合わせ回路では、1つの入力ネットを駆動できる出力は1つだけとする。
同じ入力ネットに複数の出力が接続されている場合、回路スキャンはコンパイルエラーにする。
値を合流したい場合は、加算・ANDなど用途に対応するゲートを明示的に挟む。

ROMへコンパイルする組み合わせ回路では、出力が状態要素を介さず自分自身の入力へ戻る閉路を許可しない。
閉路が検出された場合、コンパイルエラーにする。

## ケーブルの接続ルールまとめ

| 接続元 | 接続先 | 接続 |
|--------|--------|------|
| 無色 | 無色 | ○ |
| 無色 | 単色 | ○ |
| 無色 | 複数色 | × |
| 単色 | 同色 | ○ |
| 単色 | 異色 | × |
| 単色 | 複数色 | 同色のみ |
| 複数色 | 複数色 | 共通する色のみ |

## Channel Selector Block

入力: 標準16カラーポート
出力: 一般1ピン

右クリックでGUIを開き、どの色のケーブル**の**値を出力するか決める。

## Channel Injector Block

入力: 一般1ピン
出力: 標準16カラーポート

右クリックでGUIを開き、どの色のケーブル**へ**値を出力するかを決める。

## ピンの接続ルール

一般1ピンポートは、無色ケーブルおよび単色状態の任意の染色ケーブルと接続する。

一般1ピンポートは、複数色状態には直接接続しない。

*Dyed* Pin Cableがカラーポートと接続される場合、ピンの色とケーブルの色は対応する。

## Appendix A. 実装上の補足

Pin Cable Blockは無色・染色を問わずBlockEntityを持つ。
染色済みのBlockEntityは保持色集合を永続化する。無色・染色の種別はブロック種別で決まり、軸と接続方向は周囲のブロック状態から再計算する。

通常のPin Cable Blockとして存在できるのは、保持している**全色が**同一軸上の直線として表現できる場合に限る。
それ以外の場合は対応するInternal Junction Blockへ変換する。

無色ケーブルと染色ケーブルを同一座標へ重ねようとした場合、設置は失敗し、ブロックは変化せず、アイテムも消費しない。

複数色状態のブロックを通常破壊した場合、保持している全色をまとめて除去し、それぞれ対応するアイテムをドロップする。

Internal Junction Blockは既存のjunctionブロックを流用せず、`undyed_internal_junction`および`dyed_internal_junction`として新規登録する。

Pin Cable Blockおよび両Internal Junction BlockはBlockEntityを持つ。
ケーブル種別、保持色集合などのデータ構造は可能な限り共通化する。

複数色状態へ未保持の色の*Dyed* Pin Cableを追加した場合、色の追加に成功し、対応アイテムを1個消費する。
クリエイティブモードでは消費しない。

周囲に接続先がない状態でケーブルを設置した場合、初期軸は設置対象としてクリックした面の軸とする。

通常破壊時、無色ケーブルはPin Cableを1個、単色の染色ケーブルは対応する*Dyed* Pin Cableを1個ドロップする。

16色集合を表現する値オブジェクト ColorSet16 を定義する。
永続化および同期のため Codec / StreamCodec を提供する。

## Appendix B. Further Extensions
色別除去操作は今回の実装範囲外とする。

Channel Selector BlockおよびChannel Injector Blockは今回の実装範囲外とする。

## Appendix C. 仕様とテストの対応

| 仕様 | テスト | 種別 |
|---|---|---|
| 無色と単色は接続し、無色と複数色は接続しない | `undyedCableConnectsOnlyToSingleColoredCable` | GameTest |
| 染色ケーブルは共通する色だけで接続する | `multiColorCablesConnectOnlyThroughSharedColors`、`scannerDoesNotConnectDifferentDyedCableColors` | GameTest |
| 異なる色はDyed Junction内で混線しない | `runtimeDoesNotLeakSignalBetweenColorsInDyedJunction`、`scannerDoesNotConnectAcrossDifferentColorsInDyedJunction` | GameTest |
| 同色はDyed Junctionを通過する | `runtimeKeepsSameColorConnectedThroughDyedJunction` | GameTest |
| カーブ・分岐・立体交差は対応するJunctionへ変換する | `bentPinCableBecomesInternalJunction`、`sameColorDyedBranchBecomesInternalJunction`、`differentDyedColorsCanCrossAsInternalJunction` | GameTest |
| 隣接Dyed Junctionおよび追加色は色ごとに接続する | `adjacentDyedJunctionsConnectEachSharedColor`、`dyedJunctionRetainsAdditionalColorFromAdjacentCable`、`newlyCreatedDyedJunctionCapturesAdjacentCableColors` | GameTest |
| 分岐解消後は直線ケーブルへ戻る | `junctionReturnsToStraightCableAfterBranchRemoval` | GameTest |
| 複数色を同一座標へ保持できる | `dyedCableBlockEntityCanHoldMultipleColors` | GameTest |
| 複数色状態へ同じ色は追加・消費・隣接設置しない | `duplicateColorDoesNotPlaceOrConsumeOnMultiColorCable` | GameTest |
| 単色状態へ同じ色を使うと対象面の隣接位置へ延長配置する | `duplicateColorExtendsSingleColorCable` | GameTest |
| 無色ケーブルへ染色ケーブルは重ねられない | `dyedCableCannotBePlacedOnUndyedCable` | GameTest |
| 複数色ケーブルを破壊すると保持色ごとのアイテムをドロップする | `multiColorCableDropContentsIncludesEveryHeldColor` | GameTest |

描画モデルのピン位置・太さはクライアント側のBERによる見た目であり、現行の自動GameTestでは検証できない。リソースモデルを変更した場合は、X/Y/Zの各軸と6方向の接続をクライアント上で確認する。

### 実行時のネットワークキャッシュ

結合ネットワークはワールドごとに保持し、毎ゲームティックの全体スキャンは行わない。
隣接更新時は更新座標とその6近傍のケーブル種別・色集合・ポートを比較し、接続構造が変わったネットワークだけを無効化する。次の評価時に再構築する。
信号値だけの変更や、同じ接続構造でのケーブル・ジャンクション間の変換ではネットワークを保持する。信号値のキャッシュはゲームティックごとに更新する。
チャンクのアンロード時は、そのチャンクに依存するネットワークを無効化する。

GameTest `networkCacheSurvivesSignalUpdatesAcrossTicks`、`networkCacheInvalidatesAfterColorAndPortChanges`、`runtimeInvalidatesNetworkAfterCablePathChanges`で検証する。

### Fabricクライアントのケーブル描画

ケーブルの箱モデルは、ブロック内座標へ位置・拡縮・回転を適用した頂点としてキャッシュする。
各ブロックの接続形状から組み立てたメッシュもキャッシュし、毎フレームの接続走査や箱ごとの汎用ブロック描画は行わない。
ブロック更新、ケーブルの色データ同期・変更、依存するチャンクのロード・アンロード時に該当メッシュを破棄する。
リソース再読み込み時には、テクスチャUVとアダプターモデルを含む全メッシュを破棄し、次の描画時に再生成する。
光量、overlay、カメラに対する座標変換は描画時に適用する。

互いに逆向きで同一平面にある長方形の面について、別の面に完全に覆われた内部面を除去する。
部分的に重なる面や斜めの面は残す。この判定はFabricのユニットテスト `CableMeshTest` で検証する。
見た目はクライアント上でX/Y/Zの直線、6方向のジャンクション・アダプター、色変更、リソース再読み込みを確認する。
