package dev.felnull.reglia;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 他プラグインから Reglia に通知を送るための公開API。
 * すべて非同期（送信は戻り値の CompletableFuture で結果を受け取る）。
 */
public interface RegliaAPI {

    /** 例: "1.0" */
    String getApiVersion();

    /** 最小: {message} を差し込むだけの通知 */
    CompletableFuture<Boolean> notify(String formatName, String message);

    /** 任意のプレースホルダ差し込み（{key} → value） */
    CompletableFuture<Boolean> notify(String formatName, Map<String, String> data);

    /** タグ付き（{tags} に join した文字列が入る想定） */
    CompletableFuture<Boolean> notify(String formatName, Map<String, String> data, List<String> tags);
}