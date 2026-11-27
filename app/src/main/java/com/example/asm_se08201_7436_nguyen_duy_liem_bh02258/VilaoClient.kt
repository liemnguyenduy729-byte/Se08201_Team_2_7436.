package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class VilaoClient {
    fun ask(key: String, question: String): String {
        val connection = URL("https://api.vilao.ai/v1/chat/completions").openConnection() as HttpURLConnection
        try {
            connection.requestMethod="POST"
            connection.instanceFollowRedirects = false
            connection.connectTimeout=15000
            connection.readTimeout=90000
            connection.doOutput=true
            connection.setRequestProperty("Authorization", "Bearer " + key)
            connection.setRequestProperty("Content-Type", "application/json")
            val messages=JSONArray().put(JSONObject().put("role","system").put("content","Bạn là AI Study Mentor. Giải thích kiến thức theo từng bước, khuyến khích tự học và nói rõ khi không chắc chắn.")).put(JSONObject().put("role","user").put("content",question))
            val body=JSONObject().put("model","occ/claude-opus-4-8").put("messages",messages).put("max_tokens",1600)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            if(connection.responseCode !in 200..299) throw IllegalStateException(when(connection.responseCode) { 401,403 -> "Khóa API không hợp lệ hoặc không có quyền truy cập."; 429 -> "Đã vượt giới hạn yêu cầu. Vui lòng thử lại sau."; else -> "Dịch vụ AI đang lỗi. Vui lòng thử lại." })
            val data=connection.inputStream.bufferedReader().use { it.readText() }
            val answer = JSONObject(data).optJSONArray("choices")?.optJSONObject(0)
                ?.optJSONObject("message")?.opt("content") as? String ?: ""
            if(answer.isBlank()) throw IllegalStateException("AI chưa trả về câu trả lời. Hãy thử lại.")
            return answer
        } finally { connection.disconnect() }
    }
}
