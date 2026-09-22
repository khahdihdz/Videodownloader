package com.khahdihdz.videodownloader

import android.content.Context
import dev.ffmpegkit_maintained.ytdlp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder

data class Format(val id:String,val height:Int,val label:String)
data class VideoInfo(val url:String,val platform:VideoPlatform,val title:String,val duration:Long,val thumbnail:String?,val formats:List<Format>)

class Downloader(context: Context) {
    init { YtDlp.init(context.applicationContext) }

    suspend fun analyze(url:String):VideoInfo {
        val normalized = canonicalYoutubeUrl(url)
        val meta = fetchOembedOrPage(normalized)

        // Analysis must not invoke yt-dlp. YouTube can make format probing block
        // for a long time; the UI can safely use the "Tốt nhất" fallback format.
        return VideoInfo(
            normalized,
            UrlDetector.detect(normalized),
            meta.first,
            meta.second,
            meta.third,
            emptyList()
        )
    }

    private fun canonicalYoutubeUrl(raw:String):String {
        if (UrlDetector.detect(raw) != VideoPlatform.YOUTUBE) return raw
        return runCatching {
            val u=URI(raw.trim())
            val host=(u.host?:"").lowercase().removePrefix("www.")
            val videoId=when {
                host=="youtu.be" -> u.path.trim('/').substringBefore('/')
                else -> u.getQueryParam("v")
                    ?: u.path.substringAfter("/shorts/","").substringBefore('/')
                    ?: u.path.substringAfter("/live/","").substringBefore('/')
            }
            if(videoId.isNullOrBlank()) raw else "https://www.youtube.com/watch?v=$videoId"
        }.getOrDefault(raw)
    }

    private fun fetchOembedOrPage(url:String):Triple<String,Long,String?> {
        val videoId = youtubeVideoId(url)
        val thumbnail = videoId?.let { "https://i.ytimg.com/vi/$it/hqdefault.jpg" }

        try {
            val api="https://www.youtube.com/oembed?url="+URLEncoder.encode(url,"UTF-8")+"&format=json"
            val conn=URL(api).openConnection() as HttpURLConnection
            conn.connectTimeout=7000
            conn.readTimeout=7000
            conn.instanceFollowRedirects=true
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36")
            conn.setRequestProperty("Accept","application/json,text/plain,*/*")
            conn.setRequestProperty("Accept-Language","vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
            if(conn.responseCode in 200..299){
                val j=JSONObject(conn.inputStream.bufferedReader().use{it.readText()})
                val title=j.optString("title").trim()
                if(title.isNotBlank()) return Triple(title,0L,j.optString("thumbnail_url",thumbnail))
            }
            conn.disconnect()
        } catch(_:Throwable){}

        try {
            val conn=URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout=7000
            conn.readTimeout=7000
            conn.instanceFollowRedirects=true
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36")
            conn.setRequestProperty("Accept-Language","vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
            conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            val html=conn.inputStream.bufferedReader().use{it.readText()}
            conn.disconnect()
            val title=findMeta(html,"og:title")
                ?: Regex("""<title[^>]*>(.*?)</title>""",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).find(html)?.groupValues?.get(1)
                ?: Regex("""\"title\"\s*:\s*\"((?:\\.|[^\"])*)\"""").find(html)?.groupValues?.get(1)
            if(!title.isNullOrBlank()) {
                return Triple(decodeHtml(title).trim(),0L,findMeta(html,"og:image")?.let(::decodeHtml) ?: thumbnail)
            }
        } catch(_:Throwable){}

        return Triple(if(videoId != null) "YouTube video ($videoId)" else "YouTube video",0L,thumbnail)
    }

    private fun youtubeVideoId(url:String):String? = runCatching {
        val u=URI(url.trim())
        val host=(u.host?:"").lowercase().removePrefix("www.")
        when {
            host=="youtu.be" -> u.path.trim('/').substringBefore('/').takeIf{it.matches(Regex("[A-Za-z0-9_-]{6,}"))}
            else -> (u.getQueryParam("v")
                ?: u.path.substringAfter("/shorts/","").substringBefore('/')
                ?: u.path.substringAfter("/live/","").substringBefore('/'))
                .takeIf{it.matches(Regex("[A-Za-z0-9_-]{6,}"))}
        }
    }.getOrNull()

    private fun findMeta(html:String,name:String):String? {
        val tags=Regex("""<meta\s+[^>]*>""",RegexOption.IGNORE_CASE).findAll(html)
        for(m in tags){
            val tag=m.value
            val prop=Regex("""(?:property|name)\s*=\s*["']([^"']+)["']""",RegexOption.IGNORE_CASE).find(tag)?.groupValues?.get(1)
            if(prop.equals(name,true))
                return Regex("""content\s*=\s*["']([^"']*)["']""",RegexOption.IGNORE_CASE).find(tag)?.groupValues?.get(1)
        }
        return null
    }

    private fun decodeHtml(value:String):String =
        value.replace("&amp;","&").replace("&quot;","\"").replace("&#39;","'")
            .replace("&lt;","<").replace("&gt;",">")

    fun download(info:VideoInfo,f:Format,out:File):Flow<Int> = channelFlow{
        val req=YtDlpRequest(info.url)
            .setOutputTemplate(out.absolutePath)
            .addOption("-f",f.id)
            .addOption("--no-playlist")
            .addOption("--newline")
        val job=YtDlp.executeAsync(req,object:DownloadProgressCallback{
            override fun onProgressUpdate(progress:Float,etaInSeconds:Long,line:String){
                trySend(progress.toInt().coerceIn(0,100))
            }
        })
        launch {
            try { job.get(); trySend(100) }
            finally { close() }
        }
    }

    private fun URI.getQueryParam(name:String):String? =
        rawQuery?.split("&")?.firstNotNullOfOrNull{
            val key=it.substringBefore("=")
            if(key==name) URLDecoder.decode(it.substringAfter("=",""),"UTF-8") else null
        }
}
