# RegliaSystem

**RegliaSystem** は、Minecraft Paper サーバー (1.16.5 対応) 用の Discord 通知プラグインです。  
JDA を利用して Discord ボットと連携し、サーバー内イベントやコマンドから指定フォーマットの通知を送信できます。  
また、Velocity プロキシのサーバーIDも自動取得し、通知に `{server}` として埋め込めます。

---

## 📦 機能

- Discord ボットと接続し、任意のフォーマットでメッセージ送信
- `formats/` ディレクトリの YAML で通知フォーマットを柔軟に定義
- `/dnotice` コマンドで通知送信・リロードなどを操作
- Velocity プロキシの **サーバID** を Plugin Messaging 経由で取得し、プレースホルダ `{server}` で利用可能

---

## 🔧 導入方法

1. `config.yml` に Discord Bot のトークンやデフォルトサーバ名を記入
   ```yaml
   token: "YOUR_DISCORD_BOT_TOKEN"
   serverName: "lobby"   # Velocity IDが取れないときのフォールバック
   ```

2. `formats/` ディレクトリを自動生成、または必要に応じて自作  
   - プラグイン初回起動時に `formats/` が生成されます  
   - 例: `formats/info.yml`
     ```yaml
     name: "info"
     channel: "123456789012345678"   # Discord チャンネルID
     priority: 1
     format: "[Reglia:{server}] {message}"
     ```

   - 複数チャンネルも指定可能:
     ```yaml
     channel:
       - "123456789012345678"
       - "234567890987654321"
     ```

3. プラグインを `plugins/` に入れてサーバー起動

---

## 💬 コマンド

- `/dnotice send <format> <message>`  
  指定フォーマットで Discord にメッセージ送信  
  例:  
  ```
  /dnotice send info "サーバー起動しました"
  ```

- `/dnotice reload`  
  `formats/` 内のフォーマットファイルを再読み込み

- `/dnotice init`（任意実装）  
  デフォルトのフォーマットファイルを書き出す

---

## 🧩 プレースホルダ
- `{player}` : Sender名
- - `{time}` : 現在の時刻
- `{server}` : Velocity のサーバID（例: `lobby`）。未取得時は `config.yml` の `serverName` を使用
- `{message}` : コマンド/APIで渡した本文
- 今後 `{time}` なども拡張予定

---

## 🚀 Velocity サーバID 取得

- **BungeeCord Plugin Messaging Channel** を使用
- 起動時にプレイヤーが居れば即問い合わせ、Join 時にも問い合わせ
- 取得結果はログに表示され、`{server}` プレースホルダに差し込まれます

---

## ⚠️ 注意事項

- Discord の Bot Token には **必要な Intent** を有効化しておく必要があります。  
  → JDA6 を使うので [Discord Developer Portal] の設定確認を忘れずに。

---

## ✅ TODO / 今後の予定

- 通知トリガーを Bukkit API から追加（イベント監視）
- Embed フォーマット対応
- 他プラグインから呼び出せる API 拡張
- Bot のチャット応答機能
