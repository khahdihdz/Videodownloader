package com.khahdihdz.videodownloader
import com.sapher.youtubedl.YoutubeDL
import com.sapher.youtubedl.YoutubeDLRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.channels.awaitClose
import org.json.JSONObject
import java.io.File
data class Format(val id:String,val height:Int,val label:String)
data class VideoInfo(val url:String,val platform:VideoPlatform,val title:String,val duration:Long,val thumbnail:String?,val formats:List<Format>)
class Downloader {
 suspend fun analyze(url:String):VideoInfo {
  val r=YoutubeDLRequest(url).apply{addOption("--dump-single-json");addOption("--no-playlist");addOption("--no-warnings");addOption("--skip-download")}
  val root=JSONObject(YoutubeDL.execute(r).out); val fs=mutableListOf<Format>(); val a=root.optJSONArray("formats")
  if(a!=null) for(i in 0 until a.length()){ val f=a.optJSONObject(i)?:continue; val h=f.optInt("height",0); val v=f.optString("vcodec","none")!="none"; val au=f.optString("acodec","none")!="none"; if(v&&au&&h>0) fs+=Format(f.optString("format_id"),h,h.toString()+"p") }
  val clean=fs.distinctBy{it.height}.sortedBy{it.height}
  return VideoInfo(url,UrlDetector.detect(url),root.optString("title","Video"),root.optLong("duration",0),root.optString("thumbnail",null),clean)
 }
 fun download(info:VideoInfo,f:Format,out:File):Flow<Int>=channelFlow{
  val r=YoutubeDLRequest(info.url).apply{addOption("-f",f.id+"/best[height<="+f.height+"]/best");addOption("-o",out.absolutePath);addOption("--no-playlist");addOption("--newline")}
  YoutubeDL.execute(r){p,_->trySend(p.toInt().coerceIn(0,100)).isSuccess}
  trySend(100);close()
  awaitClose{}
 }
}