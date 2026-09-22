package com.khahdihdz.videodownloader
import android.content.*;import android.os.Bundle;import androidx.activity.ComponentActivity;import androidx.activity.compose.setContent;import androidx.compose.foundation.layout.*;import androidx.compose.material3.*;import androidx.compose.runtime.*;import androidx.compose.ui.Modifier;import androidx.compose.ui.unit.dp;import androidx.lifecycle.viewmodel.compose.viewModel;import coil.compose.AsyncImage
class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()};handle(intent)}
 override fun onNewIntent(i:Intent){super.onNewIntent(i);handle(i)}
 private fun handle(i:Intent?){if(i?.action==Intent.ACTION_SEND){val u=UrlDetector.extract(i.getStringExtra(Intent.EXTRA_TEXT).orEmpty());if(u!=null)sharedUrl=u}}
 companion object{var sharedUrl by mutableStateOf("")}
}
@Composable fun App(vm:MainVM=viewModel()){
 var url by remember{mutableStateOf(MainActivity.sharedUrl)};val info by vm.info.collectAsState();val err by vm.err.collectAsState();val loading by vm.loading.collectAsState()
 Column(Modifier.fillMaxSize().padding(20.dp)){Text("Video Downloader",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(16.dp));OutlinedTextField(url,{url=it},Modifier.fillMaxWidth(),singleLine=true,label={Text("Dán liên kết video")});Spacer(Modifier.height(10.dp));Button({vm.analyze(url)},Modifier.fillMaxWidth()){Text(if(loading)"Đang phân tích…" else "Phân tích")};err?.let{Text(it,color=MaterialTheme.colorScheme.error)}
 info?.let{x->AsyncImage(x.thumbnail,null,Modifier.fillMaxWidth().height(190.dp));Text(x.title,style=MaterialTheme.typography.titleLarge);Text(x.platform.name);Text("Chất lượng");if(x.formats.isEmpty()){val f=Format("best",0,"Tốt nhất");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(f.label);Button({vm.download(x,f)}){Text("Tải")}}}else{x.formats.forEach{f->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(f.label);Button({vm.download(x,f)}){Text("Tải")}}}}}
 }
}