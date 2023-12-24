package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val messageList = remember { mutableStateListOf<ChatMessage>() }
            ChatFace(messageList)
        }
    }

    private fun sendQuestion(question: String, messageList: SnapshotStateList<ChatMessage>) {
        val url = "https://gpt.lucent.blog/v1/chat/completions"
        // 创建一个请求队列
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // 提问
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()
        val jsonObject2 = JSONObject()
        jsonObject2.put("role","user")
        jsonObject2.put("content",question)
        jsonArray.put(jsonObject2)
        jsonObject.put("model", "gpt-3.5-turbo")
        jsonObject.put("messages",jsonArray)
        jsonObject.put("temperature", 0.7)

        val postRequest: JsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonObject,
                Response.Listener { response ->
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                    Log.d("reply", "getResponse: " + responseMsg)
                    messageList.add(ChatMessage(responseMsg.trim(), 1))
                },
                Response.ErrorListener { error ->
                    Log.d("OpenAI", "getResponse: " + error.message + "\n" + error)
                    val msg = if(error.message!=null) error.message else "网络错误，请检查网络设置或稍后再试（可能需要一点点魔法喔）"
                    messageList.add(ChatMessage(msg!!, 1))
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer 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"
                    return params
                }
            }
        queue.add(postRequest)
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatFace(messageList: SnapshotStateList<ChatMessage>) {
        var text by remember { mutableStateOf("") }

        Column {
            Text(
                text = "ChatMysti",
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(resId = R.font.opposans_b)),
                modifier = Modifier
                    .padding(start = 30.dp, top = 30.dp, bottom = 30.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .wrapContentHeight(Alignment.Bottom)
            ) {
                items(messageList) {
                    ChatBlock(text = it.text, type = it.type)
                }
            }
            Row(
                Modifier
                    .padding(start = 30.dp, end = 20.dp, bottom = 15.dp, top = 15.dp)
                    .wrapContentHeight(Alignment.Bottom)
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Color.Black,
                        containerColor = Color(0xFFf2f2f2),
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        placeholderColor = Color(0xFFc9c9c9)
                    ),
                    placeholder = @Composable {
                        Text(
                            text = "来询问一下你的今日运势吧",
                            fontFamily = FontFamily(Font(resId = R.font.opposans_r)),
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(50.dp))
                )
                Box(
                    Modifier
                        .padding(start = 20.dp, bottom = 10.dp, top = 15.dp)
                        .background(
                            color = Color(0xFFFC9F88),
                            shape = RoundedCornerShape(37.dp)
                        )
                        .clickable {
                            if (text.isEmpty())
                            else {
                                messageList.add(ChatMessage(text, 0))

                                val question = text
                                text = ""

                                sendQuestion(question, messageList)
                            }
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "send message",
                        tint = Color.White,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .padding(7.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun ChatBlock(text:String,type:Int) {
        if(type==0)
            ChatBlocksRequire(text = text)
        else
            ChatBlocksRespond(text = text)
    }

    @Composable
    fun ChatBlocksRequire(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(align = Alignment.End)
                .padding(start = 30.dp, end = 30.dp, bottom = 10.dp, top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFC9F88))
                    .widthIn(max = 290.dp)
            ) {
                SelectionContainer{Text(
                    textAlign = TextAlign.Right,
                    text = text,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(resId = R.font.opposans_r)),
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 15.dp,
                        bottom = 15.dp
                    )
                )}
            }
        }
    }

    @Composable
    fun ChatBlocksRespond(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp, bottom = 10.dp, top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFf2f2f2))
                    .widthIn(max = 290.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(resId = R.font.opposans_r)),
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 15.dp,
                            bottom = 15.dp
                        )
                    )
                }
            }
        }
    }
}


data class ChatMessage(var text: String, var type: Int)