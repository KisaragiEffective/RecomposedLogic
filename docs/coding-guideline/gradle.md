# Gradle に関してのコーディングライン

## GAV-style の依存性識別子を用いる

`compile` などで外部アーティファクトを参照するとき、named arguments ではなくコンマ区切りのGAV-styleの識別子を用いる。

参照: https://docs.gradle.org/8.14.3/userguide/best_practices_dependencies.html#single-gav-string
