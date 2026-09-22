package com.khahdihdz.videodownloader
import java.net.URI
enum class VideoPlatform { YOUTUBE, FACEBOOK, UNKNOWN }
object UrlDetector {
 private val re=Regex("""https?://[^\s<>"']+""",RegexOption.IGNORE_CASE)
 private val tracking=setOf("utm_source","utm_medium","utm_campaign","utm_term","utm_content","fbclid","gclid")
 fun extract(text:String):String?=re.find(text)?.value?.trimEnd('.',',',')',']','}')
 fun normalize(raw:String):String{
  val u=URI(raw.trim()); val host=(u.host?:"").lowercase().removePrefix("www.")
  val q=u.rawQuery?.split("&")?.filter{it.substringBefore("=").lowercase() !in tracking}?.joinToString("&")
  return URI(u.scheme.lowercase(),u.userInfo,host,u.port,u.path,q,null).toString()
 }
 fun detect(raw:String):VideoPlatform=runCatching{
  val h=URI(normalize(raw)).host.lowercase().removePrefix("www.")
  when { h=="youtube.com"||h=="youtu.be"||h.endsWith(".youtube.com")->VideoPlatform.YOUTUBE
         h=="facebook.com"||h.endsWith(".facebook.com")||h=="fb.watch"->VideoPlatform.FACEBOOK
         else->VideoPlatform.UNKNOWN }
 }.getOrDefault(VideoPlatform.UNKNOWN)
}