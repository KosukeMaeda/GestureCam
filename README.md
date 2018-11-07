# GestureCam
ジェスチャでキャプチャ

## WHAT
みなさん自撮りしてますか？ 昨今の自撮りブームでいろいろなカメラアプリが流行っていますよね。 でも、まだまだ自撮りって難しいと思うんですよ。シャッターボタンを押すときにブレてしまったり、セルフタイマーは押してからダッシュしなきゃいけなかったり。 そこで紹介するのがこの"ジェスチャでキャプチャ"です。カメラに向けてポーズをとれば、はいおっけー！簡単に、スマートに写真を撮ることができちゃいます！

## Usage
1. get token from Face++
2. add the tokens to `app/src/main/res/values/strings.xml`
```xml
<resources>
    <string name="app_name">ジェスチャでキャプチャ</string>
    <string name="api_key">YOUR_API_KEY</string>
    <string name="api_secret_key">YOUR_SECRET_API_KEY</string>
</resources>
```
3. build and run!

## Avaliable gestures
 - heart_a
 - heart_b
 - heart_c
 - heart_d
 - thumb_up
 - phonecall
 - rock

Illustratios of gestures are [here](https://console.faceplusplus.com/documents/10069384)
