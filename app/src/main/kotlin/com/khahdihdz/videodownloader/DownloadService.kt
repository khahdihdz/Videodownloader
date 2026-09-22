package com.khahdihdz.videodownloader
import android.app.*;import android.content.*;import android.os.*;import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*;import java.io.File
class DownloadService:Service(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO);private val d=Downloader()
 override fun onCreate(){super.onCreate();getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("dl","Downloads",NotificationManager.IMPORTANCE_LOW));startForeground(1,n("Đang chuẩn bị",0))}
 override fun onStartCommand(i:Intent?,flags:Int,id:Int):Int{
  val url=i?.getStringExtra("url")?:return START_NOT_STICKY;val fid=i.getStringExtra("formatId")?:"best";val h=i.getIntExtra("height",720);val title=i.getStringExtra("title")?:"video"
  scope.launch{try{val info=d.analyze(url);val f=info.formats.firstOrNull{it.id==fid}?:Format(fid,h,h.toString()+"p");val tmp=File(cacheDir,"video_"+System.currentTimeMillis()+".mp4");d.download(info,f,tmp).collect{p->startForeground(1,n("Đang tải: "+title,p))};save(tmp,title);tmp.delete();startForeground(1,n("Đã tải xong",100));delay(1200)}catch(_:Throwable){startForeground(1,n("Tải video thất bại",0));delay(1200)}finally{stopSelf(id)}};return START_NOT_STICKY}
 private fun n(t:String,p:Int)=NotificationCompat.Builder(this,"dl").setSmallIcon(android.R.drawable.stat_sys_download).setContentTitle("Video Downloader").setContentText(t).setProgress(100,p,false).setOngoing(p in 1..99).build()
 private fun save(f:File,title:String){val v=ContentValues().apply{put(MediaStore.Video.Media.DISPLAY_NAME,title.replace(Regex("[\\\\/:*?"<>|]"),"_").take(100)+".mp4");put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/VideoDownloader");put(MediaStore.Video.Media.IS_PENDING,1)};val u=contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,v)?:error("MediaStore");contentResolver.openOutputStream(u)!!.use{o->f.inputStream().use{it.copyTo(o)}};v.clear();v.put(MediaStore.Video.Media.IS_PENDING,0);contentResolver.update(u,v,null,null)}
 override fun onDestroy(){scope.cancel();super.onDestroy()};override fun onBind(i:Intent?)=null
}