package com.imagebeatmaker.ai

import android.content.ContentValues
import android.media.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.*
import kotlin.math.PI
import kotlin.math.sin

data class Beat(val kick: MutableList<Boolean>, val snare: MutableList<Boolean>,
                val hihat: MutableList<Boolean>, val sticking: MutableList<String>)

class MainActivity: ComponentActivity() {
 override fun onCreate(b: Bundle?) { super.onCreate(b); setContent { App() } }

 private fun wavBytes(beat: Beat, bpm: Int): ByteArray {
  val sr=44100; val step=(sr*60.0/bpm/4.0).toInt(); val total=step*64
  val pcm=ShortArray(total)
  fun tone(s:Int,f:Double,d:Double,v:Double) {
   val st=s*step; val nmax=(sr*d).toInt()
   for(n in 0 until nmax) { val i=st+n; if(i>=total) break
    val env=1.0-n.toDouble()/nmax
    val x=(sin(2*PI*f*n/sr)*env*v*32767).toInt()
    pcm[i]=(pcm[i].toInt()+x).coerceIn(-32768,32767).toShort()
   }
  }
  for(i in 0 until 64) { if(beat.kick[i]) tone(i,65,.18,.75); if(beat.snare[i]) tone(i,180,.10,.35); if(beat.hihat[i]) tone(i,5000,.045,.16) }
  val out=ByteArrayOutputStream(); fun le(v:Int,n:Int){ repeat(n){out.write(v shr (8*it) and 255)} }
  out.write("RIFF".toByteArray()); le(36+pcm.size*2,4); out.write("WAVEfmt ".toByteArray()); le(16,4); le(1,2); le(1,2); le(sr,4); le(sr*2,4); le(2,2); le(16,2); out.write("data".toByteArray()); le(pcm.size*2,4)
  pcm.forEach{le(it.toInt(),2)}; return out.toByteArray()
 }

 private fun saveWav(bytes:ByteArray):Uri? {
  val cv=ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME,"image_beat_${System.currentTimeMillis()}.wav"); put(MediaStore.Downloads.MIME_TYPE,"audio/wav") }
  val u=contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,cv) ?: return null
  contentResolver.openOutputStream(u)?.use{it.write(bytes)}; return u
 }

 @Composable fun App() {
  val scope=rememberCoroutineScope(); var uri by remember{mutableStateOf<Uri?>(null)}; var status by remember{mutableStateOf("Upload a handwritten rhythm image.")}; var busy by remember{mutableStateOf(false)}; var bpm by remember{mutableIntStateOf(80)}
  val kick=remember{mutableStateListOf(*Array(64){false})}; val snare=remember{mutableStateListOf(*Array(64){false})}; val hh=remember{mutableStateListOf(*Array(64){false})}; val lr=remember{mutableStateListOf(*Array(64){""})}
  val picker=rememberLauncherForActivityResult(GetContent){uri=it; status=if(it==null)"No image selected." else "Image selected."}
  fun demo(){ for(i in 0 until 64){kick[i]=i%8==0; snare[i]=i%8==4; hh[i]=true; lr[i]=if(i%2==0)"L" else "R"}; status="Demo beat loaded."}
  MaterialTheme { Scaffold(topBar={TopAppBar(title={Text("Image Beat Maker AI")})}){p->Column(Modifier.padding(p).padding(12.dp).verticalScroll(rememberScrollState())) {
   Text("Handwritten image → AI → 64-step drum beat",fontSize=20.sp); Text("L/R sticking is preserved separately from Kick/Snare.")
   Spacer(Modifier.height(10.dp)); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({picker.launch("image/*")}){Icon(Icons.Default.PhotoLibrary,null);Text(" Image")};Button(enabled=uri!=null&&!busy,onClick={
    busy=true; status="Analyzing image with AI…"; scope.launch(Dispatchers.IO){try{
     val bytes=contentResolver.openInputStream(uri!!)!!.readBytes(); val req=Request.Builder().url(BuildConfig.API_BASE_URL+"/api/analyze").post(MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image","beat.jpg",bytes.toRequestBody("image/jpeg".toMediaType())).build()).build()
     OkHttpClient().newCall(req).execute().use{r-> if(!r.isSuccessful) throw Exception("Server ${r.code}"); val o=JSONObject(r.body!!.string()); val k=o.getJSONArray("kick"); val s=o.getJSONArray("snare"); val h=o.getJSONArray("hihat"); val st=o.getJSONArray("sticking"); withContext(Dispatchers.Main){for(i in 0 until 64){kick[i]=k.getBoolean(i);snare[i]=s.getBoolean(i);hh[i]=h.getBoolean(i);lr[i]=st.getString(i)};status=o.optString("explanation","AI analysis complete.");busy=false}}
    }catch(e:Exception){withContext(Dispatchers.Main){busy=false;status="AI error: ${e.message}. Check server URL."}}}
   }){if(busy)CircularProgressIndicator(Modifier.size(18.dp))else Icon(Icons.Default.AutoAwesome,null);Text(" AI Analyze")}}
   Spacer(Modifier.height(8.dp)); Text(status,fontSize=13.sp); Spacer(Modifier.height(10.dp))
   Row(verticalAlignment=Alignment.CenterVertically){Text("BPM $bpm");Slider(Modifier.weight(1f),bpm.toFloat(),{bpm=it.toInt()},40f..240f)}
   Text("64-Step Grid",fontSize=18.sp); Spacer(Modifier.height(4.dp))
   Row(Modifier.horizontalScroll(rememberScrollState())){Column(Modifier.width(58.dp)){listOf("KICK","SNARE","HH","L/R").forEach{Box(Modifier.height(36.dp),contentAlignment=Alignment.CenterStart){Text(it,fontSize=10.sp)}}};Column{
    repeat(4){r->Row{for(i in 0 until 64){val on=when(r){0->kick[i];1->snare[i];2->hh[i];else->lr[i].isNotEmpty()};Box(Modifier.width(25.dp).height(36.dp).padding(1.dp).background(if(on)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,RoundedCornerShape(3.dp)).clickable{when(r){0->kick[i]=!kick[i];1->snare[i]=!snare[i];2->hh[i]=!hh[i];else->lr[i]=if(lr[i]=="L")"R" else if(lr[i]=="R")"" else "L"}} ,contentAlignment=Alignment.Center){if(r==3&&lr[i].isNotEmpty())Text(lr[i],fontSize=9.sp,color=MaterialTheme.colorScheme.onPrimary)}}}}
   }}
   Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({scope.launch(Dispatchers.Default){play(kick,snare,hh,bpm)}}){Icon(Icons.Default.PlayArrow,null);Text(" Play")};Button({demo()}){Icon(Icons.Default.AutoAwesome,null);Text(" Demo")};Button({kick.fill(false);snare.fill(false);hh.fill(false);lr.fill("")}){Text("Clear")}}
   Spacer(Modifier.height(8.dp));Button(onClick={val b=Beat(kick.toMutableList(),snare.toMutableList(),hh.toMutableList(),lr.toMutableList());val u=saveWav(wavBytes(b,bpm));status=if(u!=null)"WAV saved to Downloads." else "Export failed."}){Icon(Icons.Default.Download,null);Text(" Export WAV")}
   Spacer(Modifier.height(6.dp));Text("MP3 export: the included backend also supports adding server-side MP3 conversion (FFmpeg/LAME). Android's built-in encoders do not guarantee MP3 output on every device.",fontSize=11.sp)
  }}}
 }
 private fun play(k:List<Boolean>,s:List<Boolean>,h:List<Boolean>,bpm:Int){val sr=44100;val step=(sr*60.0/bpm/4).toInt();val total=step*64;val p=ShortArray(total);fun t(st:Int,f:Double,d:Double,v:Double){val a=st*step;val n=(sr*d).toInt();for(x in 0 until n){val i=a+x;if(i>=total)break;p[i]=(p[i]+(sin(2*PI*f*x/sr)*(1-x.toDouble()/n)*v*32767).toInt()).toShort()}};for(i in 0 until 64){if(k[i])t(i,65,.18,.75);if(s[i])t(i,180,.1,.35);if(h[i])t(i,5000,.045,.16)};val at=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(AudioFormat.Builder().setSampleRate(sr).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(p.size*2).setTransferMode(AudioTrack.MODE_STATIC).build();at.write(p,0,p.size);at.play();Thread.sleep((step*64L*1000/sr)+100);at.release()}
}
