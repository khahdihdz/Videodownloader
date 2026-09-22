package com.khahdihdz.videodownloader
import android.content.Context
import dev.ffmpegkit_maintained.ytdlp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.io.File

data class Format(val id:String,val height:Int,val label:String)
data class VideoInfo(val url:String,val platform:VideoPlatform,val title:String,val duration:Long,val thumbnail:String?,val formats:List<Format>)

class Downloader(context: Context) {
 init { YtDlp.init(context.applicationContext) }

 suspend fun analyze(url:String):VideoInfo {
  val meta = fetchOembedOrPage(url)
  val logs = mutableListOf<String>()
  val req=YtDlpRequest(url).addOption("-F").addOption("--no-playlist")
  YtDlp.executeDebug(req, object:LogCallback{override fun onLog(level:String,message:String){logs += message}}, null).get()
  val heights=logs.flatMap{line->Regex("""\b(\d{3,4})p\b""").findAll(line).map{it.groupValues[1].toInt()}.toList()}
    .filter{it>0&&it<=4320}.distinct().sorted()
  val formats=heights.map{Format("best[height<="+it+"]",it,it.toString()+"p")}
  if(formats.isEmpty()) throw IllegalStateException("Không có format video phù hợp.")
  return VideoInfo(url,UrlDetector.detect(url),meta.first,meta.second,meta.third,formats)
 }

 private fun fetchOembedOrPage(url:String):Triple<String,Long,String?> {
  return try {
   val api="https://www.youtube.com/oembed?url="+URLEncoder.encode(url,"UTF-8")+"&format=json"
   val conn=URL(api).openConnection() as HttpURLConnection
   conn.connectTimeout=10000;conn.readTimeout=10000
   if(conn.responseCode in 200..299){
    val j=JSONObject(conn.inputStream.bufferedReader().use{it.readText()})
    return Triple(j.optString("title","Video"),0L,j.optString("thumbnail_url",null))
   }
  } catch(_:Throwable){}
  val c=URL(url).openConnection() as HttpURLConnection
  c.connectTimeout=10000;c.readTimeout=10000;c.setRequestProperty("User-Agent","Mozilla/5.0")
  val html=c.inputStream.bufferedReader().use{it.readText()}
  val title=Regex("""<meta[^>]+property=["']og:title["'][^>]+content=["']([^"']+)["']""",RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
    ?: Regex("""<title>(.*?)</title>""",RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
    ?: "Video"
  val thumb=Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""",RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)
  return Triple(title.replace("&amp;","&"),0L,thumb)
 }

 fun download(info:VideoInfo,f:Format,out:File):Flow<Int> = channelFlow{
  val req=YtDlpRequest(info.url).setOutputTemplate(out.absolutePath).addOption("-f",f.id).addOption("--no-playlist").addOption("--newline")
  val job=YtDlp.executeAsync(req,object:DownloadProgressCallback{
   override fun onProgressUpdate(progress:Float,etaInSeconds:Long,line:String){trySend(progress.toInt().coerceIn(0,100))}
  })
  launch { try { job.get(); trySend(100) } finally { close() } }
 }
}