# 型システム

* Scalar: IEEE-754の倍精度実数。ただし、以下の違いがある。
    * +0.0及び-0.0は0.0として表示される。
    * Infinity及び-Infinityは表示されない。
    * NaNは出てこない。
* Boolean: true 及び false のみを構築子として持つ列挙型。
* U8: 8ビット符号なし整数。
* U16: 16ビット符号なし整数。
* U32: 32ビット符号なし整数。
* U64: 64ビット符号なし整数。
* I8: 8ビット符号つき整数（2の補数）。
* I16: 16ビット符号つき整数（2の補数）。
* I32: 32ビット符号つき整数（2の補数）。
* I64: 64ビット符号つき整数（2の補数）。
* F32: IEEE-754の単精度実数。
* F64: IEEE-754の倍精度実数。

## 代入可能性
型Aの値が型Bへ代入可能である条件は以下の通り。

* T -> T
* Scalar -> F64

それ以外は明示的な変換を挟む必要がある。

### 値を保存する整数拡幅
Integer Zero-Extension Gate を使うこと

* U8 -> U16 or U32 or U64
* U16 -> U32 or U64
* U32 -> U64

Integer Signed-Extension Gate を使うこと
* I8 -> I16 or I32 or I64
* I16 -> I32 or I64
* I32 -> I64

### 値域がカバーされているケース
Integer Range-Preserving Conversion Gate を使うこと

* U8 -> I16 or I32 or I64
* U16 -> I32 or I64
* U32 -> I64

### 精度が失われるケース

以下のゲートを使うこと
* Extract Least Bits Gate
* Integer Saturate Gate
* Nearest Floating Point Number Calculation Gate
* Rounded Integer Value Calculation Gate
