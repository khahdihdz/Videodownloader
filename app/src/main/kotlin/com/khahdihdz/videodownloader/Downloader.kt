package com.khahdihdz.videodownloader
import android.content.Context
import dev.ffmpegkit_maintained.ytdlp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class Format(val id:String,val height:Int,val label:String)
data class VideoInfo(val url:String,val platform:VideoPlatform,val title:String,val duration:Long,val thumbnail:String?,val formats:List<Format>)

class Downloader(context: Context) {
 init { YtDlp.init(context.applicationContext) }

 suspend fun analyze(url:String):VideoInfo {
  val normalized = canonicalYoutubeUrl(url)
  val meta = fetchOembedOrPage(normalized)
  val logs = mutableListOf<String>()
  val req=YtDlpRequest(normalized).addOption("-F").addOption("--no-playlist")
  val response = YtDlp.executeDebug(
   req,
   object:LogCallback{
    override fun onLog(level:String,message:String){ logs += message }
   },
   null
  ).get()

  val heights=logs.flatMap{line->
   Regex("""(?<!\d)(\d{3,4})p(?!\d)""").findAll(line)
    .map{it.groupValues[1].toInt()}
    .toList()
  }.filter{it>0&&it<=4320}.distinct().sorted()

  val formats=heights.map{Format("best[height<=$it]",it,"$it p")}
  // Metadata must remain usable even when YouTube blocks format enumeration.
  // Recent YouTube changes can make yt-dlp fail to list formats while oEmbed/page metadata still works.
  return VideoInfo(normalized,UrlDetector.detect(normalized),meta.first,meta.second,meta.third,formats)
 }

 private fun canonicalYoutubeUrl(raw:String):String {
  val platform=UrlDetector.detect(raw)
  if(platform!=VideoPlatform.YOUTUBE) return raw
  return runCatching {
   val u=java.net.URI(raw.trim())
   val host=(u.host?:"").lowercase().removePrefix("www.")
   val videoId=when {
    host=="youtu.be" -> u.path.trim('/').substringBefore('/')
    else -> u.getQueryParam("v")
     ?: u.path.substringAfter("/shorts/","").substringBefore('/')
     ?: u.path.substringAfter("/live/","").substringBefore('/')
   }
   if(videoId.isNullOrBlank()) raw
   else "https://www.youtube.com/watch?v=$videoId"
  }.getOrDefault(raw)
 }

 private fun fetchOembedOrPage(url:String):Triple<String,Long,String?> {
  try {
   val api="https://www.youtube.com/oembed?url="+URLEncoder.encode(url,"UTF-8")+"&format=json"
   val conn=URL(api).openConnection() as HttpURLConnection
   conn.connectTimeout=10000;conn.readTimeout=10000
   if(conn.responseCode in 200..299){
    val j=JSONObject(conn.inputStream.bufferedReader().use{it.readText()})
    return Triple(j.optString("title","Video"),0L,j.optString("thumbnail_url",null))
   }
  } catch(_:Throwable){}

  return try {
   val conn=URL(url).openConnection() as HttpURLConnection
   conn.connectTimeout=10000;conn.readTimeout=10000
   conn.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36")
   conn.setRequestProperty("Accept-Language","vi-VN,vi;q=0.9,en;q=0.8")
   val html=conn.inputStream.bufferedReader().use{it.readText()}
   val title=findMeta(html,"og:title") ?: Regex("""<title[^>]*>(.*?)</title>""",RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1) ?: "Video"
   val thumb=findMeta(html,"og:image")
   Triple(decodeHtml(title).trim(),0L,thumb?.let(::decodeHtml))
  } catch(e:Throwable) {
   throw IllegalStateException("Không lấy được thông tin video: "+(e.message?:"unknown"),e)
  }
 }

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
  val req=YtDlpRequest(info.url).setOutputTemplate(out.absolutePath).addOption("-f",f.id).addOption("--no-playlist").addOption("--newline")
  val job=YtDlp.executeAsync(req,object:DownloadProgressCallback{
   override fun onProgressUpdate(progress:Float,etaInSeconds:Long,line:String){trySend(progress.toInt().coerceIn(0,100))}
  })
  launch { try { job.get(); trySend(100) } finally { close() } }
 }

 private fun java.net.URI.getQueryParam(name:String):String? =
  rawQuery?.split("&")?.firstNotNullOfOrNull{
   val key=it.substringBefore("=")
   if(key==name) java.net.URLDecoder.decode(it.substringAfter("=",""),"UTF-8") else null
  }
}
