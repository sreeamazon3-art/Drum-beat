require("dotenv").config(); const express=require("express"),multer=require("multer"),OpenAI=require("openai");
const app=express(), upload=multer({storage:multer.memoryStorage(),limits:{fileSize:10*1024*1024}}); const client=new OpenAI({apiKey:process.env.OPENAI_API_KEY});
app.get("/health",(q,s)=>s.json({ok:true}));
app.post("/api/analyze",upload.single("image"),async(req,res)=>{try{if(!req.file)return res.status(400).json({error:"image required"});
 const data=req.file.buffer.toString("base64"); const prompt=`Analyze this handwritten rhythm notation for a drum machine. L means left hand and R means right hand; do NOT assume L=kick or R=snare. Infer the rhythmic events and produce exactly 64 sixteenth-note steps. Return kick, snare, hihat as 64 booleans and sticking as 64 strings (L,R,or empty). Keep the rhythm faithful to the image.`;
 const r=await client.chat.completions.create({model:process.env.OPENAI_MODEL||"gpt-5.6-luna",response_format:{type:"json_object"},messages:[{role:"user",content:[{type:"text",text:prompt},{type:"image_url",image_url:{url:`data:${req.file.mimetype};base64,${data}`}}]}]});
 const o=JSON.parse(r.choices[0].message.content); for(const k of ["kick","snare","hihat","sticking"])if(!Array.isArray(o[k])||o[k].length!==64)throw Error(k+" must have 64 entries"); res.json(o);
}catch(e){console.error(e);res.status(500).json({error:e.message})}});
app.listen(process.env.PORT||3000,()=>console.log("AI backend running"));
